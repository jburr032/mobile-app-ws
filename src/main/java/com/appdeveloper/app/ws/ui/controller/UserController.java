package com.appdeveloper.app.ws.ui.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appdeveloper.app.ws.exceptions.UserServiceException;
import com.appdeveloper.app.ws.model.request.UserDetailsRequestModel;
import com.appdeveloper.app.ws.model.response.ErrorMessages;
import com.appdeveloper.app.ws.model.response.OperationStatusModel;
import com.appdeveloper.app.ws.model.response.UserRest;
import com.appdeveloper.app.ws.service.impl.UserServiceImpl;
import com.appdeveloper.app.ws.shared.dto.UserDto;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserServiceImpl userService;

	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public UserRest getUser(@PathVariable String id) {

		try {
			UserRest returnValue = new UserRest();

			UserDto userDto = userService.getUserByUserId(id);

			BeanUtils.copyProperties(userDto, returnValue);

			return returnValue;
		} catch (Exception e) {
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		}

	}

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) {

		if (userDetails.getFirstName().isEmpty() || userDetails.getLastName().isEmpty()
				|| userDetails.getEmail().isEmpty() || userDetails.getPassword().isEmpty()) {
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		}

		// Use the user response object
		UserRest returnValue = new UserRest();

		// Create user transfer object
		UserDto userDto = new UserDto();

		// Copy request body info to user transfer object
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto createdUser = userService.createUser(userDto);

		BeanUtils.copyProperties(createdUser, returnValue);

		return returnValue;

	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		// Use the user response object
		UserRest returnValue = new UserRest();

		// Create user transfer object
		UserDto userDto = new UserDto();

		// Copy request body info to user transfer object
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updatedUser = userService.updateUser(id, userDto);

		BeanUtils.copyProperties(updatedUser, returnValue);

		return returnValue;
	}

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {
		if (id == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		OperationStatusModel returnValue = new OperationStatusModel();

		try {
			returnValue.setOperationName(RequestOperationName.DELETE.name());
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

			userService.deleteUser(id);

			return returnValue;

		} catch (Exception e) {
			returnValue.setOperationName(RequestOperationName.DELETE.name());
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

			return returnValue;
		}

	}

	@GetMapping
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "25") int limit) {
		
		List<UserRest> returnValue = new ArrayList<>();
		List<UserDto> userValues = userService.getUsers(page, limit);
		
		for(UserDto userDto: userValues) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			
			returnValue.add(userModel);
		}

		return returnValue;
	}

}
