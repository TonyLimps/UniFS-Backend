package org.tonylimps.unifs.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@TableName("users_rooms")
public class UserRoom {
    Long userId;
    Long roomId;
    Integer roomAdmin;
    ZonedDateTime joinTime;
}
