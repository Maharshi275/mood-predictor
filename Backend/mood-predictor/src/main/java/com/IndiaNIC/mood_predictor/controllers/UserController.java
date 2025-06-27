package com.IndiaNIC.mood_predictor.controllers;

import com.IndiaNIC.mood_predictor.dtos.UserLoginDto;
import com.IndiaNIC.mood_predictor.dtos.UserRegisterationDto;
import com.IndiaNIC.mood_predictor.entities.User;
import com.IndiaNIC.mood_predictor.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterationDto registerationDto) {
        try {
            User registeredUser =  userService.registerUser(registerationDto);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDto loginDto) {
        try {
            User loggedUser = userService.userLogin(loginDto);
            return new ResponseEntity<>(loggedUser, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
