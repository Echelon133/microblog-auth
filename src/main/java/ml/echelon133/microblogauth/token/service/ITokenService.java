package ml.echelon133.microblogauth.token.service;

import ml.echelon133.microblogauth.token.AccessToken;
import ml.echelon133.microblogauth.token.RefreshToken;
import ml.echelon133.microblogauth.token.TokenPair;
import ml.echelon133.microblogauth.user.User;

import javax.servlet.http.Cookie;

public interface ITokenService {
    TokenPair generateTokenPairForUser(User user);
    AccessToken renewAccessToken(String token) throws IllegalArgumentException;
    Cookie buildRefreshTokenCookie(RefreshToken refreshToken);
    Cookie buildAccessTokenCookie(AccessToken accessToken);
}
