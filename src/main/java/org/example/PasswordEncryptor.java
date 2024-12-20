package org.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

// Класс для шифрования паролей
class PasswordEncryptor {



    // Метод для шифрования пароля в формате Base64
    public String encryptBase64(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
    }

    // Метод для шифрования пароля с использованием MD5
    public String encryptMD5(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(password.getBytes());

        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    // Метод для шифрования пароля с солью
    public String encryptWithSalt(String password) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);

        byte[] hashedPassword = md.digest(password.getBytes());

        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hashedPassword);
    }

    // Метод для шифрования с использованием шифра Фейстеля
    public String encryptFeistel(String password) {
        return FeistelCipher.encrypt(password);
    }


    //расшифровка

    public String decryptBase64(String encryptpass){
        if (encryptpass == null || encryptpass.isEmpty()) {
            return null; // Возвращаем null, если строка null или пустая
        }

        try {
            // Декодируем строку из Base64 в массив байтов.
            byte[] decodedBytes = Base64.getDecoder().decode(encryptpass);

            // Преобразуем байты в строку, используя UTF-8 (можно изменить на другую кодировку).
            return new String(decodedBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            // Обрабатываем ошибку, если строка не валидный Base64
            System.err.println("Ошибка при расшифровке Base64: " + e.getMessage());
            return null; // Возвращаем null в случае ошибки
        }
    }



    public String decryptFeistel(String encrpt_pass){
        return  FeistelCipher.decrypt(encrpt_pass);
    }
}
