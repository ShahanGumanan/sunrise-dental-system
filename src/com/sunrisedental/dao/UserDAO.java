package com.sunrisedental.dao;

import com.sunrisedental.model.User;
import java.util.List;

public interface UserDAO {
    User findByUsername(String username);
    boolean create(User user);
    List<User> findAll();
    boolean updateStatus(int id, boolean isActive);
    boolean updateStatus(int id, boolean isActive, int actingUserId);
}