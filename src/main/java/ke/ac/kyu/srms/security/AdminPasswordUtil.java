
package ke.ac.kyu.srms.security;

import org.mindrot.jbcrypt.BCrypt;

public final class AdminPasswordUtil {

    private static final int BCRYPT_ROUNDS = 12;

    private AdminPasswordUtil() {
        // Prevent instantiation.
    }

    /**
     * Generates a secure BCrypt password hash.
     */
    public static String hashPassword(char[] password) {

        if (password == null || password.length < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        String plainPassword = new String(password);

        return BCrypt.hashpw(
                plainPassword,
                BCrypt.gensalt(BCRYPT_ROUNDS)
        );
    }

    /**
     * Verifies a password against its stored BCrypt hash.
     */
    public static boolean verifyPassword(
            char[] password,
            String storedHash
    ) {

        if (password == null
                || password.length == 0
                || storedHash == null
                || storedHash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(
                    new String(password),
                    storedHash
            );
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
