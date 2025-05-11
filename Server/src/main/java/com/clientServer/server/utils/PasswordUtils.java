package com.clientServer.server.utils;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    public static String hashPassword(String password) {
        String randomString = BCrypt.gensalt();
        return BCrypt.hashpw(password, randomString);
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public static boolean isHashed(String password) {
        return password != null && password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}