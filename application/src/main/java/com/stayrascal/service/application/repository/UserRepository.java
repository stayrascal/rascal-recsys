package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.User;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    void addUser(@Param("user") User user);

    void deleteUserByUUID(@Param("uuid") String uuid);

    User getUserById(@Param("id") int id);

    User getUserByUUID(@Param("uuid") String uuid);
}
