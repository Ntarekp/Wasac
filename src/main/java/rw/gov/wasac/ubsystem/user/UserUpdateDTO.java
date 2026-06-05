package rw.gov.wasac.ubsystem.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class UserUpdateDTO {

    @NotBlank(message = "Full names are required")
    private String fullNames;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+2507\\d{8}$", message = "Phone number must be in format +2507XXXXXXXX")
    private String phoneNumber;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /** Optional — relink ROLE_CUSTOMER user to a customer profile. */
    private UUID customerId;
}
