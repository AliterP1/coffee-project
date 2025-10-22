package com.example.mycoffeedemo.utils;

import jakarta.servlet.http.HttpServletRequest;

public class CurrentUserUtil {
    public static Long getCurrentUserId(HttpServletRequest request) {
        Object id = request.getAttribute("userId");
        return id == null ? null : Long.valueOf(id.toString());
    }
    public static String getCurrentUserRole(HttpServletRequest request) {
        Object r = request.getAttribute("role");
        return r == null ? null : r.toString();
    }
}
