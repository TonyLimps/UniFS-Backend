package org.tonylimps.unifs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.tonylimps.unifs.model.dto.response.MemberResponse;
import org.tonylimps.unifs.model.entity.Room;
import org.tonylimps.unifs.model.entity.UserRoom;

import java.util.Set;

@Mapper
public interface UserRoomMapper extends BaseMapper<UserRoom> {
    @Select("SELECT * FROM users_rooms WHERE room_id = #{roomId} AND user_id = #{userId};")
    UserRoom select(Long roomId, Long userId);

    @Select("SELECT * FROM users_rooms ur JOIN users u ON ur.user_id = u.id WHERE room_id = #{roomId};")
    Set<MemberResponse> selectMembersByRoomId(Long roomId);

    @Select("SELECT r.* FROM users_rooms ur JOIN rooms r ON ur.room_id = r.id WHERE user_id = #{userId};")
    Set<Room> selectRoomsByUserId(Long userId);

    @Delete("DELETE FROM users_rooms WHERE room_id = #{roomId} AND user_id = #{userId};")
    int delete(Long roomId, Long userId);

    @Delete("DELETE FROM users_rooms WHERE room_id = #{roomId};")
    int deleteByRoomId(Long roomId);

    @Select("SELECT user_id FROM users_rooms WHERE room_id in (SELECT room_id FROM users_rooms WHERE user_id = #{userId});")
    Set<Long> selectSameRoomUserIds(Long userId);
}
