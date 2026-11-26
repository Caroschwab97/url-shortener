package com.shortener.link;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/** Genera códigos aleatorios en base62 (a-z, A-Z, 0-9). */
@Component
public class CodeGenerator {

    public static final int CODE_LENGTH = 7;
    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
