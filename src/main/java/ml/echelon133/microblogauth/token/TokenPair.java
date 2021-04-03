package ml.echelon133.microblogauth.token;

public class TokenPair {
    private final RefreshToken refreshToken;
    private final AccessToken accessToken;

    public TokenPair(RefreshToken refreshToken, AccessToken accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }
}
