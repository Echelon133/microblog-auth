package ml.echelon133.microblogauth.token.service;

import ml.echelon133.microblogauth.token.model.AccessToken;
import ml.echelon133.microblogauth.token.model.RefreshToken;
import ml.echelon133.microblogauth.token.model.TokenPair;
import ml.echelon133.microblogauth.token.repository.AccessTokenRepository;
import ml.echelon133.microblogauth.token.repository.RefreshTokenRepository;
import ml.echelon133.microblogauth.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTests {

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private static User testUser;

    @BeforeAll
    public static void beforeAll() {
        testUser = new User("testUser", "", "", "");
    }

    @Test
    public void generateTokenPairForUser_CorrectFlowWhenRefreshTokenExists() {
        Optional<RefreshToken> refreshToken = Optional.of(
                new RefreshToken(testUser.getUuid(), testUser.getUsername(), testUser.getAuthorities())
        );
        AccessToken unwrappedAccessToken = new AccessToken(testUser.getUuid(), testUser.getUsername(), testUser.getAuthorities());

        // given
        given(refreshTokenRepository.findByOwnerUsername(testUser.getUsername()))
                .willReturn(refreshToken);
        given(accessTokenRepository.save(any())).willReturn(unwrappedAccessToken);
        given(refreshTokenRepository.findById(refreshToken.get().getToken()))
                .willReturn(refreshToken);

        // when
        TokenPair pair = tokenService.generateTokenPairForUser(testUser);

        // then
        assertEquals(refreshToken.get().getToken(), pair.getRefreshToken().getToken());
        assertEquals(unwrappedAccessToken.getToken(), pair.getAccessToken().getToken());
    }

    @Test
    public void generateTokenPairForUser_CorrectFlowWhenRefreshTokenDoesNotExist() {
        RefreshToken unwrappedRefreshToken = new RefreshToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());
        AccessToken unwrappedAccessToken = new AccessToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());

        Optional<RefreshToken> refreshToken = Optional.of(
            unwrappedRefreshToken
        );


        // given
        given(refreshTokenRepository.findByOwnerUsername(testUser.getUsername()))
                .willReturn(Optional.empty());
        given(refreshTokenRepository.save(any())).willReturn(unwrappedRefreshToken);
        given(accessTokenRepository.save(any())).willReturn(unwrappedAccessToken);

        // when
        TokenPair pair = tokenService.generateTokenPairForUser(testUser);

        // then
        assertEquals(refreshToken.get().getToken(), pair.getRefreshToken().getToken());
        assertEquals(unwrappedAccessToken.getToken(), pair.getAccessToken().getToken());
    }

    @Test
    public void renewAccessToken_CorrectFlowWhenTokenExists() {
        RefreshToken refreshToken = new RefreshToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());
        AccessToken newAccessToken = new AccessToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());

        // given
        given(refreshTokenRepository.findById(refreshToken.getToken()))
                .willReturn(Optional.of(refreshToken));
        given(accessTokenRepository.save(any())).willReturn(newAccessToken);

        // when
        AccessToken accessToken = tokenService.renewAccessToken(refreshToken.getToken());

        // then
        assertEquals(newAccessToken.getToken(), accessToken.getToken());
    }

    @Test
    public void renewAccessToken_CorrectFlowWhenTokenDoesNotExist() {
        RefreshToken refreshToken = new RefreshToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());

        // given
        given(refreshTokenRepository.findById(refreshToken.getToken()))
                .willReturn(Optional.empty());

        // when
        String msg = assertThrows(IllegalArgumentException.class, () -> {
            tokenService.renewAccessToken(refreshToken.getToken());
        }).getMessage();

        // then
        assertEquals("Refresh token invalid", msg);
    }

    @Test
    public void buildRefreshTokenCookie_SetsCorrectMaxAgeAndPathForNewToken() {
        RefreshToken refreshToken = new RefreshToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());

        // when
        Cookie refreshTokenCookie = ITokenService.buildRefreshTokenCookie(refreshToken);

        // then
        assertEquals("refreshToken", refreshTokenCookie.getName());
        assertEquals(RefreshToken.REFRESH_TOKEN_LENGTH, refreshTokenCookie.getValue().length());
        assertEquals(RefreshToken.REFRESH_TOKEN_TTL, refreshTokenCookie.getMaxAge());
        assertEquals("/api/token/renew", refreshTokenCookie.getPath());
    }

    @Test
    public void buildRefreshTokenCookie_SetsCorrectMaxAgeAndPathForOlderToken() {
        long ttl = 1000;

        RefreshToken refreshToken = new RefreshToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());
        refreshToken.setExpiration(ttl);

        // when
        Cookie refreshTokenCookie = ITokenService.buildRefreshTokenCookie(refreshToken);

        // then
        assertEquals("refreshToken", refreshTokenCookie.getName());
        assertEquals(RefreshToken.REFRESH_TOKEN_LENGTH, refreshTokenCookie.getValue().length());
        assertEquals(ttl, refreshTokenCookie.getMaxAge());
        assertEquals("/api/token/renew", refreshTokenCookie.getPath());
    }

    @Test
    public void buildAccessTokenCookie_SetsCorrectMaxAgeForNewToken() {
        AccessToken accessToken = new AccessToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());

        // when
        Cookie accessTokenCookie = ITokenService.buildAccessTokenCookie(accessToken);

        // then
        assertEquals("accessToken", accessTokenCookie.getName());
        assertEquals(AccessToken.ACCESS_TOKEN_LENGTH, accessTokenCookie.getValue().length());
        assertEquals(AccessToken.ACCESS_TOKEN_TTL, accessTokenCookie.getMaxAge());
    }

    @Test
    public void buildAccessTokenCookie_SetsCorrectMaxAgeAndPathForOlderToken() {
        long ttl = 1000;

        AccessToken accessToken = new AccessToken(testUser.getUuid(),
                testUser.getUsername(), testUser.getAuthorities());
        accessToken.setExpiration(ttl);

        // when
        Cookie accessTokenCookie = ITokenService.buildAccessTokenCookie(accessToken);

        // then
        assertEquals("accessToken", accessTokenCookie.getName());
        assertEquals(AccessToken.ACCESS_TOKEN_LENGTH, accessTokenCookie.getValue().length());
        assertEquals(ttl, accessTokenCookie.getMaxAge());
        assertEquals("/api", accessTokenCookie.getPath());
    }
}
