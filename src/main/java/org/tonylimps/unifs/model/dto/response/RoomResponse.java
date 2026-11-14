package org.tonylimps.unifs.model.dto.response;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
public class RoomResponse {
    Long id;
    String name;
    String authEmail;
    ZonedDateTime createTime;
    Set<MemberResponse> members;
}
