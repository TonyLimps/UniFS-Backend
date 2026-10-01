package org.tonylimps.unifs.model.dto.request;

import lombok.Data;

@Data
public class PatchUserRequest {
    String name;
    String publicKey;
}
