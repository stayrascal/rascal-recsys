package com.stayrascal.service.application.controller;

import com.stayrascal.service.application.constraints.Error;
import com.stayrascal.service.application.domain.User;
import com.stayrascal.service.application.dto.result.ErrorResult;
import com.stayrascal.service.application.user.UserAlreadyExistsException;
import com.stayrascal.service.application.user.UserNotFoundException;
import com.stayrascal.service.application.user.UserService;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class UsersController {

  private UserService service;
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  public UsersController(UserService service) {
    this.service = service;
  }

  @PostMapping(value = "/api/v1/users/login")
  public ResponseEntity login(@RequestBody User user) {
    logger.debug("User: {} try to login.", user.getId());
    Optional<User> userOptional = service.selectUserById(user.getId());
    if (userOptional.isPresent()){
      return ResponseEntity.ok().build();
    }else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity handleUserNotFound(UserNotFoundException e) {
    Error error = new Error(e.getMessage(), 101);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResult(error));
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity handleUserAlreadyExist(UserAlreadyExistsException e) {
    Error error = new Error(e.getMessage(), 102);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResult(error));
  }
}
