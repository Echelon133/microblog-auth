package ml.echelon133.microblogauth.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.echelon133.microblogauth.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
public class TokenControllerTests {

    static User testUser;

    private MockMvc mockMvc;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private TokenController tokenController;

    @InjectMocks
    private TokenExceptionHandler tokenExceptionHandler;

    private JacksonTester<TokenPair> jsonTokenPair;

    private JacksonTester<RefreshTokenDto> jsonRefreshTokenDto;

    private JacksonTester<Map<String, String>> jsonMapResponse;

    @BeforeAll
    public static void beforeAll() {
        testUser = new User("testUser", "", "", "");
    }

    @BeforeEach
    public void beforeEach() {
        JacksonTester.initFields(this, new ObjectMapper());

        SecurityContextPersistenceFilter filter;
        filter = new SecurityContextPersistenceFilter();
        mockMvc = MockMvcBuilders
                .standaloneSetup(tokenController)
                .setControllerAdvice(tokenExceptionHandler)
                .addFilter(filter)
                .build();
    }

    @Test
    public void generateTokens_GeneratesTokenPair() throws Exception {
        TokenPair pair = new TokenPair(
                new RefreshToken(),
                new AccessToken());
        JsonContent<TokenPair> jsonContent = jsonTokenPair.write(pair);

        // given
        given(tokenService.generateTokenPairForUser(testUser)).willReturn(pair);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonContent.getJson());
    }

    @Test
    public void generateTokens_SetsCookies() throws Exception {
        UUID ownerUuid = UUID.randomUUID();
        String ownerUsername = "test";

        TokenPair pair = new TokenPair(
                new RefreshToken(ownerUuid, ownerUsername, List.of((GrantedAuthority) () -> "ROLE_USER")),
                new AccessToken(ownerUuid, ownerUsername, List.of((GrantedAuthority) () -> "ROLE_USER")));

        Map<String, String> res = Map.of("accessToken", pair.getAccessToken().getToken(),
                "refreshToken", pair.getRefreshToken().getToken());

        JsonContent<Map<String, String>> jsonContent = jsonMapResponse.write(res);

        // given
        given(tokenService.generateTokenPairForUser(testUser)).willReturn(pair);
        given(tokenService.buildAccessTokenCookie(any())).willCallRealMethod();
        given(tokenService.buildRefreshTokenCookie(any())).willCallRealMethod();

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        Cookie rTokenCookie = response.getCookie("refreshToken");
        Cookie aTokenCookie = response.getCookie("accessToken");

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonContent.getJson());
        assertEquals(pair.getRefreshToken().getToken(), rTokenCookie.getValue());
        assertEquals(RefreshToken.REFRESH_TOKEN_TTL, rTokenCookie.getMaxAge());
        assertEquals("/api/token/renew", rTokenCookie.getPath());

        assertEquals(pair.getAccessToken().getToken(), aTokenCookie.getValue());
        assertEquals(AccessToken.ACCESS_TOKEN_TTL, aTokenCookie.getMaxAge());
    }

    @Test
    public void renewAccessToken_ThrowsWhenTokenNull() throws Exception {
        RefreshTokenDto dto = new RefreshTokenDto(null);
        JsonContent<RefreshTokenDto> jsonContent = jsonRefreshTokenDto.write(dto);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Refresh token required");
    }

    @Test
    public void renewAccessToken_ThrowsWhenTokenInvalid() throws Exception {
        RefreshTokenDto dto = new RefreshTokenDto("aaaa");
        JsonContent<RefreshTokenDto> jsonContent = jsonRefreshTokenDto.write(dto);

        // given
        given(tokenService.renewAccessToken("aaaa"))
                .willThrow(new IllegalArgumentException("Refresh token invalid"));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Refresh token invalid");
    }

    @Test
    public void renewAccessToken_ResponseCorrectWhenRenewingWorks() throws Exception {
        AccessToken accessToken = new AccessToken(testUser.getUuid(), testUser.getUsername());
        RefreshTokenDto dto = new RefreshTokenDto("aaaa");

        JsonContent<RefreshTokenDto> refreshTokenContent = jsonRefreshTokenDto.write(dto);
        JsonContent<Map<String, String>> accessTokenContent = jsonMapResponse
                .write(Map.of("accessToken", accessToken.getToken()));

        // given
        given(tokenService.renewAccessToken("aaaa")).willReturn(accessToken);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(refreshTokenContent.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(accessTokenContent.getJson());
    }


}
