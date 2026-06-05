package rw.gov.wasac.ubsystem.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {

    Optional<Otp> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, EOtpPurpose purpose);
}
