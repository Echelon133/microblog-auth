package ml.echelon133.microblogauth.token.repository;

import ml.echelon133.microblogauth.token.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByOwnerUsername(String username);
}
