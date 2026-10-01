package org.tonylimps.unifs.model.dto.request;

import lombok.Data;

@Data
public class LoginFinishRequest {
    Long id;
    String signature;
}
