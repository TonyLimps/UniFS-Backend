package org.tonylimps.unifs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.tonylimps.unifs.model.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM users WHERE public_key = #{publicKey};")
    User selectByPublicKey(String publicKey);
}
