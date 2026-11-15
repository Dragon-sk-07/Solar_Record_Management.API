package com.suraj.Customer_Portal_29.config;

import com.suraj.Customer_Portal_29.entity.Owner;

public final class CurrentUserContext {

    private static final ThreadLocal<Owner> HOLDER = new ThreadLocal<>();

    private CurrentUserContext() {}

    public static void set(Owner user) {
        HOLDER.set(user);
    }

    public static Owner get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}