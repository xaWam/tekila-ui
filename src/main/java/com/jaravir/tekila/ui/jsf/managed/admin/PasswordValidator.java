package com.jaravir.tekila.ui.jsf.managed.admin;

/**
 * @author ElmarMa on 10/3/2018
 */
public class PasswordValidator {

    public static void validate(String rawPassword) {

        if (rawPassword == null || rawPassword.isEmpty())
            throw new IllegalArgumentException("password can not be empty");
        if (rawPassword.contains(" "))
            throw new IllegalArgumentException("password can not contain space character");

        checkLength(rawPassword);

        checkCapitalCharacter(rawPassword);

        checkLowerLetter(rawPassword);

        checkNumber(rawPassword);

        checkSpecialCharacter(rawPassword);


    }

    private static void checkCapitalCharacter(String rawPassword) {
        boolean found = false;
        for (int i = 0; i < rawPassword.length(); i++) {
            if (Character.isUpperCase(rawPassword.charAt(i))) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new IllegalArgumentException("password must contain at least one Capital letter");
    }

    private static void checkLowerLetter(String rawPassword) {
        boolean found = false;
        for (int i = 0; i < rawPassword.length(); i++) {
            if (Character.isLowerCase(rawPassword.charAt(i))) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new IllegalArgumentException("password must contain at least one Lower letter");
    }


    private static void checkNumber(String rawPassword) {
        boolean found = false;
        for (int i = 0; i < rawPassword.length(); i++) {
            if (Character.isDigit(rawPassword.charAt(i))) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new IllegalArgumentException("password must contain at least one Digit");
    }

    private static void checkSpecialCharacter(String rawPassword) {
        boolean found = false;
        for (int i = 0; i < rawPassword.length(); i++) {
            if (!Character.isLetterOrDigit(rawPassword.charAt(i))) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new IllegalArgumentException("password must contain at least one special character");
    }

    private static void checkLength(String rawPassword) {
        if (rawPassword.length() < 8)
            throw new IllegalArgumentException("password length must be at least 8 symbols");
    }

}
