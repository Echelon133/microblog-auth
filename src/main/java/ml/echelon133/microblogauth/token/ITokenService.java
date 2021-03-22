package ml.echelon133.microblogauth.token;

import ml.echelon133.microblogauth.user.User;

public interface ITokenService {
    TokenPair generateTokenPairForUser(User user);
    AccessToken renewAccessToken(String token) throws IllegalArgumentException;
}
