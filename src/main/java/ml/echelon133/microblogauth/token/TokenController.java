package ml.echelon133.microblogauth.token;

import ml.echelon133.microblogauth.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public ResponseEntity<TokenPair> generateTokens() {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TokenPair pair = tokenService.generateTokenPairForUser(loggedUser);

        return new ResponseEntity<>(pair, HttpStatus.OK);
    }

    @PostMapping("/renew")
    public ResponseEntity<Map<String, String>> renewAccessToken(
            @Valid @RequestBody RefreshTokenDto refreshTokenDto,
            BindingResult result) {

        if (result.hasErrors()) {
            throw new IllegalArgumentException("Refresh token required");
        }

        AccessToken accessToken = tokenService.renewAccessToken(refreshTokenDto.getRefreshToken());
        Map<String, String> response = Map.of("accessToken", accessToken.getToken());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
