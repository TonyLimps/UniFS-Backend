package org.tonylimps.unifs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tonylimps.unifs.model.dto.request.LoginFinishRequest;
import org.tonylimps.unifs.model.dto.response.LoginFinishResponse;
import org.tonylimps.unifs.model.dto.response.LoginStartResponse;
import org.tonylimps.unifs.model.dto.request.LoginStartRequest;
import org.tonylimps.unifs.exception.ApiException;
import org.tonylimps.unifs.exception.ErrorCode;
import org.tonylimps.unifs.mapper.UserMapper;
import org.tonylimps.unifs.model.entity.User;
import org.tonylimps.unifs.model.pojo.AuthSession;
import org.tonylimps.unifs.security.JwtUtil;
import org.tonylimps.unifs.security.PassKeyUtil;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class LoginService {
    private final int REQUEST_TIMEOUT_SECONDS;
    private final UserMapper userMapper;
    private final PassKeyUtil passKeyUtil;
    private final JwtUtil jwtUtil;
    private final Map<Long, AuthSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public LoginService(UserMapper userMapper,
                        PassKeyUtil passKeyUtil,
                        JwtUtil jwtUtil,
                        @Value("${register-login-request-timeout-seconds}") int exp) {
        this.userMapper = userMapper;
        this.passKeyUtil = passKeyUtil;
        this.jwtUtil = jwtUtil;
        this.REQUEST_TIMEOUT_SECONDS = exp;
    }

    public LoginStartResponse start(LoginStartRequest request) {
        var id = request.getId();
        var user = userMapper.selectById(id);
        if (user == null) {
            throw new ApiException(ErrorCode.LOGIN_FAILED);
        }
        var session = new AuthSession();
        var challenge = passKeyUtil.generateChallenge();
        BeanUtils.copyProperties(user, session);
        session.setExpireTime(LocalDateTime.now().plusSeconds(REQUEST_TIMEOUT_SECONDS));
        session.setChallenge(challenge);
        sessions.put(id, session);

        var response = new LoginStartResponse();
        response.setChallenge(session.getChallenge());
        return response;
    }

    public LoginFinishResponse finish(LoginFinishRequest request) {
        var id = request.getId();
        var session = sessions.get(id);
        // check if request timed out
        if (session == null) throw new ApiException(ErrorCode.LOGIN_FAILED);
        if (session.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.LOGIN_FAILED);
        }

        // verify signature
        var signature = request.getSignature();
        var rawChallenge = session.getChallenge();
        var publicKey = session.getPublicKey();
        if (!passKeyUtil.verify(rawChallenge, signature, publicKey)) {
            throw new ApiException(ErrorCode.LOGIN_FAILED);
        }

        // generate token
        User user = userMapper.selectById(id);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        var token = jwtUtil.generateToken(user);
        var response = new LoginFinishResponse();
        response.setToken(token);
        sessions.remove(request.getId());

        log.debug("User logged in: uid={}", user.getId());
        return response;
    }

    @Scheduled(cron = "${clean-timeout-requests-cron}")
    public void cleanTimeoutRequests() {
        AtomicInteger cleaned = new AtomicInteger();
        sessions.forEach((id, info) -> {
            if (info.getExpireTime().isBefore(LocalDateTime.now())) {
                sessions.remove(id);
                cleaned.addAndGet(1);
            }
        });
        log.info("Cleaned {} timeout login requests", cleaned.get());
    }
}
