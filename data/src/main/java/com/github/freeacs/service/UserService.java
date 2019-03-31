package com.github.freeacs.service;

import com.github.freeacs.dao.UserDao;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Option<UserDto> getByUserName(String username) {
        return userDao.findUser(username)
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getSecret(),
                        user.getFullname(),
                        user.getAccesslist()
                ));
    }
}
