package rw.gov.wasac.ubsystem.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "Full names are required")
    private String fullNames;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+2507\\d{8}$", message = "Phone number must be in format +2507XXXXXXXX")
    private String phoneNumber;
}
