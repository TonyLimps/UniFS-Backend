package org.tonylimps.unifs.model.dto.request;

import lombok.Data;

@Data
public class RoomRequest {
    String name;
    String password;
    String authEmail;
}
