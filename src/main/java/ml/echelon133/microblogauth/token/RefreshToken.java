package ml.echelon133.microblogauth.token;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.UUID;

// refresh tokens should expire after 30 days
@RedisHash(value="refreshToken", timeToLive = 2592000)
public class RefreshToken implements Serializable {

    private static final int REFRESH_TOKEN_LENGTH = 80;

    @Id
    private String token;
    private UUID ownerUuid;

    @Indexed
    private String ownerUsername;

    public RefreshToken(UUID ownerUuid, String ownerUsername) {
        this.token = TokenGenerator.generateToken(REFRESH_TOKEN_LENGTH);
        this.ownerUuid = ownerUuid;
        this.ownerUsername = ownerUsername;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
