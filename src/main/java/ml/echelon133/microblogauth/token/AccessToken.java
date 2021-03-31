package ml.echelon133.microblogauth.token;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// access tokens should expire after 60 minutes
@RedisHash(value="accessToken", timeToLive = 3600)
public class AccessToken implements Serializable {

    private static final int ACCESS_TOKEN_LENGTH = 64;

    @Id
    private String token;
    private UUID ownerUuid;
    private List<String> roles;

    @Indexed
    private String ownerUsername;

    public AccessToken(UUID ownerUuid, String ownerUsername) {
        this.token = TokenGenerator.generateToken(ACCESS_TOKEN_LENGTH);
        this.ownerUuid = ownerUuid;
        this.ownerUsername = ownerUsername;
    }

    public AccessToken(UUID ownerUuid, String ownerUsername, Collection<? extends GrantedAuthority> auth) {
        this(ownerUuid, ownerUsername);
        this.roles = auth
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public AccessToken(UUID ownerUuid, String ownerUsername, List<String> roles) {
        this(ownerUuid, ownerUsername);
        this.roles = roles;
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
