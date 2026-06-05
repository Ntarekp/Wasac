package rw.gov.wasac.ubsystem.user;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.wasac.ubsystem.enums.EStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByStatus(EStatus status);
}