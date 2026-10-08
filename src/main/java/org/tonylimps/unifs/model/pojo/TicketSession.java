package org.tonylimps.unifs.model.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketSession {
    Long userId;
    LocalDateTime expireTime;
}
