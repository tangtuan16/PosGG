package Controllers;

import Models.User;
import Services.UserService;
import Utils.DBConnection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserController {
    private UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public boolean login(String username, String password) {
        return userService.login(username, password);
    }

    public boolean register(String username, String password, String name, String role) {
        boolean result = userService.register(username, password, name, role);
        return result;
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public boolean updateUserInfo(User user) {
        return userService.updateUserInfo(user);
    }

    public boolean updatePassword(int userId, String hashed) {
        return userService.updatePassword(userId, hashed);
    }
}
