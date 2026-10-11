package org.tonylimps.unifs.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinRoomRequest {
    @NotNull @NotEmpty
    String password;
}
