package com.messagesservice.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messagesservice.dto.ErrorResponse;
import com.messagesservice.util.JwtProvider;

import reactor.core.publisher.Mono;

@Component
@Order(-100)
public class JwtAuthFilter implements WebFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "missing_token", "Authorization Bearer token is required");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtProvider.validate(token)) {
            return unauthorized(exchange, "invalid_token", "Token is invalid or expired");
        }

        exchange.getAttributes().put(AuthContext.ATTR_USER_ID, jwtProvider.getUserId(token));
        exchange.getAttributes().put(AuthContext.ATTR_ROLES, jwtProvider.getRoles(token));
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String error, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(new ErrorResponse(error, message));
        } catch (JsonProcessingException e) {
            bytes = ("{\"error\":\"" + error + "\",\"message\":\"" + message + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
