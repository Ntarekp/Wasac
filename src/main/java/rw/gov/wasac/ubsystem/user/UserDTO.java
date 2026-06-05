package rw.gov.wasac.ubsystem.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.ERole;

import java.util.UUID;

@Data
public class UserDTO {

    @NotBlank(message = "Full names are required")
    private String fullNames;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    /** Optional — a temporary password is generated and emailed when omitted (required for staff roles). */
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private ERole role;

    /** Required when role is ROLE_CUSTOMER — links user to an existing customer profile. */
    private UUID customerId;
}