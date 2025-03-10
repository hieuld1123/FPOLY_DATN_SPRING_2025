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
}
