package ml.echelon133.microblogauth.token;

import ml.echelon133.microblogauth.token.*;
import ml.echelon133.microblogauth.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        // when
        TokenPair pair = tokenService.generateTokenPairForUser(testUser);

        // then
        assertEquals(refreshToken.get().getToken(), pair.getRefreshToken());
        assertEquals(unwrappedAccessToken.getToken(), pair.getAccessToken());
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
        assertEquals(refreshToken.get().getToken(), pair.getRefreshToken());
        assertEquals(unwrappedAccessToken.getToken(), pair.getAccessToken());
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
}
