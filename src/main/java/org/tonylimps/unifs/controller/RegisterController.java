package org.tonylimps.unifs.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tonylimps.unifs.model.dto.request.RegisterFinishRequest;
import org.tonylimps.unifs.model.dto.request.CreateUserRequest;
import org.tonylimps.unifs.model.dto.response.RegisterFinishResponse;
import org.tonylimps.unifs.model.dto.response.RegisterStartResponse;
import org.tonylimps.unifs.service.RegisterService;

@Validated
@RestController
@RequestMapping("/api/register")
public class RegisterController {

    private final RegisterService registerService;

    @Autowired
    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping
    public ResponseEntity<RegisterStartResponse> start(@Valid @RequestBody CreateUserRequest request) {
        var result = registerService.start(request);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/finish")
    public ResponseEntity<RegisterFinishResponse> finish(@Valid @RequestBody RegisterFinishRequest request) {
        var result = registerService.finish(request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
