package org.tonylimps.unifs.model.dto.response;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class UserResponse {
    Long id;
    String name;
    Integer admin;
    ZonedDateTime registerTime;
    ZonedDateTime lastLoginTime;
}
