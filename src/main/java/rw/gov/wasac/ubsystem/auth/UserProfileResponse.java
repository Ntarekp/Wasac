package rw.gov.wasac.ubsystem.auth;

import lombok.Builder;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.enums.EStatus;

import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID userId;
    private String fullNames;
    private String email;
    private String phoneNumber;
    private ERole role;
    private EStatus status;
    private UUID customerId;
    private boolean mustChangePassword;
    private boolean emailVerified;
}
