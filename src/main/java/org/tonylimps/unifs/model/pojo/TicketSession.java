package org.tonylimps.unifs.model.pojo;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class TicketSession {
    Long userId;
    ZonedDateTime expireTime;
}
