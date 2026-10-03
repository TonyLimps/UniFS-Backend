package org.tonylimps.unifs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tonylimps.unifs.model.dto.request.JoinRoomRequest;
import org.tonylimps.unifs.model.dto.request.PatchMemberRequest;
import org.tonylimps.unifs.model.dto.request.RoomRequest;
import org.tonylimps.unifs.model.dto.response.RoomResponse;
import org.tonylimps.unifs.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(Authentication authentication, @PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(authentication, roomId));
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(Authentication authentication, @RequestBody RoomRequest request) {
        var response = roomService.createRoom(authentication, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> patchRoomById(Authentication authentication,
                                              @PathVariable Long roomId,
                                              @RequestBody RoomRequest request) {
        roomService.patchRoomById(authentication, roomId, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(Authentication authentication, @PathVariable Long roomId) {
        roomService.deleteRoomById(authentication, roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{roomId}/members")
    public ResponseEntity<Void> joinRoom(Authentication authentication,
                                         @PathVariable Long roomId,
                                         @RequestBody JoinRoomRequest request) {
        roomService.joinRoom(authentication, roomId, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{roomId}/members/{userId}")
    public ResponseEntity<Void> patchMemberById(Authentication authentication,
                                                @PathVariable Long roomId,
                                                @PathVariable Long userId,
                                                @RequestBody PatchMemberRequest request) {
        roomService.patchMemberById(authentication, roomId, userId, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<Void> deleteMemberById(Authentication authentication,
                                                 @PathVariable Long roomId,
                                                 @PathVariable Long userId) {
        roomService.deleteMemberById(authentication, roomId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
