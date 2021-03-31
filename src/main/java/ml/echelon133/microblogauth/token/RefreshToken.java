package ml.echelon133.microblogauth.token;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// refresh tokens should expire after 30 days
@RedisHash(value="refreshToken", timeToLive = 2592000)
public class RefreshToken implements Serializable {

    private static final int REFRESH_TOKEN_LENGTH = 80;

    @Id
    private String token;
    private UUID ownerUuid;
    private List<String> roles;

    @Indexed
    private String ownerUsername;

    public RefreshToken() {}
    public RefreshToken(UUID ownerUuid, String ownerUsername, Collection<? extends GrantedAuthority> auth) {
        this.token = TokenGenerator.generateToken(REFRESH_TOKEN_LENGTH);
        this.ownerUuid = ownerUuid;
        this.ownerUsername = ownerUsername;
        this.roles = auth
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
