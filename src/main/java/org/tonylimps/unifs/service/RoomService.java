package org.tonylimps.unifs.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tonylimps.unifs.exception.BusinessException;
import org.tonylimps.unifs.exception.ErrorCode;
import org.tonylimps.unifs.mapper.RoomMapper;
import org.tonylimps.unifs.mapper.UserMapper;
import org.tonylimps.unifs.mapper.UserRoomMapper;
import org.tonylimps.unifs.model.dto.request.RoomRequest;
import org.tonylimps.unifs.model.dto.response.RoomMemberResponse;
import org.tonylimps.unifs.model.dto.response.RoomResponse;
import org.tonylimps.unifs.model.dto.response.UserResponse;
import org.tonylimps.unifs.model.entity.Room;
import org.tonylimps.unifs.model.entity.User;
import org.tonylimps.unifs.model.entity.UserRoom;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final UserMapper userMapper;
    private final RoomMapper roomMapper;
    private final UserRoomMapper userRoomMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RoomService(UserMapper userMapper,
                       RoomMapper roomMapper,
                       UserRoomMapper userRoomMapper,
                       PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roomMapper = roomMapper;
        this.userRoomMapper = userRoomMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public RoomResponse getRoomById(Authentication authentication, Long roomId) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(userId, roomId);
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (userRoom == null && !isAdmin) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }

        Set<RoomMemberResponse> members = userRoomMapper.selectMembersByRoomId(roomId);
        RoomResponse roomResponse = new RoomResponse();
        BeanUtils.copyProperties(room, roomResponse);
        roomResponse.setMembers(members);
        return roomResponse;
    }

    public RoomResponse createRoom(Authentication authentication, RoomRequest roomRequest) {
        Room room = new Room();
        BeanUtils.copyProperties(roomRequest, room);
        room.setPassword(passwordEncoder.encode(roomRequest.getPassword()));
        roomMapper.insert(room);

        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = new UserRoom();
        userRoom.setUserId(userId);
        userRoom.setRoomId(room.getId());
        userRoom.setRoomAdmin(1);
        userRoomMapper.insert(userRoom);
        Set<RoomMemberResponse> members = userRoomMapper.selectMembersByRoomId(room.getId());
        RoomResponse roomResponse = new RoomResponse();
        BeanUtils.copyProperties(room, roomResponse);
        roomResponse.setMembers(members);
        return roomResponse;
    }

    public void patchRoomById(Authentication authentication, Long roomId, RoomRequest roomRequest) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(userId, roomId);
        boolean isServerAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (userRoom == null && !isServerAdmin) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }
        if (userRoom != null && userRoom.getRoomAdmin() != 1) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        Room room = new Room();
        BeanUtils.copyProperties(roomRequest, room);
        room.setId(roomId);
        room.setPassword(passwordEncoder.encode(roomRequest.getPassword()));
        int lines = roomMapper.updateById(room);
        if (lines == 0) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }
    }

    public void deleteRoomById(Authentication authentication, Long roomId) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(userId, roomId);
        boolean isServerAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (userRoom == null && !isServerAdmin) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }
        if (userRoom != null && userRoom.getRoomAdmin() != 1) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        int lines = roomMapper.deleteById(roomId);
        if (lines == 0) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }
    }
}
