package com.campus.market.util;

import com.campus.market.common.BusinessException;
import com.campus.market.common.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLE = "role";

    @Value("${campus.jwt.secret}")
    private String secret;

    @Value("${campus.jwt.expire-hours:72}")
    private long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成 token */
    public String createToken(Long userId, Integer role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE, role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireHours * 3600_000L))
                .signWith(key())
                .compact();
    }

    /** 解析 token，失败抛出未登录异常 */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    public Long getUserId(Claims claims) {
        Object uid = claims.get(CLAIM_USER_ID);
        return uid == null ? null : Long.valueOf(uid.toString());
    }

    public Integer getRole(Claims claims) {
        Object role = claims.get(CLAIM_ROLE);
        return role == null ? null : Integer.valueOf(role.toString());
    }

    /** 从请求头中提取 token（兼容 Bearer 前缀） */
    public String resolveToken(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        return header.startsWith("Bearer ") ? header.substring(7) : header;
    }
}
