package ml.echelon133.microblogauth.token.service;

import ml.echelon133.microblogauth.token.model.AccessToken;
import ml.echelon133.microblogauth.token.model.RefreshToken;
import ml.echelon133.microblogauth.token.model.TokenPair;
import ml.echelon133.microblogauth.user.User;

import javax.servlet.http.Cookie;

public interface ITokenService {
    TokenPair generateTokenPairForUser(User user);
    AccessToken renewAccessToken(String token) throws IllegalArgumentException;
    Cookie buildRefreshTokenCookie(RefreshToken refreshToken);
    Cookie buildAccessTokenCookie(AccessToken accessToken);
}
