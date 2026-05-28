package com.aiguruz.common.util;

import com.aiguruz.user.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static User currentUser() {
        return (User) SecurityContextHolder.getContext()
                          .getAuthentication().getPrincipal();
    }
    public static String currentUserId()    { return currentUser().getId(); }
    public static String currentTenantId()  { return currentUser().getTenantId(); }
    public static String currentEmail()     { return currentUser().getEmail(); }
}

