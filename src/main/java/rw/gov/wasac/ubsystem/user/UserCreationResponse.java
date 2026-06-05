package rw.gov.wasac.ubsystem.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreationResponse {
    private User user;
    private boolean temporaryPasswordEmailed;
    private String message;
}
