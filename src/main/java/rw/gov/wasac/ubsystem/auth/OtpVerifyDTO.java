package rw.gov.wasac.ubsystem.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;

@Data
public class OtpVerifyDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit code")
    private String code;

    @NotNull(message = "OTP purpose is required")
    private EOtpPurpose purpose;
}
