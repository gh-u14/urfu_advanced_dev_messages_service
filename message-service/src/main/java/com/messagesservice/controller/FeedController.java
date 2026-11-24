package com.messagesservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.messagesservice.config.AuthContext;
import com.messagesservice.dto.FeedItemResponse;
import com.messagesservice.dto.PageResponse;
import com.messagesservice.service.FeedService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/feed")
    public Mono<PageResponse<FeedItemResponse>> getFeed(
            ServerWebExchange exchange,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset) {
        Long userId = AuthContext.requireUserId(exchange);
        return Mono.fromCallable(() -> feedService.getFeed(userId, limit, offset))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
