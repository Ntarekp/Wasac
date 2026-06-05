package rw.gov.wasac.ubsystem.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;

@Data
public class OtpRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "OTP purpose is required")
    private EOtpPurpose purpose;
}
