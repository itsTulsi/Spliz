package com.fairshare.security;

import com.fairshare.entity.UserEntity;
import com.fairshare.exception.UnauthorizedException;

public class UserContext {

    private static final ThreadLocal<UserEntity> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(UserEntity user) {
        CURRENT_USER.set(user);
    }

    public static UserEntity getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static UserEntity requireCurrentUser() {
        UserEntity user = CURRENT_USER.get();
        if (user == null) {
            throw new UnauthorizedException("Authentication required. Please sign in.");
        }
        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
