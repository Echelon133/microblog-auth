package ml.echelon133.microblogauth.token.service;

import ml.echelon133.microblogauth.token.model.AccessToken;
import ml.echelon133.microblogauth.token.model.RefreshToken;
import ml.echelon133.microblogauth.token.model.TokenPair;
import ml.echelon133.microblogauth.user.User;

import javax.servlet.http.Cookie;

public interface ITokenService {
    TokenPair generateTokenPairForUser(User user);
    AccessToken renewAccessToken(String token) throws IllegalArgumentException;

    static Cookie buildRefreshTokenCookie(RefreshToken refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken",
                refreshToken.getToken());
        refreshTokenCookie.setMaxAge((int) refreshToken.getExpiration());
        refreshTokenCookie.setPath("/api/token/renew");
        return refreshTokenCookie;
    }

    static Cookie buildAccessTokenCookie(AccessToken accessToken) {
        Cookie accessTokenCookie = new Cookie("accessToken",
                accessToken.getToken());
        accessTokenCookie.setMaxAge((int) accessToken.getExpiration());
        accessTokenCookie.setPath("/");
        return accessTokenCookie;
    }
}
