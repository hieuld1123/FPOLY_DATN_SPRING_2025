package com.example.datnsd26.utilities;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CommonUtils {

    public static String generateInvoiceCode() {
        Random random = new Random();
        int randomNumber = random.nextInt(9000) + 1000;
        return "HD" + randomNumber;
    }

    public static String generateCustomerCode() {
        Random random = new Random();
        int randomNumber = random.nextInt(9000) + 1000;
        return "KH" + randomNumber;
    }

    public static String generateRandomPassword(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Độ dài mật khẩu phải lớn hơn 0");
        }

        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialCharacters = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        String allCharacters = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        if (length >= 4) {
            password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
            password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
            password.append(numbers.charAt(random.nextInt(numbers.length())));
            password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

            for (int i = 4; i < length; i++) {
                password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
            }

            char[] passwordArray = password.toString().toCharArray();
            for (int i = passwordArray.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                char temp = passwordArray[i];
                passwordArray[i] = passwordArray[j];
                passwordArray[j] = temp;
            }
            return new String(passwordArray);
        } else {
            for (int i = 0; i < length; i++) {
                password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
            }
            return password.toString();
        }
    }
}