package org.tonylimps.unifs.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    Long id;
    String name;
    LocalDateTime registerTime;
    LocalDateTime lastLoginTime;
}
