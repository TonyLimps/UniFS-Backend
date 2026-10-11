package org.tonylimps.unifs.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tonylimps.unifs.model.dto.request.LoginFinishRequest;
import org.tonylimps.unifs.model.dto.response.LoginFinishResponse;
import org.tonylimps.unifs.model.dto.response.LoginStartResponse;
import org.tonylimps.unifs.model.dto.request.LoginStartRequest;
import org.tonylimps.unifs.service.LoginService;

@Validated
@RestController
@RequestMapping("/api/login")
public class LoginController {
    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<LoginStartResponse> start(@Valid @RequestBody LoginStartRequest loginStartRequest) {
        return ResponseEntity.ok(loginService.start(loginStartRequest));
    }

    @PostMapping("/finish")
    public ResponseEntity<LoginFinishResponse> finish(@Valid @RequestBody LoginFinishRequest loginFinishRequest) {
        return ResponseEntity.ok(loginService.finish(loginFinishRequest));
    }

}
