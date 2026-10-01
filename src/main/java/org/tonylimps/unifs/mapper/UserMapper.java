package org.tonylimps.unifs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.tonylimps.unifs.model.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
