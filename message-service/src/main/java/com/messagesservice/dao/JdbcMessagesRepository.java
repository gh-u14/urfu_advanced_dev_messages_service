package com.messagesservice.dao;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.messagesservice.model.Message;

@Repository
public class JdbcMessagesRepository implements MessagesRepository {

    private static final String COLUMNS = "id, text, author_id, created_at, updated_at, deleted_at";

    private final JdbcTemplate jdbc;
    private final MessageRowMapper messageMapper;

    public JdbcMessagesRepository(JdbcTemplate jdbc, MessageRowMapper messageMapper) {
        this.jdbc = jdbc;
        this.messageMapper = messageMapper;
    }

    @Override
    public Message save(Message message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO messages (text, author_id, created_at, updated_at)
                    VALUES (?, ?, ?, ?)
                    """,
                    new String[] {"id"});
            ps.setString(1, message.getText());
            ps.setLong(2, message.getAuthorId());
            ps.setTimestamp(3, Timestamp.valueOf(now));
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null && keyHolder.getKeys() != null) {
            Object id = keyHolder.getKeys().get("id");
            if (id instanceof Number number) {
                key = number;
            }
        }
        if (key == null) {
            throw new IllegalStateException("Failed to obtain generated message id");
        }

        message.setId(key.longValue());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        return message;
    }

    @Override
    public Optional<Message> findById(Long id) {
        List<Message> messages = jdbc.query(
                "SELECT " + COLUMNS + " FROM messages WHERE id = ? AND deleted_at IS NULL",
                messageMapper,
                id);
        return messages.stream().findFirst();
    }

    @Override
    public Message update(Message message) {
        LocalDateTime now = LocalDateTime.now();
        jdbc.update(
                """
                UPDATE messages
                SET text = ?, updated_at = ?
                WHERE id = ? AND deleted_at IS NULL
                """,
                message.getText(),
                Timestamp.valueOf(now),
                message.getId());
        message.setUpdatedAt(now);
        return message;
    }

    @Override
    public void softDelete(Long id) {
        LocalDateTime now = LocalDateTime.now();
        jdbc.update(
                """
                UPDATE messages
                SET deleted_at = ?, updated_at = ?
                WHERE id = ? AND deleted_at IS NULL
                """,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now),
                id);
    }

    @Override
    public List<Message> findByAuthorId(Long authorId, int limit, int offset) {
        return jdbc.query(
                """
                SELECT """ + COLUMNS + """
                FROM messages
                WHERE author_id = ? AND deleted_at IS NULL
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """,
                messageMapper,
                authorId,
                limit,
                offset);
    }

    @Override
    public List<Message> findFeed(Long followerId, int limit, int offset) {
        return jdbc.query(
                """
                SELECT m.id, m.text, m.author_id, m.created_at, m.updated_at, m.deleted_at
                FROM subscriptions s
                JOIN messages m ON m.author_id = s.followee_id
                WHERE s.follower_id = ?
                  AND m.deleted_at IS NULL
                ORDER BY m.created_at DESC, m.id DESC
                LIMIT ? OFFSET ?
                """,
                messageMapper,
                followerId,
                limit,
                offset);
    }
}
