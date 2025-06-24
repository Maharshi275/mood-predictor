package com.IndiaNIC.mood_predictor.services;


import com.IndiaNIC.mood_predictor.dtos.UserLoginDto;
import com.IndiaNIC.mood_predictor.dtos.UserRegisterationDto;
import com.IndiaNIC.mood_predictor.entities.User;
import com.IndiaNIC.mood_predictor.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserRegisterationDto registerationDto) {

        if(userRepository.existsByUsername(registerationDto.getUsername())) {
            throw new IllegalArgumentException("Username " + registerationDto.getUsername() + " already exists.");
        }

        if(userRepository.existsByEmail(registerationDto.getEmail())) {
            throw new IllegalArgumentException("Email " + registerationDto.getEmail() + " already exists.");
        }

        User newUser = new User();
        newUser.setUsername(registerationDto.getUsername());
        newUser.setEmail(registerationDto.getEmail());
        newUser.setPasswordHash(registerationDto.getPassword());
        return userRepository.save(newUser);
    }

    public User userLogin(UserLoginDto loginDto) {
        Optional<User> userOptional = userRepository.findByUsername(loginDto.getUsername());

        if(userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Username or password.");
        }

        User user = userOptional.get();

        if(!user.getPasswordHash().equals(loginDto.getPassword())) {
            throw new IllegalArgumentException("Invalid username or Password.");
        }

        return user;
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
}
