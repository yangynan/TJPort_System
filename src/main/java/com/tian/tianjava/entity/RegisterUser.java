package com.tian.tianjava.entity;
import lombok.*;

import java.time.LocalDateTime;
@Setter
@Getter
@Data
public class RegisterUser {

        private String username;
        private String password;
        private String email;
        private String emailCode;

        // getters and setters

}
