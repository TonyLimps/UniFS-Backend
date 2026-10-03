package org.tonylimps.unifs.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value="users")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    Long id;
    String name;
    Integer serverAdmin;
    String publicKey;
    LocalDateTime registerTime;
    LocalDateTime lastLoginTime;
}