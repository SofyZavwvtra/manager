package org.example;

public class Password {
    public String service;          // Название сервиса
    public String encryptedPassword; // Зашифрованный пароль
    public String method; // Зашифрованный пароль

    public Password(String service, String encryptedPassword) {
        this.service = service;
        this.encryptedPassword = encryptedPassword;
        this.method = "";
    }

    public Password(String service, String encryptedPassword, String method) {
        this.service = service;
        this.encryptedPassword = encryptedPassword;
        this.method = method;
    }
}
