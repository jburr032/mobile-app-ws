package com.appdeveloper.app.ws.service.impl;

import com.appdeveloper.app.ws.share.Utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appdeveloper.app.ws.UserRepository;
import com.appdeveloper.app.ws.exceptions.UserServiceException;
import com.appdeveloper.app.ws.io.entity.UserEntity;
import com.appdeveloper.app.ws.model.response.ErrorMessages;
import com.appdeveloper.app.ws.service.UserService;
import com.appdeveloper.app.ws.shared.dto.UserDto;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private Utils utils;

	@Autowired

	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDto createUser(UserDto user) {

		if (userRepo.findByEmail(user.getEmail()) != null)
			throw new RuntimeException("Record already exists");

		// Create a user entity object to save to DB
		UserEntity newUser = new UserEntity();

		// Copy user to newUser object
		BeanUtils.copyProperties(user, newUser);

		newUser.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

		String generatedUserId = utils.generateUserId(30);
		newUser.setUserId(generatedUserId);

		// Save user entity to DB
		UserEntity storedUser = userRepo.save(newUser);

		// Create userDto with UserEntity object
		UserDto returnValue = new UserDto();

		BeanUtils.copyProperties(storedUser, returnValue);

		return returnValue;

	}

	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepo.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		// Create userDto with UserEntity object
		UserDto returnValue = new UserDto();

		BeanUtils.copyProperties(userEntity, returnValue);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepo.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUserByUserId(String id) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepo.findByUserId(id);

		if (userEntity == null)
			throw new RuntimeException("Record already exists");

		BeanUtils.copyProperties(userEntity, returnValue);

		return returnValue;
	}

	@Override
	public UserDto updateUser(String id, UserDto userDetails) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepo.findByUserId(id);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setFirstName(userDetails.getFirstName());
		userEntity.setLastName(userDetails.getLastName());

		BeanUtils.copyProperties(userEntity, returnValue);

		return returnValue;

	}
	
	@Override
	public void deleteUser(String id) {
		UserEntity userEntity = userRepo.findByUserId(id);
		
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		
		userRepo.delete(userEntity);
		
	}
	
	@Override
	public List<UserDto> getUsers(int page, int limit){
		List<UserDto> returnValue = new ArrayList<>();
		
		if(page > 0) page = page - 1;
		
		Pageable pageableRequest = PageRequest.of(page, limit);
		
		
		Page<UserEntity> usersPage = userRepo.findAll(pageableRequest);
		List<UserEntity> usersList = usersPage.getContent();
		
		for(UserEntity userEntity: usersList) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			
			returnValue.add(userDto);
		}

		return returnValue;
	}

}
