package ml.echelon133.microblogauth.token;

public class TokenPair {
    private final String refreshToken;
    private final String accessToken;

    public TokenPair(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
