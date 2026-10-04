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
import org.tonylimps.unifs.model.dto.request.JoinRoomRequest;
import org.tonylimps.unifs.model.dto.request.PatchMemberRequest;
import org.tonylimps.unifs.model.dto.request.RoomRequest;
import org.tonylimps.unifs.model.dto.response.MemberResponse;
import org.tonylimps.unifs.model.dto.response.RoomResponse;
import org.tonylimps.unifs.model.entity.Room;
import org.tonylimps.unifs.model.entity.UserRoom;

import java.util.Set;

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
        UserRoom userRoom = userRoomMapper.select(roomId, userId);
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (userRoom == null && !isAdmin) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }

        Set<MemberResponse> members = userRoomMapper.selectMembersByRoomId(roomId);
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
        Set<MemberResponse> members = userRoomMapper.selectMembersByRoomId(room.getId());
        RoomResponse roomResponse = new RoomResponse();
        BeanUtils.copyProperties(room, roomResponse);
        roomResponse.setMembers(members);
        return roomResponse;
    }

    public void patchRoomById(Authentication authentication, Long roomId, RoomRequest roomRequest) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(roomId, userId);
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
        roomMapper.updateById(room);

        if (roomRequest.getPassword() != null) {
            userRoomMapper.deleteByRoomId(roomId);
        }
    }

    public void deleteRoomById(Authentication authentication, Long roomId) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(roomId, userId);
        boolean isServerAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (userRoom == null && !isServerAdmin) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }
        if (userRoom != null && userRoom.getRoomAdmin() != 1) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        roomMapper.deleteById(roomId);
    }

    public void joinRoom(Authentication authentication, Long roomId, JoinRoomRequest request) {
        Long userId = (Long) authentication.getPrincipal();

        String rawPassword = request.getPassword();
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.JOIN_ROOM_FAILED);
        }
        String password = room.getPassword();
        if (!passwordEncoder.matches(rawPassword, password)) {
            throw new BusinessException(ErrorCode.JOIN_ROOM_FAILED);
        }

        UserRoom userRoom = new UserRoom();
        userRoom.setUserId(userId);
        userRoom.setRoomId(roomId);
        userRoomMapper.insert(userRoom);
    }

    public void patchMemberById(Authentication authentication,
                                Long roomId,
                                Long memberId,
                                PatchMemberRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(roomId, userId);
        boolean isServerAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (userRoom == null && !isServerAdmin) {
            throw new BusinessException(ErrorCode.MEMBER_ACCESS_DENIED);
        }
        if (userRoom != null && userRoom.getRoomAdmin() != 1) {
            throw new BusinessException(ErrorCode.MEMBER_ACCESS_DENIED);
        }

        UserRoom targetUserRoom = userRoomMapper.select(roomId, memberId);
        if (targetUserRoom == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        BeanUtils.copyProperties(request, targetUserRoom);
        userRoomMapper.updateById(targetUserRoom);
    }

    public void deleteMemberById(Authentication authentication, Long roomId, Long memberId) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(roomId, userId);
        boolean isServerAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (userRoom == null && !isServerAdmin) {
            throw new BusinessException(ErrorCode.MEMBER_ACCESS_DENIED);
        }
        if (userRoom != null && userRoom.getRoomAdmin() != 1 && !userId.equals(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_ACCESS_DENIED);
        }

        UserRoom targetUserRoom = userRoomMapper.select(roomId, memberId);
        if (targetUserRoom == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        userRoomMapper.delete(roomId, memberId);
    }
}
