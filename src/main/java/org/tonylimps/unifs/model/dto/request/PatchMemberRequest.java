package org.tonylimps.unifs.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PatchMemberRequest {
    @Min(0) @Max(1)
    Integer roomAdmin;
}
