package com.messagesservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.messagesservice.config.AuthContext;
import com.messagesservice.dto.AuthorDto;
import com.messagesservice.dto.ItemsResponse;
import com.messagesservice.service.SubscriptionService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PutMapping("/users/me/following/{id}")
    public Mono<ResponseEntity<Void>> follow(
            ServerWebExchange exchange,
            @PathVariable("id") Long followeeId) {
        Long followerId = AuthContext.requireUserId(exchange);
        return Mono.fromRunnable(() -> subscriptionService.follow(followerId, followeeId))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @DeleteMapping("/users/me/following/{id}")
    public Mono<ResponseEntity<Void>> unfollow(
            ServerWebExchange exchange,
            @PathVariable("id") Long followeeId) {
        Long followerId = AuthContext.requireUserId(exchange);
        return Mono.fromRunnable(() -> subscriptionService.unfollow(followerId, followeeId))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/users/{id}/following")
    public Mono<ItemsResponse<AuthorDto>> getFollowing(@PathVariable("id") Long userId) {
        return Mono.fromCallable(() -> subscriptionService.getFollowing(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/users/{id}/followers")
    public Mono<ItemsResponse<AuthorDto>> getFollowers(@PathVariable("id") Long userId) {
        return Mono.fromCallable(() -> subscriptionService.getFollowers(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
