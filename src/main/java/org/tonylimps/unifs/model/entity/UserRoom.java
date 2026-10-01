package org.tonylimps.unifs.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@TableName("users_rooms")
public class UserRoom {
    @NotNull
    Long userId;
    @NotNull
    Long roomId;
}
