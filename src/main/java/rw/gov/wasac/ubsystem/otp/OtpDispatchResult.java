package rw.gov.wasac.ubsystem.otp;

import lombok.Builder;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;

@Data
@Builder
public class OtpDispatchResult {
    private String email;
    private EOtpPurpose purpose;
    private boolean newlyIssued;
    private long otpExpiresInSeconds;
    private long resendAvailableInSeconds;
    private String message;
}
