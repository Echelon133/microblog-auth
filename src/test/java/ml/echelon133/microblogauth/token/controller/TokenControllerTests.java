package ml.echelon133.microblogauth.token.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.echelon133.microblogauth.token.controller.TokenController;
import ml.echelon133.microblogauth.token.exception.TokenExceptionHandler;
import ml.echelon133.microblogauth.token.model.AccessToken;
import ml.echelon133.microblogauth.token.model.RefreshToken;
import ml.echelon133.microblogauth.token.model.RefreshTokenDto;
import ml.echelon133.microblogauth.token.model.TokenPair;
import ml.echelon133.microblogauth.token.service.ITokenService;
import ml.echelon133.microblogauth.token.service.TokenService;
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
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.Cookie;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private AccessToken buildTestAccessToken() {
        return new AccessToken(testUser.getUuid(), testUser.getUsername(), testUser.getAuthorities());
    }

    private RefreshToken buildTestRefreshToken() {
        return new RefreshToken(testUser.getUuid(), testUser.getUsername(), testUser.getAuthorities());
    }

    @Test
    public void generateTokens_GeneratesTokenPair() throws Exception {
        TokenPair pair = new TokenPair(
                buildTestRefreshToken(),
                buildTestAccessToken()
        );

        Map<String, String> res = Map.of(
                "accessToken", pair.getAccessToken().getToken(),
                "refreshToken", pair.getRefreshToken().getToken()
        );
        JsonContent<Map<String, String>> jsonContent = jsonMapResponse.write(res);

        // given
        given(tokenService.generateTokenPairForUser(testUser)).willReturn(pair);
        given(ITokenService.buildRefreshTokenCookie(any())).willCallRealMethod();
        given(ITokenService.buildAccessTokenCookie(any())).willCallRealMethod();

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
        TokenPair pair = new TokenPair(
                buildTestRefreshToken(),
                buildTestAccessToken()
        );

        Map<String, String> res = Map.of(
                "accessToken", pair.getAccessToken().getToken(),
                "refreshToken", pair.getRefreshToken().getToken()
        );
        JsonContent<Map<String, String>> jsonContent = jsonMapResponse.write(res);

        // given
        given(tokenService.generateTokenPairForUser(testUser)).willReturn(pair);
        given(ITokenService.buildAccessTokenCookie(any())).willCallRealMethod();
        given(ITokenService.buildRefreshTokenCookie(any())).willCallRealMethod();

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
        assertEquals("/api", aTokenCookie.getPath());
    }

    @Test
    public void renewAccessToken_ThrowsWhenTokenNotProvided() throws Exception {

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content("{}")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Refresh token required");
    }

    @Test
    public void renewAccessToken_WorksWhenCorrectCookiePresent() throws Exception {
        AccessToken accessToken = buildTestAccessToken();
        JsonContent<Map<String, String>> accessTokenContent = jsonMapResponse
                .write(Map.of("accessToken", accessToken.getToken()));

        Cookie refreshToken = new Cookie("refreshToken", "aaaa");

        // given
        given(tokenService.renewAccessToken(refreshToken.getValue()))
                .willReturn(accessToken);
        given(ITokenService.buildAccessTokenCookie(accessToken)).willCallRealMethod();

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .cookie(refreshToken)
                        .contentType(APPLICATION_JSON)
                        .content("{}")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(accessTokenContent.getJson());
    }

    @Test
    public void renewAccessToken_TokenFromCookieIsPrioritized() throws Exception {
        String bodyRefreshToken = "{\"refreshToken\":\"bbbb\"}";

        AccessToken accessToken = buildTestAccessToken();
        JsonContent<Map<String, String>> accessTokenContent = jsonMapResponse
                .write(Map.of("accessToken", accessToken.getToken()));

        Cookie refreshToken = new Cookie("refreshToken", "aaaa");

        // given
        given(tokenService.renewAccessToken(refreshToken.getValue()))
                .willReturn(accessToken);
        given(ITokenService.buildAccessTokenCookie(accessToken)).willCallRealMethod();

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .cookie(refreshToken)
                        .contentType(APPLICATION_JSON)
                        .content(bodyRefreshToken)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(accessTokenContent.getJson());
    }

    @Test
    public void renewAccessToken_ThrowsWhenTokenInvalid() throws Exception {
        RefreshTokenDto dto = new RefreshTokenDto("aaaa");
        JsonContent<RefreshTokenDto> jsonContent = jsonRefreshTokenDto.write(dto);

        // given
        given(tokenService.renewAccessToken(dto.getRefreshToken()))
                .willThrow(new IllegalArgumentException("Refresh token invalid"));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Refresh token invalid");
    }

    @Test
    public void renewAccessToken_RefreshTokenTakenFromRequestBodyWhenCookieNotPresent() throws Exception {
        AccessToken accessToken = buildTestAccessToken();
        RefreshTokenDto dto = new RefreshTokenDto("aaaa");

        JsonContent<RefreshTokenDto> refreshTokenContent = jsonRefreshTokenDto.write(dto);
        JsonContent<Map<String, String>> accessTokenContent = jsonMapResponse
                .write(Map.of("accessToken", accessToken.getToken()));

        // given
        given(tokenService.renewAccessToken(dto.getRefreshToken()))
                .willReturn(accessToken);
        given(ITokenService.buildAccessTokenCookie(accessToken)).willCallRealMethod();

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(refreshTokenContent.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(accessTokenContent.getJson());
    }

    @Test
    public void renewAccessToken_SetsAccessTokenCookie() throws Exception {
        AccessToken accessToken = buildTestAccessToken();
        JsonContent<Map<String, String>> accessTokenContent = jsonMapResponse
                .write(Map.of("accessToken", accessToken.getToken()));

        Cookie refreshToken = new Cookie("refreshToken", "aaaa");

        // given
        given(tokenService.renewAccessToken(refreshToken.getValue()))
                .willReturn(accessToken);
        given(ITokenService.buildAccessTokenCookie(accessToken)).willCallRealMethod();

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/renew")
                        .accept(APPLICATION_JSON)
                        .cookie(refreshToken)
                        .contentType(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        Cookie aTokenCookie = response.getCookie("accessToken");

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(accessTokenContent.getJson());
        assertEquals(accessToken.getToken(), aTokenCookie.getValue());
        assertEquals(AccessToken.ACCESS_TOKEN_TTL, aTokenCookie.getMaxAge());
        assertEquals("/api", aTokenCookie.getPath());
    }

    @Test
    public void clearTokens_ReturnsCorrectEmptyTokens() throws Exception {

        // given
        given(ITokenService.buildAccessTokenCookie(any())).willCallRealMethod();
        given(ITokenService.buildRefreshTokenCookie(any())).willCallRealMethod();

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/token/clearTokens")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        Cookie rTokenCookie = response.getCookie("refreshToken");
        Cookie aTokenCookie = response.getCookie("accessToken");

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertEquals(null, rTokenCookie.getValue());
        assertEquals(0, rTokenCookie.getMaxAge());
        assertEquals("/api/token/renew", rTokenCookie.getPath());

        assertEquals(null, aTokenCookie.getValue());
        assertEquals(0, aTokenCookie.getMaxAge());
        assertEquals("/api", aTokenCookie.getPath());
    }
}
