package org.tonylimps.unifs.model.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthSession {
    String name;
    String publicKey;
    String challenge;
    LocalDateTime expireTime;
}
