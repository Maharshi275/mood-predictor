package com.IndiaNIC.mood_predictor.dtos;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {

    private String username;

    private String password;
}
