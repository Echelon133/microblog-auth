package ml.echelon133.microblogauth.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.typeconversion.UuidStringConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@NodeEntity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    @Convert(UuidStringConverter.class)
    private UUID uuid;
    private String username;
    private String displayedUsername;
    private String email;
    private String description;
    private String password;
    private String aviURL;
    private Date creationDate;


    public User() {}
    public User(String username, String email, String password, String aviURL) {
        this();
        this.username = username;
        this.displayedUsername = username;
        this.email = email;
        this.password = password;
        this.aviURL = aviURL;
        this.description = "";
        this.creationDate = new Date();
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> "ROLE_USER");
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    public String getDescription() {
        return description;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDisplayedUsername() {
        return displayedUsername;
    }

    @JsonIgnore
    public String getEmail() {
        return email;
    }

    public String getAviURL() {
        return aviURL;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDisplayedUsername(String displayedUsername) {
        this.displayedUsername = displayedUsername;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAviURL(String aviURL) {
        this.aviURL = aviURL;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}