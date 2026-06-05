package rw.gov.wasac.ubsystem.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordResponse {
    private String email;
    private String message;
}
