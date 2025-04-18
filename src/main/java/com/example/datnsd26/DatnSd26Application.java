package com.example.datnsd26;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class DatnSd26Application {

    public static void main(String[] args) {
        SpringApplication.run(DatnSd26Application.class, args);

    }

}
