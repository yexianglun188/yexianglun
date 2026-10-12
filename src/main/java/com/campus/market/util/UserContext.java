package com.campus.market.util;

import com.campus.market.common.BusinessException;
import com.campus.market.common.ResultCode;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE = new ThreadLocal<>();

    public static void set(Long userId, Integer role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 获取当前用户id，未登录则抛出异常 */
    public static Long requireUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    public static Integer getRole() {
        return ROLE.get();
    }

    public static boolean isAdmin() {
        Integer role = ROLE.get();
        return role != null && role == 1;
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
