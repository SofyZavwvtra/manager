package org.example;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class FeistelCipher {

    private static final int ROUNDS = 16;

    public static String encrypt(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (text.length() % 2 != 0) {
            text = text + "#";
        }
        String left = text.substring(0, text.length() / 2);
        String right = text.substring(text.length() / 2);

        for (int i = 0; i < ROUNDS; i++) {
            String previousLeft = left;
            left = right;
            right = xor(previousLeft, f(right, i));
        }
        return left + right;
    }

    public static String decrypt(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (text.length() % 2 != 0) {
            return null;
        }
        String left = text.substring(0, text.length() / 2);
        String right = text.substring(text.length() / 2);

        for (int i = ROUNDS - 1; i >= 0; i--) {
            String previousRight = right;
            right = left;
            left = xor(previousRight, f(left, i));
        }
        if (left.endsWith("#")) {
            left = left.substring(0, left.length() - 1);
        }
        return left + right;
    }

    private static String f(String text, int round) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append((char) (c + round));
        }
        return sb.toString();
    }

    private static String xor(String str1, String str2) {
        StringBuilder result = new StringBuilder();
        int len = Math.min(str1.length(), str2.length());
        for (int i = 0; i < len; i++) {
            result.append((char) (str1.charAt(i) ^ str2.charAt(i)));
        }
        return result.toString();
    }

}