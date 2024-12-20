package org.example;

public class AppUser {
    public int id;
    public String username;
    public String password; // Поле для хранения пароля


    public AppUser(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
}

