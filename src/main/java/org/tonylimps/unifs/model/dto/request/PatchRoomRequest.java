package org.tonylimps.unifs.model.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatchRoomRequest {
    @Size(min = 4, max=32)
    String name;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{8,}$")
    String password;
    @Pattern(regexp = "^(?:[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})?$")
    String authEmail;
}
