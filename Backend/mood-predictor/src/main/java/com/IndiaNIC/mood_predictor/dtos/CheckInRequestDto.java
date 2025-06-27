package com.IndiaNIC.mood_predictor.dtos;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequestDto {
    private UUID userId;
    private String questions;
    private String answers;
}
