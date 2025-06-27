package com.IndiaNIC.mood_predictor.services;

import com.IndiaNIC.mood_predictor.dtos.QuestionsResponseDto;
import com.IndiaNIC.mood_predictor.entities.CheckIn;
import com.IndiaNIC.mood_predictor.entities.User;
import com.IndiaNIC.mood_predictor.repositories.CheckInRepository;
import com.IndiaNIC.mood_predictor.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public CheckInService(CheckInRepository checkInRepository, UserRepository userRepository, GeminiService geminiService) {
        this.checkInRepository = checkInRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
    }

    public QuestionsResponseDto getDailyCheckInQuestions(User user) {

        Optional<CheckIn> existingCheckIn = checkInRepository.findByUserAndCheckInDate(user, LocalDate.now());

        if (existingCheckIn.isPresent()) {
            throw new IllegalStateException("You have already completed a mood check-in for today.");
        }

        String questionsJson = geminiService.generateCheckinQuestions();

        return new QuestionsResponseDto(questionsJson);
    }

    public String submitCheckIn(User user, String questionsJson, String answersJson) {

        String predictedMood = geminiService.predictMood(questionsJson, answersJson);

        CheckIn newCheckIn = new CheckIn();
        newCheckIn.setUser(user);

        newCheckIn.setQuestions(questionsJson);
        newCheckIn.setAnswers(answersJson);
        newCheckIn.setPredictedMood(predictedMood);

        checkInRepository.save(newCheckIn);

        return predictedMood;
    }
}
