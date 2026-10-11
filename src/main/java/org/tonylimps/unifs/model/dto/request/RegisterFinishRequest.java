package org.tonylimps.unifs.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterFinishRequest {
    @NotNull
    Long id;
    @NotNull
    String signature;
}
