package ml.echelon133.microblogauth.token;

import ml.echelon133.microblogauth.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private ITokenService tokenService;

    @Autowired
    public TokenController(ITokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> generateTokens(HttpServletResponse response) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TokenPair pair = tokenService.generateTokenPairForUser(loggedUser);

        response.addCookie(tokenService.buildRefreshTokenCookie(pair.getRefreshToken()));
        response.addCookie(tokenService.buildAccessTokenCookie(pair.getAccessToken()));

        return new ResponseEntity<>(Map.of(
                "refreshToken", pair.getRefreshToken().getToken(),
                "accessToken", pair.getAccessToken().getToken()
        ), HttpStatus.OK);
    }

    @PostMapping("/renew")
    public ResponseEntity<Map<String, String>> renewAccessToken(
            @CookieValue(name = "refreshToken", defaultValue = "") String cookieToken,
            @RequestBody(required = false) RefreshTokenDto refreshTokenDto) {

        String refreshToken = "";

        // take the refresh token either from the cookie or the body of the request
        if (cookieToken.equals("") && refreshTokenDto.getRefreshToken() == null) {
            throw new IllegalArgumentException("Refresh token required");
        } else {
            // if both body and cookie are set, cookies take precedence
            if (!cookieToken.equals("")) {
                refreshToken = cookieToken;
            } else {
                refreshToken = refreshTokenDto.getRefreshToken();
            }
        }

        AccessToken accessToken = tokenService.renewAccessToken(refreshToken);
        Map<String, String> response = Map.of("accessToken", accessToken.getToken());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
