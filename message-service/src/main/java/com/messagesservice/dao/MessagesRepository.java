package com.messagesservice.dao;

import java.util.List;
import java.util.Optional;

import com.messagesservice.model.Message;

public interface MessagesRepository {

    Message save(Message message);

    Optional<Message> findById(Long id);

    Message update(Message message);

    void softDelete(Long id);

    List<Message> findByAuthorId(Long authorId, int limit, int offset);

    List<Message> findFeed(Long followerId, int limit, int offset);
}
