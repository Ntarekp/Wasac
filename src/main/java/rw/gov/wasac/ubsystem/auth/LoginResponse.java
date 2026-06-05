package rw.gov.wasac.ubsystem.auth;

import lombok.*;
import rw.gov.wasac.ubsystem.enums.ERole;

import java.util.UUID;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class LoginResponse {
    private UUID userId;
    private String fullNames;
    private String email;
    private ERole role;
    private String token;
    private String tokenType = "Bearer";
}