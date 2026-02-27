package com.fy.erp.security;

public class UserContext {
    private static final ThreadLocal<UserPrincipal> USER = new ThreadLocal<>();

    public static void set(UserPrincipal principal) {
        USER.set(principal);
    }

    public static UserPrincipal get() {
        return USER.get();
    }

    public static void clear() {
        USER.remove();
    }
}
