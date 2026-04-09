package app.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil
{
    private PasswordUtil()
    {
    }

    public static String hashPassword(String password)
    {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String inputtedPassword, String hashedPassword)
    {
        return BCrypt.checkpw(inputtedPassword, hashedPassword);
    }
}