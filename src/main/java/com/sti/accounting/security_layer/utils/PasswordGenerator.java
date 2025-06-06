package com.sti.accounting.security_layer.utils;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class PasswordGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private enum CharGroup {
        LOWERCASE("abcdefghijklmnopqrstuvwxyz"),
        UPPERCASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
        DIGITS("0123456789"),
        SPECIAL("!@#$%&*()_+-=[]?");

        private final String chars;

        CharGroup(String chars) {
            this.chars = chars;
        }

        public char randomChar() {
            return chars.charAt(RANDOM.nextInt(chars.length()));
        }

        public String getChars() {
            return chars;
        }
    }

    private static final String ALL_CHARS = String.join("",
            CharGroup.LOWERCASE.getChars(),
            CharGroup.UPPERCASE.getChars(),
            CharGroup.DIGITS.getChars(),
            CharGroup.SPECIAL.getChars()
    );

    public String generatePassword(int length) {

        validateLength(length);

        // Garantizar al menos un carácter de cada grupo
        StringBuilder password = new StringBuilder(length);
        Set<CharGroup> usedGroups = EnumSet.noneOf(CharGroup.class);

        // Primero añadir un carácter de cada grupo obligatorio
        for (CharGroup group : CharGroup.values()) {
            char randomChar = group.randomChar();
            password.append(randomChar);
            usedGroups.add(group);
        }

        // Completar con caracteres aleatorios
        IntStream.range(CharGroup.values().length, length)
                .forEach(i -> {
                    char randomChar = ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length()));
                    password.append(randomChar);
                });

        return shuffle(password.toString());
    }

    private void validateLength(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("The password must be at least 8 characters long.");
        }
    }

    private String shuffle(String input) {
        List<Character> chars = input.chars()
                .mapToObj(c -> (char)c)
                .collect(Collectors.toList());

        Collections.shuffle(chars, RANDOM);

        return chars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}