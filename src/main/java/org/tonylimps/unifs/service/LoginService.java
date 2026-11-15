package org.tonylimps.unifs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tonylimps.unifs.model.dto.request.LoginFinishRequest;
import org.tonylimps.unifs.model.dto.request.LoginStartRequest;
import org.tonylimps.unifs.model.dto.response.LoginStartResponse;
import org.tonylimps.unifs.exception.ApiException;
import org.tonylimps.unifs.exception.ErrorCode;
import org.tonylimps.unifs.mapper.UserMapper;
import org.tonylimps.unifs.model.dto.response.LoginFinishResponse;
import org.tonylimps.unifs.model.entity.User;
import org.tonylimps.unifs.model.pojo.AuthSession;
import org.tonylimps.unifs.security.JwtUtil;
import org.tonylimps.unifs.security.PassKeyUtil;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class LoginService {
    private final int REQUEST_TIMEOUT_SECONDS;
    private final SecureRandom random = new SecureRandom();
    private final UserMapper userMapper;
    private final PassKeyUtil passKeyUtil;
    private final JwtUtil jwtUtil;
    private final Map<Long, AuthSession> loginSessions = new ConcurrentHashMap<>();
    private final Map<Long, AuthSession> registerSessions = new ConcurrentHashMap<>();

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
        var publicKey = request.getPublicKey();
        var user = userMapper.selectByPublicKey(publicKey);
        var registering = user == null;
        var id = registering ? random.nextLong() : user.getId();
        var session = new AuthSession();
        var challenge = passKeyUtil.generateChallenge();
        BeanUtils.copyProperties(request, session);
        session.setExpireTime(ZonedDateTime.now().plusSeconds(REQUEST_TIMEOUT_SECONDS));
        session.setChallenge(challenge);
        (registering ? registerSessions : loginSessions).put(id, session);

        var response = new LoginStartResponse();
        response.setChallenge(session.getChallenge());
        response.setId(id);
        return response;
    }

    public LoginFinishResponse finish(LoginFinishRequest request) {
        var id = request.getId();

        var registering = !loginSessions.containsKey(id);
        var sessions = (registering ? registerSessions : loginSessions);
        var session = sessions.get(id);
        if (session == null) throw new ApiException(ErrorCode.LOGIN_FAILED);

        // check if request timed out
        if (session.getExpireTime().isBefore(ZonedDateTime.now())) {
            throw new ApiException(ErrorCode.LOGIN_FAILED);
        }

        // verify signature
        var signature = request.getSignature();
        var rawChallenge = session.getChallenge();
        var publicKey = session.getPublicKey();
        if (!passKeyUtil.verify(rawChallenge, signature, publicKey)) {
            throw new ApiException(ErrorCode.LOGIN_FAILED);
        }

        User user;
        if (registering) {
            // create user
            user = new User();
            id = user.getId();
            BeanUtils.copyProperties(session, user);
            userMapper.insert(user);
        } else {
            // generate token
            user = userMapper.selectById(id);
            user.setLastLoginTime(ZonedDateTime.now());
            userMapper.updateById(user);
        }

        var token = jwtUtil.generateToken(user);
        var response = new LoginFinishResponse();
        response.setId(id);
        response.setToken(token);
        sessions.remove(request.getId());

        if (registering) {
            log.info("User registered: uid={} name={}", user.getId(), user.getName());
        } else {
            log.debug("User logged in: uid={}", user.getId());
        }

        return response;
    }

    @Scheduled(cron = "${clean-timeout-requests-cron}")
    public void cleanTimeoutRequests() {
        AtomicInteger cleaned = new AtomicInteger();
        loginSessions.forEach((id, info) -> {
            if (info.getExpireTime().isBefore(ZonedDateTime.now())) {
                loginSessions.remove(id);
                cleaned.addAndGet(1);
            }
        });
        log.info("Cleaned {} timeout login requests", cleaned.get());
        registerSessions.forEach((id, info) -> {
            if (info.getExpireTime().isBefore(ZonedDateTime.now())) {
                registerSessions.remove(id);
                cleaned.addAndGet(1);
            }
        });
        log.info("Cleaned {} timeout register requests", cleaned.get());

    }
}
