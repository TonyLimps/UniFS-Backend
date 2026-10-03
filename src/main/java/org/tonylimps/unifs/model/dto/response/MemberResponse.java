package org.tonylimps.unifs.model.dto.response;

import lombok.Data;

@Data
public class MemberResponse {
    Long id;
    String name;
    Integer roomAdmin;
    Integer serverAdmin;
}
