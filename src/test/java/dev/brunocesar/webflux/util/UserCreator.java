package dev.brunocesar.webflux.util;

import dev.brunocesar.webflux.domain.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Component;

@Component
public class UserCreator {

    private static final String USER_PASSWORD = "user123";
    private static final String ADMIN_PASSWORD = "admin123";

    public User createUser() {
        var user = new User();
        user.setId(1);
        user.setUsername("user");
        user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(USER_PASSWORD));
        user.setName("User");
        user.setAuthorities("ROLE_USER");
        return user;
    }

    public User createAdmin() {
        var admin = new User();
        admin.setId(2);
        admin.setUsername("admin");
        admin.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(ADMIN_PASSWORD));
        admin.setName("Admin");
        admin.setAuthorities("ROLE_ADMIN,ROLE_USER");
        return admin;
    }

    public static String getUserPassword() {
        return USER_PASSWORD;
    }

    public static String getAdminPassword() {
        return ADMIN_PASSWORD;
    }
}
