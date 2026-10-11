package org.tonylimps.unifs.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotNull @Size(min=4, max=32)
    String name;
    @NotNull @NotEmpty
    String publicKey;
}
