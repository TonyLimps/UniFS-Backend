package org.tonylimps.unifs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<RoomResponse> createRoom(Authentication authentication, @RequestBody RoomRequest roomRequest) {
        var response = roomService.createRoom(authentication, roomRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> patchRoomById(Authentication authentication, @PathVariable Long roomId, @RequestBody RoomRequest roomRequest) {
        roomService.patchRoomById(authentication, roomId, roomRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(Authentication authentication, @PathVariable Long roomId) {
        roomService.deleteRoomById(authentication, roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
