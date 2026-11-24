package com.messagesservice.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.messagesservice.client.UserServiceClient;
import com.messagesservice.dao.SubscriptionsRepository;
import com.messagesservice.dto.AuthorDto;
import com.messagesservice.dto.ItemsResponse;
import com.messagesservice.exception.BadRequestException;

@Service
public class SubscriptionService {

    private final SubscriptionsRepository subscriptionsRepository;
    private final UserServiceClient userServiceClient;

    public SubscriptionService(
            SubscriptionsRepository subscriptionsRepository,
            UserServiceClient userServiceClient) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BadRequestException("invalid_follow", "Нельзя подписаться на себя");
        }
        userServiceClient.getUser(followeeId);
        subscriptionsRepository.follow(followerId, followeeId);
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        subscriptionsRepository.unfollow(followerId, followeeId);
    }

    public ItemsResponse<AuthorDto> getFollowing(Long userId) {
        return toAuthors(subscriptionsRepository.findFollowingIds(userId));
    }

    public ItemsResponse<AuthorDto> getFollowers(Long userId) {
        return toAuthors(subscriptionsRepository.findFollowerIds(userId));
    }

    private ItemsResponse<AuthorDto> toAuthors(List<Long> ids) {
        if (ids.isEmpty()) {
            return new ItemsResponse<>(List.of());
        }

        Map<Long, AuthorDto> byId = userServiceClient.getUsersByIds(ids).stream()
                .collect(Collectors.toMap(AuthorDto::getId, Function.identity(), (a, b) -> a));

        List<AuthorDto> items = ids.stream()
                .map(id -> byId.getOrDefault(id, new AuthorDto(id, null, null)))
                .toList();
        return new ItemsResponse<>(items);
    }
}
