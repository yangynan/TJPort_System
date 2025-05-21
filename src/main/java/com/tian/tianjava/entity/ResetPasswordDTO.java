package com.tian.tianjava.entity;

import lombok.*;

import java.time.LocalDateTime;
@Setter
@Getter
@Data
public class ResetPasswordDTO {
    private String email;
    private String newPassword;
    private String emailCode;
    // getters/setters
}