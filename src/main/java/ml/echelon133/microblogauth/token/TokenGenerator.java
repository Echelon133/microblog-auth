package ml.echelon133.microblogauth.token;

import java.security.SecureRandom;

public class TokenGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final char[] availableChars = (
            "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789"
    ).toCharArray();

    public static String generateToken(int tokenLength) {
        char[] token = new char[tokenLength];

        for (int i = 0; i < tokenLength; i++) {
            int index = random.nextInt(availableChars.length);
            token[i] = availableChars[index];
        }
        return String.valueOf(token);
    }
}
