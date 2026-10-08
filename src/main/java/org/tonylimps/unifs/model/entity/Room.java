package org.tonylimps.unifs.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "rooms")
public class Room {
    @TableId(type = IdType.ASSIGN_ID)
    Long id;
    String name;
    String password;
    String authEmail;
    LocalDateTime createTime;
    LocalDateTime lastUsedTime;
}
