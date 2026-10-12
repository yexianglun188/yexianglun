package com.campus.market.interceptor;

import com.campus.market.annotation.RequireAdmin;
import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ResultCode;
import com.campus.market.util.JwtUtil;
import com.campus.market.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;

/**
 * 登录鉴权拦截器：解析 token 写入上下文，并按注解校验权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跨域预检请求直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        // 非 Controller 方法（静态资源等）放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 1. 尝试解析 token（未携带 token 时不报错，由注解决定是否拦截）
        String token = jwtUtil.resolveToken(request.getHeader("Authorization"));
        if (token != null && !token.isBlank()) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                UserContext.set(jwtUtil.getUserId(claims), jwtUtil.getRole(claims));
            } catch (Exception e) {
                log.debug("token 解析失败: {}", e.getMessage());
            }
        }

        // 2. 管理员权限校验
        if (findAnnotation(handlerMethod, RequireAdmin.class) != null) {
            if (UserContext.getUserId() == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }
            if (!UserContext.isAdmin()) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            return true;
        }

        // 3. 登录校验
        if (findAnnotation(handlerMethod, RequireLogin.class) != null && UserContext.getUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 防止线程复用导致的用户信息串号
        UserContext.clear();
    }

    /** 方法注解优先，其次取类注解 */
    private <A extends Annotation> A findAnnotation(HandlerMethod handlerMethod, Class<A> type) {
        A annotation = handlerMethod.getMethodAnnotation(type);
        return annotation != null ? annotation : handlerMethod.getBeanType().getAnnotation(type);
    }
}
