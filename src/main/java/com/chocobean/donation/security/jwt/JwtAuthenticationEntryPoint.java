package com.chocobean.donation.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String jwtException = (String) request.getAttribute("jwtException");
        String message = switch (jwtException != null ? jwtException : "") {
            case "TOKEN_EXPIRED"     -> "액세스 토큰이 만료되었습니다.";
            case "INVALID_SIGNATURE" -> "JWT 서명이 올바르지 않습니다.";
            case "MALFORMED_TOKEN"   -> "JWT 형식이 올바르지 않습니다.";
            case "UNSUPPORTED_TOKEN" -> "지원하지 않는 JWT 형식입니다.";
            case "EMPTY_TOKEN"       -> "JWT 값이 비어있습니다.";
            default                  -> "인증 토큰이 없거나 유효하지 않습니다.";
        };

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", new Date());
        data.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        data.put("error", "Unauthorized");
        data.put("message", message);
        data.put("path", request.getRequestURI());

        response.getOutputStream().println(mapper.writeValueAsString(data));
    }
}
