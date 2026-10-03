package org.tonylimps.unifs.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users_rooms")
public class UserRoom {
    Long userId;
    Long roomId;
    Integer roomAdmin;
    LocalDateTime joinTime;
}
