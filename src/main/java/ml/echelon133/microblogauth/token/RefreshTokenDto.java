package ml.echelon133.microblogauth.token;

import javax.validation.constraints.NotNull;

public class RefreshTokenDto {

    @NotNull
    private String refreshToken;

    public RefreshTokenDto() {}
    public RefreshTokenDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
