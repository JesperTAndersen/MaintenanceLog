package app.utils;

import app.exceptions.ApiException;

import java.util.regex.Pattern;

public final class ValidationUtil
{
    private ValidationUtil()
    {
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\s-]{6,20}$");

    public static void lengthBetween(String value, String fieldName, int min, int max)
    {
        if (value == null) return;

        int length = value.trim().length();
        if (length < min || length > max)
        {
            throw new ApiException(400, String.format("%s must be between %d and %d characters", fieldName, min, max));
        }
    }

    public static void matches(String value, Pattern pattern, String errorMessage)
    {
        if (value == null) return;
        if (!pattern.matcher(value.trim()).matches())
        {
            throw new ApiException(400, errorMessage);
        }
    }

    public static void validateEmailNonNull(String email)
    {
        matches(email, EMAIL_PATTERN, "Invalid email format");
        if (email.trim().length() > 254)
        {
            throw new ApiException(400, "Email is too long");
        }
    }

    public static void validatePhoneNonNull(String phone)
    {
        matches(phone, PHONE_PATTERN, "Invalid phone format");
    }

    public static void validatePasswordNonNull(String password)
    {
        lengthBetween(password, "Password", 4, 72);
    }
}