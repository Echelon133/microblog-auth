package ml.echelon133.microblogauth.token;

import ml.echelon133.microblogauth.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.Optional;

@Service
public class TokenService implements ITokenService {

    private AccessTokenRepository accessTokenRepository;
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public TokenService(AccessTokenRepository accessTokenRepository, RefreshTokenRepository refreshTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public TokenPair generateTokenPairForUser(User user) {
        Optional<RefreshToken> refreshToken;
        Optional<AccessToken> accessToken;

        // the user can only have one refresh token
        // so check if there is one in the db
        refreshToken = refreshTokenRepository.findByOwnerUsername(user.getUsername());

        // if that specific user doesn't have a refresh token
        // create a new one and save it
        if (refreshToken.isEmpty()) {
            RefreshToken newRefreshToken = new RefreshToken(
                    user.getUuid(),
                    user.getUsername(),
                    user.getAuthorities()
            );
            refreshToken = Optional.of(refreshTokenRepository.save(newRefreshToken));
        } else {
            // If the refresh token already exists, reload it using findById
            // because findByOwnerUsername query seems to ignore @TimeToLive
            // field and doesn't set it to correct TTL value
            refreshToken = refreshTokenRepository.findById(refreshToken.get().getToken());
        }

        // user can have multiple working access tokens
        // always generate a new access token when the user logs in
        AccessToken newAccessToken = new AccessToken(
                user.getUuid(),
                user.getUsername(),
                user.getAuthorities()
        );

        accessToken = Optional.of(accessTokenRepository.save(newAccessToken));

        return new TokenPair(
                refreshToken.get(),
                accessToken.get()
        );
    }

    @Override
    public AccessToken renewAccessToken(String token) throws IllegalArgumentException {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(token);

        if (refreshToken.isPresent()) {
            AccessToken newAccessToken = new AccessToken(
                    refreshToken.get().getOwnerUuid(),
                    refreshToken.get().getOwnerUsername(),
                    refreshToken.get().getRoles()
            );
            return accessTokenRepository.save(newAccessToken);
        }
        throw new IllegalArgumentException("Refresh token invalid");
    }

    @Override
    public Cookie buildRefreshTokenCookie(RefreshToken refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken",
                refreshToken.getToken());
        refreshTokenCookie.setMaxAge((int) refreshToken.getExpiration());
        refreshTokenCookie.setPath("/api/token/renew");
        return refreshTokenCookie;
    }

    @Override
    public Cookie buildAccessTokenCookie(AccessToken accessToken) {
        Cookie accessTokenCookie = new Cookie("accessToken",
                accessToken.getToken());
        accessTokenCookie.setMaxAge((int) accessToken.getExpiration());
        accessTokenCookie.setPath("/api");
        return accessTokenCookie;
    }
}
