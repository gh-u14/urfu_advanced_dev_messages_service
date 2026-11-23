package com.messagesservice.dao;

import java.util.List;

public interface SubscriptionsRepository {

    void follow(Long followerId, Long followeeId);

    void unfollow(Long followerId, Long followeeId);

    List<Long> findFollowingIds(Long followerId);

    List<Long> findFollowerIds(Long followeeId);
}
