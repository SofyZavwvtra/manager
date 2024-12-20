package org.example;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import java.util.Scanner;
import java.util.regex.Pattern;

public class PasswordManagerApp {
    private static final Pattern ALLOWED_ASCII = Pattern.compile("^[\\x00-\\x7F]+$");
    private DatabaseManager dbManager; // Менеджер базы данных

    public PasswordManagerApp(String dbUrl){
        this.dbManager=new DatabaseManager(dbUrl);
    }

    public void run(){
        dbManager.createTables();
        Scanner scanner=new Scanner(System.in, StandardCharsets.UTF_8);

        while(true){
            System.out.println("1. Регистрация");
            System.out.println("2. Вход");
            System.out.println("3. Выход");

            int choice;
            try {

            if(scanner.hasNextInt()){

                choice=scanner.nextInt();
                scanner.nextLine();

                if(choice==1){
                    registerUser(scanner);
                } else if(choice==2){
                    authenticateUser(scanner);
                } else if(choice==3){
                    dbManager.closeConnection();
                    scanner.close();
                    return;
                } else{
                    System.out.println("Неверный выбор.");
                }
            }
            } catch (Exception e) {
                System.out.println("Пожалуйста, введите цифру!");
                scanner.next(); // Очистка некорректного ввода
            }
        }
    }

    private void registerUser(Scanner scanner){
        String username;
        String userpass;
        boolean isValid;

        // Ввод имени пользователя с проверкой
        do {
            System.out.print("Введите имя пользователя (разрешены все символы ASCII): ");
            username = scanner.nextLine();
            isValid = ALLOWED_ASCII.matcher(username).matches();

            if (!isValid) {
                System.out.println("Ошибка: Имя пользователя может содержать только ASCII символы.");
            }
        } while (!isValid);

        // Ввод пароля с проверкой
        do {
            System.out.print("Введите пароль (разрешены все символы ASCII): ");
            userpass = scanner.nextLine();
            isValid = ALLOWED_ASCII.matcher(userpass).matches();
            if (!isValid) {
                System.out.println("Ошибка: Пароль может содержать только ASCII символы.");
            }
        } while (!isValid);

        int check = dbManager.addUser(username,userpass); // 0 - error, 1 - все крутяк, 2 - пользователь уже существует
        switch (check) {
            case (1):
                System.out.println("Пользователь зарегистрирован.");
                break;
            case (2):
                System.out.println("Пользователь с таким именем уже существует!");
                break;
        }
    }


    private void authenticateUser(Scanner scanner){
        // Ввод имени пользователя с проверкой
        String username = "";
        String userpass;
        boolean isValid;

        do {
            System.out.print("Введите имя пользователя: ");
            username = scanner.nextLine();
            isValid = ALLOWED_ASCII.matcher(username).matches();
            if (!isValid) {
                System.out.println("Ошибка: Имя пользователя может содержать только латинские буквы и цифры.");
            }
        } while (!isValid);

        // Ввод пароля с проверкой
        do {
            System.out.print("Введите пароль: ");
            userpass = scanner.nextLine();
            isValid = ALLOWED_ASCII.matcher(userpass).matches();
            if (!isValid) {
                System.out.println("Ошибка: Некорректные символы");
            }
        } while (!isValid);

        AppUser user=dbManager.getUser(username, userpass);

        if(user!=null){
            System.out.println("Добро пожаловать "+user.username+"!");
            managePasswords(scanner, user.id);

        }
    }

    private void managePasswords(Scanner scanner,int userId){
        while(true){

            System.out.println("\nВыберите действие с паролями:");
            System.out.println("1. Добавить пароль");
            System.out.println("2. Посмотреть пароли");
            System.out.println("3. Удалить пароль");
            System.out.println("4. Выйти из аккаунта");

            int actionChoice;

            if(scanner.hasNextInt()){
                actionChoice=scanner.nextInt();
                scanner.nextLine();

                switch (actionChoice) {
                    case 1:
                        addPassword(scanner, userId);
                        break;
                    case 2:
                        viewPasswords(userId);
                        break;
                    case 3:
                        deletePassword(scanner, userId);
                        break;
                    case 4:
                        return;
                }

            }else{
                System.out.println("Пожалуйста вводите только цифры.");
                scanner.next();
            }

        }

    }

