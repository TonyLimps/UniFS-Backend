package org.tonylimps.unifs.model.pojo;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AuthSession {
    String name;
    String publicKey;
    String challenge;
    ZonedDateTime expireTime;
}
