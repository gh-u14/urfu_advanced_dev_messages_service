package com.messagesservice.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSubscriptionsRepository implements SubscriptionsRepository {

    private final JdbcTemplate jdbc;

    public JdbcSubscriptionsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void follow(Long followerId, Long followeeId) {
        jdbc.update(
                """
                INSERT INTO subscriptions (follower_id, followee_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """,
                followerId,
                followeeId);
    }

    @Override
    public void unfollow(Long followerId, Long followeeId) {
        jdbc.update(
                "DELETE FROM subscriptions WHERE follower_id = ? AND followee_id = ?",
                followerId,
                followeeId);
    }

    @Override
    public List<Long> findFollowingIds(Long followerId) {
        return jdbc.query(
                """
                SELECT followee_id
                FROM subscriptions
                WHERE follower_id = ?
                ORDER BY followee_id
                """,
                (rs, rowNum) -> rs.getLong("followee_id"),
                followerId);
    }

    @Override
    public List<Long> findFollowerIds(Long followeeId) {
        return jdbc.query(
                """
                SELECT follower_id
                FROM subscriptions
                WHERE followee_id = ?
                ORDER BY follower_id
                """,
                (rs, rowNum) -> rs.getLong("follower_id"),
                followeeId);
    }
}
