package org.tonylimps.unifs.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tonylimps.unifs.exception.BusinessException;
import org.tonylimps.unifs.exception.ErrorCode;
import org.tonylimps.unifs.model.dto.request.RegisterFinishRequest;
import org.tonylimps.unifs.model.dto.request.UserRequest;
import org.tonylimps.unifs.model.dto.response.RegisterFinishResponse;
import org.tonylimps.unifs.model.dto.response.RegisterStartResponse;
import org.tonylimps.unifs.mapper.UserMapper;
import org.tonylimps.unifs.model.entity.User;
import org.tonylimps.unifs.model.pojo.AuthSession;
import org.tonylimps.unifs.util.JwtUtil;
import org.tonylimps.unifs.util.PassKeyUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        if (session == null) throw new BusinessException(ErrorCode.REGISTER_FAILED);
        if (session.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.REGISTER_FAILED);
        }

        // verify signature
        var signature = request.getSignature();
        var rawChallenge = session.getChallenge();
        var publicKey = session.getPublicKey();
        if (!passKeyUtil.verify(rawChallenge, signature, publicKey)) {
            throw new BusinessException(ErrorCode.REGISTER_FAILED);
        }

        // create user
        var user = new User();
        BeanUtils.copyProperties(session, user);
        userMapper.insert(user);

        var token = jwtUtil.generateToken(user);
        var response = new RegisterFinishResponse();
        response.setId(user.getId());
        response.setToken(token);

        return response;
    }

    @Scheduled(cron = "${clean-timeout-requests-cron}")
    public void cleanTempUsers() {
        sessions.forEach((id, info) -> {
            if (info.getExpireTime().isBefore(LocalDateTime.now())) {
                sessions.remove(id);
            }
        });
    }
}
