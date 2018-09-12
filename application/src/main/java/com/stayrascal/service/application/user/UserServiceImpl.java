package com.stayrascal.service.application.user;

import com.stayrascal.service.application.domain.User;
import com.stayrascal.service.application.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addUser(User user) {
        Optional<User> quantum = selectUserByUUID(user.getUuid());
        if (quantum.isPresent()) {
            throw new UserAlreadyExistsException("User: " + user.getUuid() + " already exists.");
        }
        repository.addUser(user);
    }

    @Override
    public Optional<User> selectUserById(int id) {
        User user = repository.getUserById(id);
        if (user != null) return Optional.of(user);
        return Optional.empty();
    }

    @Override
    public Optional<User> selectUserByUUID(String uuid) {
        User user = repository.getUserByUUID(uuid);
        if (user != null) return Optional.of(user);
        return Optional.empty();
    }

    @Override
    public void removeUserByUUID(String uuid) {
        repository.deleteUserByUUID(uuid);
    }
}
