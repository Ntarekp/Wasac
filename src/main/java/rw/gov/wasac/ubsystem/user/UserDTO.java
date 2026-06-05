package rw.gov.wasac.ubsystem.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.ERole;

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

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private ERole role;
}