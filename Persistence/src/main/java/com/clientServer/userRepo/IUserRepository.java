package com.clientServer.userRepo;

import com.clientServer.IRepository;
import com.clientServer.entities.User;

public interface IUserRepository extends IRepository<Integer, User> {
    User findUserByUsername(String username);
}
