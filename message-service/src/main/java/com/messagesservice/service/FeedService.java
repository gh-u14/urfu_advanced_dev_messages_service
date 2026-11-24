package com.messagesservice.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.messagesservice.client.UserServiceClient;
import com.messagesservice.dao.MessagesRepository;
import com.messagesservice.dto.AuthorDto;
import com.messagesservice.dto.FeedItemResponse;
import com.messagesservice.dto.PageResponse;
import com.messagesservice.model.Message;

@Service
public class FeedService {

    private final MessagesRepository messagesRepository;
    private final UserServiceClient userServiceClient;

    public FeedService(MessagesRepository messagesRepository, UserServiceClient userServiceClient) {
        this.messagesRepository = messagesRepository;
        this.userServiceClient = userServiceClient;
    }

    public PageResponse<FeedItemResponse> getFeed(Long followerId, Integer limit, Integer offset) {
        int lim = normalizeLimit(limit);
        int off = normalizeOffset(offset);

        List<Message> messages = messagesRepository.findFeed(followerId, lim, off);
        if (messages.isEmpty()) {
            return new PageResponse<>(List.of(), lim, off);
        }

        List<Long> authorIds = messages.stream()
                .map(Message::getAuthorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, AuthorDto> authors = userServiceClient.getUsersByIds(authorIds).stream()
                .collect(Collectors.toMap(AuthorDto::getId, Function.identity(), (a, b) -> a));

        List<FeedItemResponse> items = messages.stream()
                .map(message -> new FeedItemResponse(
                        message.getId(),
                        message.getText(),
                        authors.getOrDefault(
                                message.getAuthorId(),
                                new AuthorDto(message.getAuthorId(), null, null)),
                        message.getCreatedAt()))
                .toList();

        return new PageResponse<>(items, lim, off);
    }

    private static int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 20;
        }
        return Math.min(limit, 100);
    }

    private static int normalizeOffset(Integer offset) {
        if (offset == null || offset < 0) {
            return 0;
        }
        return offset;
    }
}
