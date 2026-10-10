package org.tonylimps.unifs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tonylimps.unifs.exception.ApiException;
import org.tonylimps.unifs.exception.ErrorCode;
import org.tonylimps.unifs.mapper.RoomMapper;
import org.tonylimps.unifs.mapper.UserRoomMapper;
import org.tonylimps.unifs.model.dto.request.JoinRoomRequest;
import org.tonylimps.unifs.model.dto.request.PatchMemberRequest;
import org.tonylimps.unifs.model.dto.request.RoomRequest;
import org.tonylimps.unifs.model.dto.response.MemberResponse;
import org.tonylimps.unifs.model.dto.response.RoomResponse;
import org.tonylimps.unifs.model.entity.Room;
import org.tonylimps.unifs.model.entity.UserRoom;

import java.util.Set;

@Slf4j
@Service
@Transactional
public class RoomService {
    private final RoomMapper roomMapper;
    private final UserRoomMapper userRoomMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RoomService(RoomMapper roomMapper,
                       UserRoomMapper userRoomMapper,
                       PasswordEncoder passwordEncoder) {
        this.roomMapper = roomMapper;
        this.userRoomMapper = userRoomMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public RoomResponse getRoomById(Authentication authentication, Long roomId) {

        if(!hasReadPermission(authentication, roomId)){
            log.warn("Unauthorized access to the room: uid={} rid={}", authentication.getPrincipal(), roomId);
            throw new ApiException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new ApiException(ErrorCode.ROOM_NOT_FOUND);
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

        if(!hasModifyPermission(authentication, roomId)){
            log.warn("Unauthorized access to patch room: uid={} rid={}", authentication.getPrincipal(), roomId);
            throw new ApiException(ErrorCode.ROOM_ACCESS_DENIED);
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

        if(!hasModifyPermission(authentication, roomId)){
            log.warn("Unauthorized access to delete room: uid={} rid={}", authentication.getPrincipal(), roomId);
            throw new ApiException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        roomMapper.deleteById(roomId);
    }

    public void joinRoom(Authentication authentication, Long roomId, JoinRoomRequest request) {
        Long userId = (Long) authentication.getPrincipal();

        String rawPassword = request.getPassword();
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new ApiException(ErrorCode.JOIN_ROOM_FAILED);
        }
        String password = room.getPassword();
        if (!passwordEncoder.matches(rawPassword, password)) {
            throw new ApiException(ErrorCode.JOIN_ROOM_FAILED);
        }

        UserRoom userRoom = new UserRoom();
        userRoom.setUserId(userId);
        userRoom.setRoomId(roomId);
        userRoomMapper.insert(userRoom);
        log.info("User joined room: uid={} rid={}", userId, roomId);
    }

    public void patchMemberById(Authentication authentication,
                                Long roomId,
                                Long memberId,
                                PatchMemberRequest request) {
        if(!hasModifyPermission(authentication, roomId)){
            log.warn("Unauthorized access to patch member: uid={} rid={} target uid={}", authentication.getPrincipal(), roomId, memberId);
            throw new ApiException(ErrorCode.MEMBER_ACCESS_DENIED);
        }

        UserRoom targetUserRoom = userRoomMapper.select(roomId, memberId);
        if (targetUserRoom == null) {
            throw new ApiException(ErrorCode.MEMBER_NOT_FOUND);
        }

        BeanUtils.copyProperties(request, targetUserRoom);
        userRoomMapper.updateById(targetUserRoom);
    }

    public void deleteMemberById(Authentication authentication, Long roomId, Long memberId) {
        if(!hasModifySelfPermission(authentication, roomId, memberId)){
            log.warn("Unauthorized access to delete member: uid={} rid={} target uid={}", authentication.getPrincipal(), roomId, memberId);
            throw new ApiException(ErrorCode.MEMBER_ACCESS_DENIED);
        }

        UserRoom targetUserRoom = userRoomMapper.select(roomId, memberId);
        if (targetUserRoom == null) {
            throw new ApiException(ErrorCode.MEMBER_NOT_FOUND);
        }

        userRoomMapper.delete(roomId, memberId);
        log.info("User left room: uid={} rid={} operator={}", memberId, roomId, authentication.getPrincipal());
    }

    private boolean hasReadPermission(Authentication authentication, Long roomId) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(roomId, userId);
        boolean isServerAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return userRoom != null || isServerAdmin;
    }

    private boolean hasModifyPermission(Authentication authentication, Long roomId) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(roomId, userId);
        boolean isServerAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return isServerAdmin || (userRoom != null && userRoom.getRoomAdmin() == 1);
    }

    private boolean hasModifySelfPermission(Authentication authentication, Long roomId, Long targetMemberId) {
        Long userId = (Long) authentication.getPrincipal();
        UserRoom userRoom = userRoomMapper.select(roomId, userId);
        boolean isServerAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return isServerAdmin || (userRoom != null && userRoom.getRoomAdmin() == 1) || userId.equals(targetMemberId);
    }
}