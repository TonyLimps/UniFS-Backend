package org.tonylimps.unifs.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RoomResponse {
    Long id;
    String name;
    String authEmail;
    LocalDateTime createTime;
    Set<MemberResponse> members;
}
