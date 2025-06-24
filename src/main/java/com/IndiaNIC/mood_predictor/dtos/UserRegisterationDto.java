package com.IndiaNIC.mood_predictor.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterationDto {

    private String username;

    private String email;

    private String password;
}
