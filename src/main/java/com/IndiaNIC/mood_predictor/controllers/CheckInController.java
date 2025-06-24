package com.IndiaNIC.mood_predictor.controllers;

import com.IndiaNIC.mood_predictor.dtos.CheckInRequestDto;
import com.IndiaNIC.mood_predictor.dtos.QuestionsResponseDto;
import com.IndiaNIC.mood_predictor.entities.User;
import com.IndiaNIC.mood_predictor.services.CheckInService;
import com.IndiaNIC.mood_predictor.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;

    private final UserService userService;

    public CheckInController(CheckInService checkInService, UserService userService) {
        this.checkInService = checkInService;
        this.userService = userService;
    }

    @GetMapping("/questions/{userId}")
    public ResponseEntity<?> getCheckInQuestions(@PathVariable UUID userId) {
        try {
            User user = userService.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

            QuestionsResponseDto questionsDto = checkInService.getDailyCheckInQuestions(user);

            return new ResponseEntity<>(questionsDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            System.err.println("Error getting check-in questions : " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<?> submitCheckIn(@RequestBody CheckInRequestDto checkInRequestDto) {
        try {
            User user = userService.findById(checkInRequestDto.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found with ID : " + checkInRequestDto.getUserId()));

            String predictedMood = checkInService.submitCheckIn(
                    user,
                    checkInRequestDto.getQuestions(),
                    checkInRequestDto.getAnswers()
            );

            return new ResponseEntity<>(predictedMood, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error submitting check-in : " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("An unexpected error occurred : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

