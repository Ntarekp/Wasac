package rw.gov.wasac.ubsystem.auth;

import lombok.Builder;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;

@Data
@Builder
public class OtpPendingResponse {
    private boolean otpRequired;
    private String email;
    private EOtpPurpose purpose;
    private long otpExpiresInSeconds;
    private long resendAvailableInSeconds;
    private String message;
}
