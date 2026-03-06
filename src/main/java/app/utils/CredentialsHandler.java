package app.utils;

import org.mindrot.jbcrypt.BCrypt;

public class CredentialsHandler
{

    public static String hashPassword(String password)
    {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
