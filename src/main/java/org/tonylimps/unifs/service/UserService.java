package org.tonylimps.unifs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.tonylimps.unifs.exception.ApiException;
import org.tonylimps.unifs.exception.ErrorCode;
import org.tonylimps.unifs.mapper.UserMapper;
import org.tonylimps.unifs.model.dto.request.UserRequest;
import org.tonylimps.unifs.model.dto.response.UserResponse;
import org.tonylimps.unifs.model.entity.User;

@Slf4j
@Service
public class UserService {
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserResponse getCurrentUser(Authentication authentication) {
        Long id = (Long) authentication.getPrincipal();
        return getUserById(id);
    }

    public void patchCurrentUser(Authentication authentication, UserRequest request) {
        Long id = (Long) authentication.getPrincipal();
        patchUserById(id, request);
    }

    public void deleteCurrentUser(Authentication authentication) {
        Long id = (Long) authentication.getPrincipal();
        deleteUserById(id);
        log.info("User deleted: uid={}", id);
    }

    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);
        return userResponse;
    }

    public void patchUserById(Long id, UserRequest request) {
        User user = new User();
        user.setId(id);
        BeanUtils.copyProperties(request, user);
        int lines = userMapper.updateById(user);
        if (lines == 0) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
    }

    public void deleteUserById(Long id) {
        int lines = userMapper.deleteById(id);
        if (lines == 0) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
