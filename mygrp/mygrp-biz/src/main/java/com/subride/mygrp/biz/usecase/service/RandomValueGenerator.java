package com.subride.mygrp.biz.usecase.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

@Service
public class RandomValueGenerator {
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int LENGTH = 8;

    private final Set<String> generatedValues = new HashSet<>();
    private final SecureRandom random = new SecureRandom();

    public String generateUniqueRandomValue() {
        String randomValue;
        do {
            randomValue = generateRandomValue();
        } while (!generatedValues.add(randomValue));
        return randomValue;
    }

    private String generateRandomValue() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
