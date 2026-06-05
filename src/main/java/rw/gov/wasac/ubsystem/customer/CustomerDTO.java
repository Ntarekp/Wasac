package rw.gov.wasac.ubsystem.customer;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerDTO {

    @NotBlank(message = "Full names are required")
    private String fullNames;

    @NotBlank(message = "National ID is required")
    @Size(min = 16, max = 16, message = "National ID must be 16 characters")
    @Pattern(regexp = "^[0-9]{16}$", message = "National ID must be exactly 16 numeric digits")
    private String nationalId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;
}