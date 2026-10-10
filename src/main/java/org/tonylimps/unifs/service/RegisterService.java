package org.tonylimps.unifs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tonylimps.unifs.exception.ApiException;
import org.tonylimps.unifs.exception.ErrorCode;
import org.tonylimps.unifs.model.dto.request.RegisterFinishRequest;
import org.tonylimps.unifs.model.dto.request.UserRequest;
import org.tonylimps.unifs.model.dto.response.RegisterFinishResponse;
import org.tonylimps.unifs.model.dto.response.RegisterStartResponse;
import org.tonylimps.unifs.mapper.UserMapper;
import org.tonylimps.unifs.model.entity.User;
import org.tonylimps.unifs.model.pojo.AuthSession;
import org.tonylimps.unifs.security.JwtUtil;
import org.tonylimps.unifs.security.PassKeyUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RegisterService {
    private final int REQUEST_TIMEOUT_SECONDS;
    private final SecureRandom random = new SecureRandom();
    private final UserMapper userMapper;
    private final PassKeyUtil passKeyUtil;
    private final JwtUtil jwtUtil;
    private final Map<Long, AuthSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public RegisterService(UserMapper userMapper,
                           PassKeyUtil passKeyUtil,
                           JwtUtil jwtUtil,
                           @Value("${register-login-request-timeout-seconds}") int exp) {
        this.userMapper = userMapper;
        this.passKeyUtil = passKeyUtil;
        this.jwtUtil = jwtUtil;
        this.REQUEST_TIMEOUT_SECONDS = exp;
    }

    public RegisterStartResponse start(UserRequest request) {
        var id = random.nextLong();
        var session = new AuthSession();
        var challenge = passKeyUtil.generateChallenge();
        BeanUtils.copyProperties(request, session);
        session.setExpireTime(LocalDateTime.now().plusSeconds(REQUEST_TIMEOUT_SECONDS));
        session.setChallenge(challenge);
        sessions.put(id, session);

        var response = new RegisterStartResponse();
        response.setChallenge(session.getChallenge());
        response.setId(id);
        return response;
    }

    public RegisterFinishResponse finish(RegisterFinishRequest request) {
        var session = sessions.get(request.getId());
        // check if request timed out
        if (session == null) throw new ApiException(ErrorCode.REGISTER_FAILED);
        if (session.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.REGISTER_FAILED);
        }

        // verify signature
        var signature = request.getSignature();
        var rawChallenge = session.getChallenge();
        var publicKey = session.getPublicKey();
        if (!passKeyUtil.verify(rawChallenge, signature, publicKey)) {
            throw new ApiException(ErrorCode.REGISTER_FAILED);
        }

        // create user
        var user = new User();
        BeanUtils.copyProperties(session, user);
        userMapper.insert(user);

        var token = jwtUtil.generateToken(user);
        var response = new RegisterFinishResponse();
        response.setId(user.getId());
        response.setToken(token);
        sessions.remove(request.getId());

        log.info("User registered: uid={} name={}", user.getId(), user.getName());
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
        log.info("Cleaned {} timeout register requests", cleaned.get());
    }
}
