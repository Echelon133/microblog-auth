package ml.echelon133.microblogauth.token;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.UUID;

// access tokens should expire after 60 minutes
@RedisHash(value="accessToken", timeToLive = 3600)
public class AccessToken implements Serializable {

    private final int ACCESS_TOKEN_LENGTH = 64;

    @Id
    private String token;
    private UUID ownerUuid;

    @Indexed
    private String ownerUsername;

    public AccessToken(UUID ownerUuid, String ownerUsername) {
        this.token = TokenGenerator.generateToken(ACCESS_TOKEN_LENGTH);
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
