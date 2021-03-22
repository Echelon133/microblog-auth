package ml.echelon133.microblogauth.token;

import java.security.SecureRandom;

public class TokenGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final char[] availableChars = new char[62];

    static {
        byte startUpper = (byte)'A';
        byte endUpper = (byte)'Z' + 1;

        byte startLower = (byte)'a';
        byte endLower = (byte)'z' + 1;

        byte startDigit = (byte)'0';
        byte endDigit = (byte)'9' + 1;

        int i = 0;
        // A to Z
        for (int j = startUpper; j < endUpper; j++) {
            availableChars[i] = (char)j;
            i++;
        }
        // a to z
        for (int j = startLower; j < endLower; j++) {
            availableChars[i] = (char)j;
            i++;
        }
        // 0 to 9
        for (int j = startDigit; j < endDigit; j++) {
            availableChars[i] = (char)j;
            i++;
        }
    }

    public static String generateToken(int tokenLength) {
        char[] token = new char[tokenLength];

        for (int i = 0; i < tokenLength; i++) {
            int index = random.nextInt(availableChars.length);
            token[i] = availableChars[index];
        }
        return String.valueOf(token);
    }
}
