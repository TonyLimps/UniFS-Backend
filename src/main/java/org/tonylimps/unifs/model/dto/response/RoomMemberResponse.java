package org.tonylimps.unifs.model.dto.response;

import lombok.Data;

@Data
public class RoomMemberResponse {
    Long id;
    String name;
    Integer roomAdmin;
    Integer serverAdmin;
}
