package ml.echelon133.microblogauth.token.model;

import ml.echelon133.microblogauth.token.model.AccessToken;
import ml.echelon133.microblogauth.token.model.RefreshToken;

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