    private void addPassword(Scanner scanner,int userId){

        String service = "";
        String pass;
        boolean isValid;

        do {
            System.out.print("\nВведите название сервиса: ");
            service = scanner.nextLine();
            isValid = ALLOWED_ASCII.matcher(service).matches();
            if (!isValid) {
                System.out.println("Ошибка: Некорректные символы");
            }
        } while (!isValid);


        do {
            System.out.print("Введите пароль: ");
            pass = scanner.nextLine();
            isValid = ALLOWED_ASCII.matcher(pass).matches();
            if (!isValid) {
                System.out.println("Ошибка: Некорректные символы");
            }
        } while (!isValid);



        Password password = new Password(service, "");

        System.out.print("Выберете способ шифрования:\n" +
                "1 Base64\n" +
                "2. MD5\n" +
                "3. Шифр Фестеля\n" +
                "4. Подсаливание (с солью)\n" +
                "Введите число >> ");


        int encriptChoice;
        try{
            if (scanner.hasNextInt()) {
                encriptChoice = scanner.nextInt();
                scanner.nextLine();
                PasswordEncryptor passwordEncryptor = new PasswordEncryptor();

                switch (encriptChoice) {
                    case 1:
                        password.encryptedPassword = passwordEncryptor.encryptBase64(pass);
                        password.method = "Base64";
                        break;
                    case 2:
                        password.encryptedPassword = passwordEncryptor.encryptMD5(pass);
                        password.method = "MD5";
                        break;
                    case 3:
                        password.encryptedPassword = passwordEncryptor.encryptFeistel(pass);
                        password.method = "Fest";
                        break;

                    case 4:
                        password.encryptedPassword = passwordEncryptor.encryptWithSalt(pass);
                        password.method = "Salt";
                        break;

                }

                dbManager.addPassword(userId, password);

            } else {
                System.out.println("Пожалуйста вводите только цифры.");
                scanner.next();
            }

        } catch (Exception e){
            System.err.println("При шифровании пароля произошла ошибка: "+ e.getMessage());


        }
    }

    private void viewPasswords(int userId){
        try {
            Password[] passwords = dbManager.getPasswords(userId);
            PasswordEncryptor passwordEncryptor = new PasswordEncryptor();
            String decrypt_pass = "";
            if (passwords.length == 0) {
                System.out.println("\nНет сохраненных паролей.");
            } else {
                for (Password password : passwords) {

                    switch (password.method) {
                        case ("Base64"):
                            decrypt_pass = passwordEncryptor.decryptBase64(password.encryptedPassword);
                            System.out.printf("\nСервис: %s | Ваш пароль: %s%n", password.service, decrypt_pass);
                            break;

                        case ("MD5"):
                            System.out.printf("\nСервис: %s | Хэш пароля: %s%n", password.service, password.encryptedPassword);
                            break;

                        case ("Fest"):
                            decrypt_pass = passwordEncryptor.decryptFeistel(password.encryptedPassword);
                            System.out.printf("\nСервис: %s | Ваш пароль (fest): %s%n", password.service, decrypt_pass);
                            break;

                        case ("Salt"):
                            System.out.printf("\nСервис: %s | Хэш пароля: %s%n", password.service, password.encryptedPassword);
                            break;


                    }


                }
            }
        }catch (Exception e){
            System.out.println("Ошибка:  "+ e.getMessage());
        }

    }

    private void deletePassword(Scanner scanner,int userId){ //удаляет, если пароль есть, но ничего не происходит, если сервиса такого не нашлось
        System.out.print("\nВведите название сервиса для удаления: ");
        String serviceToDelete=scanner.nextLine();

        dbManager.deletePassword(userId,serviceToDelete);
        System.out.println("\nГотово.");
    }

    public static void main(String[] args){
        PasswordManagerApp app=new PasswordManagerApp("jdbc:sqlite:passwords.db");
        app.run();
    }
}


