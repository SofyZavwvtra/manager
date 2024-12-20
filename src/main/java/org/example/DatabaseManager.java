package org.example;

import java.sql.*;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager(String dbUrl) {
        try {
            connection = DriverManager.getConnection(dbUrl);
            createTables();
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    public void createTables() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE, " +
                        "password TEXT);")) {
            stmt.execute();
        } catch (SQLException e) {
            System.err.println("Ошибка создания таблиц: " + e.getMessage());
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS passwords (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "service TEXT, " +
                        "encrypted_password TEXT, " +
                        "method TXT, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id));")) {
            stmt.execute();
        } catch (SQLException e) {
            System.err.println("Ошибка создания таблиц: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }

    public Integer addUser(String username, String password) {
        //проверим существует ли уже юзер в базе данных
        String sql1 = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement pstmt1 = connection.prepareStatement(sql1)) {
            pstmt1.setString(1, username);
            ResultSet rs = pstmt1.executeQuery();
            if (rs.next()){
                return 2;
            }
        }catch (SQLException e){
            System.err.println("Ошибка при поиске пользователя: " + e.getMessage());
            return  0;
        }


        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            System.err.println("Ошибка добавления пользователя: " + e.getMessage());
            return  0;
        }
    }

    public AppUser getUser(String username, String password) { // Изменено на AppUser


        String sql = "SELECT id, username, password FROM users WHERE username = ? AND password = ?";
        AppUser user = null;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new AppUser(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                }
                else {
                    String sql1 = "SELECT id, username, password FROM users WHERE username = ?";
                    PreparedStatement stmt1 = connection.prepareStatement(sql1);
                    stmt1.setString(1, username);
                    ResultSet rs1 = stmt1.executeQuery();
                    if (rs1.next()) {
                        user = new AppUser(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                        System.out.println("Неверный пароль");
                        return null;
                    } else {
                        System.out.println("Пользователя не существует");
                        return null;

                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения пользователя: " + e.getMessage());
            return null;
        }
        return user;

    }





    public void deletePassword(int userId, String service) {
        String sql = "DELETE FROM passwords WHERE user_id = ? AND service = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, service);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка удаления пароля: " + e.getMessage());
        }
    }

    public Password[] getPasswords(int userId) { // Возвращаем массив паролей
        String sql = "SELECT service, encrypted_password, method FROM passwords WHERE user_id = ?";

        // Сначала подсчитываем количество паролей
        int count = 0;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) count++; // Считаем количество паролей
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения количества паролей: " + e.getMessage());
        }

        // Создаем массив нужного размера
        Password[] passwords = new Password[count];

        // Заполняем массив паролей
        int index = 0;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    passwords[index++] = new Password(rs.getString("service"), rs.getString("encrypted_password"), rs.getString("method"));

                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения паролей: " + e.getMessage());
        }

        return passwords; // Возвращаем массив паролей
    }

    public void addPassword(int userId, Password password) {
        String sql = "INSERT INTO passwords (user_id, service, encrypted_password, method) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, password.service);
            pstmt.setString(3, password.encryptedPassword);
            pstmt.setString(4, password.method);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка добавления пароля: " + e.getMessage());
        }
    }
}

