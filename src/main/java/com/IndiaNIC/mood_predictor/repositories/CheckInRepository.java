package com.IndiaNIC.mood_predictor.repositories;


import com.IndiaNIC.mood_predictor.entities.CheckIn;
import com.IndiaNIC.mood_predictor.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    Optional<CheckIn> findByUserAndCheckInDate(User user, LocalDate checkInDate);

    List<CheckIn> findByUserOrderByCreatedAtDesc(User user);
}