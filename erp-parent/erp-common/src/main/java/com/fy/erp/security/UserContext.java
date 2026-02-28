package com.fy.erp.security;

public class UserContext {
  private static final ThreadLocal<UserPrincipal> holder = new ThreadLocal<>();

  public static void set(UserPrincipal principal) {
    holder.set(principal);
  }

  public static UserPrincipal get() {
    return holder.get();
  }

  public static void clear() {
    holder.remove();
  }
}
