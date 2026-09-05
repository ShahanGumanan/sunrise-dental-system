package com.sunrisedental;

import com.sunrisedental.controller.AuthController;
import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.User;
import com.sunrisedental.util.SessionManager;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTest {
    @After
    public void clearSession() {
        SessionManager.logout();
    }

    @Test
    public void allowsActiveUserWithCorrectPassword() {
        User user = user("admin", true);
        UserDAO dao = mock(UserDAO.class);
        when(dao.findByUsername("admin")).thenReturn(user);
        assertTrue(new AuthController(dao).login("admin", "password"));
        assertTrue(SessionManager.getCurrentUser() == user);
    }

    @Test
    public void rejectsInactiveUserBeforeCheckingPassword() {
        User user = user("receptionist", false);
        UserDAO dao = mock(UserDAO.class);
        when(dao.findByUsername("receptionist")).thenReturn(user);
        assertFalse(new AuthController(dao).login("receptionist", "password"));
        verify(dao).findByUsername("receptionist");
        assertTrue(SessionManager.getCurrentUser() == null);
    }

    @Test
    public void reportsWhenAccountIsDeactivated() {
        UserDAO dao = mock(UserDAO.class);
        when(dao.findByUsername("receptionist")).thenReturn(user("receptionist", false));

        assertEquals(AuthController.LoginResult.ACCOUNT_DEACTIVATED,
                new AuthController(dao).authenticate("receptionist", "password"));
    }

    @Test
    public void rejectsWrongPassword() {
        UserDAO dao = mock(UserDAO.class);
        when(dao.findByUsername("admin")).thenReturn(user("admin", true));
        assertFalse(new AuthController(dao).login("admin", "wrong-password"));
        assertTrue(SessionManager.getCurrentUser() == null);
    }

    @Test
    public void rejectsUnknownUserAndBlankCredentials() {
        UserDAO dao = mock(UserDAO.class);
        when(dao.findByUsername(anyString())).thenReturn(null);
        AuthController controller = new AuthController(dao);
        assertFalse(controller.login("unknown", "password"));
        assertFalse(controller.login("", ""));
        verify(dao).findByUsername("unknown");
        verify(dao).findByUsername("");
    }

    private User user(String username, boolean active) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(com.sunrisedental.util.PasswordUtil.hash("password"));
        user.setRole("admin");
        user.setActive(active);
        return user;
    }
}