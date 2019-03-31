package com.github.freeacs.service;

import com.github.freeacs.dao.UserDao;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDao userDao;
    private final PermissionService permissionService;

    @Autowired
    public UserService(UserDao userDao, PermissionService permissionService) {
        this.userDao = userDao;
        this.permissionService = permissionService;
    }

    public Option<UserDto> getByUserName(String username) {
        return userDao.findUserByName(username)
                .map(user -> {
                    UserDto userDto = new UserDto(
                            user.getId(),
                            user.getUsername(),
                            user.getSecret(),
                            user.getFullname(),
                            user.getAccesslist(),
                            List.empty()
                    );
                    return userDto.withPermissions(permissionService.getByUser(userDto));
                });
    }
}
