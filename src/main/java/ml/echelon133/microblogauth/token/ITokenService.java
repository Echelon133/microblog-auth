package ml.echelon133.microblogauth.token;

import ml.echelon133.microblogauth.user.User;

import javax.servlet.http.Cookie;

public interface ITokenService {
    TokenPair generateTokenPairForUser(User user);
    AccessToken renewAccessToken(String token) throws IllegalArgumentException;
    Cookie buildRefreshTokenCookie(RefreshToken refreshToken);
    Cookie buildAccessTokenCookie(AccessToken accessToken);
}
