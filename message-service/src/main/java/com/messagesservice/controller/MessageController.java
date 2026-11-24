package com.messagesservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.messagesservice.config.AuthContext;
import com.messagesservice.dto.CreateMessageRequest;
import com.messagesservice.dto.ItemsResponse;
import com.messagesservice.dto.MessageResponse;
import com.messagesservice.dto.UpdateMessageRequest;
import com.messagesservice.service.MessageService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/messages")
    public Mono<ResponseEntity<MessageResponse>> create(
            ServerWebExchange exchange,
            @RequestBody CreateMessageRequest request) {
        Long userId = AuthContext.requireUserId(exchange);
        List<String> roles = AuthContext.roles(exchange);
        return Mono.fromCallable(() -> messageService.create(userId, roles, request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/messages/{id}")
    public Mono<MessageResponse> getById(@PathVariable Long id) {
        return Mono.fromCallable(() -> messageService.getById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PatchMapping("/messages/{id}")
    public Mono<MessageResponse> update(
            ServerWebExchange exchange,
            @PathVariable Long id,
            @RequestBody UpdateMessageRequest request) {
        Long userId = AuthContext.requireUserId(exchange);
        return Mono.fromCallable(() -> messageService.update(userId, id, request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/messages/{id}")
    public Mono<ResponseEntity<Void>> delete(
            ServerWebExchange exchange,
            @PathVariable Long id) {
        Long userId = AuthContext.requireUserId(exchange);
        List<String> roles = AuthContext.roles(exchange);
        return Mono.fromRunnable(() -> messageService.delete(userId, roles, id))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/users/{id}/messages")
    public Mono<ItemsResponse<MessageResponse>> getUserMessages(
            @PathVariable("id") Long authorId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset) {
        return Mono.fromCallable(() -> messageService.getByAuthor(authorId, limit, offset))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
