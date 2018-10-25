package com.stayrascal.service.application.user;

import com.stayrascal.service.application.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void addUser(User user);

    Optional<User> selectUserById(int id);

    Optional<User> selectUserByUUID(String uuid);

    void removeUserByUUID(String uuid);

    List<User> listUsers();
}
