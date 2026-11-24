package com.messagesservice.util;

import org.springframework.stereotype.Component;

import com.messagesservice.dto.MessageResponse;
import com.messagesservice.model.Message;

@Component
public class MessageMapper {

    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getText(),
                message.getAuthorId(),
                message.getCreatedAt());
    }
}
