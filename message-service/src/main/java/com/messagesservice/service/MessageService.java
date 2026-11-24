package com.messagesservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.messagesservice.dao.MessagesRepository;
import com.messagesservice.dto.CreateMessageRequest;
import com.messagesservice.dto.ItemsResponse;
import com.messagesservice.dto.MessageResponse;
import com.messagesservice.dto.UpdateMessageRequest;
import com.messagesservice.exception.BadRequestException;
import com.messagesservice.exception.ForbiddenException;
import com.messagesservice.exception.NotFoundException;
import com.messagesservice.model.Message;
import com.messagesservice.util.MessageMapper;

@Service
public class MessageService {

    private final MessagesRepository messagesRepository;
    private final MessageMapper messageMapper;

    public MessageService(MessagesRepository messagesRepository, MessageMapper messageMapper) {
        this.messagesRepository = messagesRepository;
        this.messageMapper = messageMapper;
    }

    @Transactional
    public MessageResponse create(Long authorId, List<String> roles, CreateMessageRequest request) {
        if (roles == null || !roles.contains("producer")) {
            throw new ForbiddenException("forbidden", "Требуется роль producer");
        }
        String text = requireText(request != null ? request.getText() : null);

        Message message = new Message();
        message.setText(text);
        message.setAuthorId(authorId);
        return messageMapper.toResponse(messagesRepository.save(message));
    }

    public MessageResponse getById(Long id) {
        Message message = messagesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("message_not_found", "Сообщение не найдено"));
        return messageMapper.toResponse(message);
    }

    @Transactional
    public MessageResponse update(Long userId, Long id, UpdateMessageRequest request) {
        Message message = messagesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("message_not_found", "Сообщение не найдено"));

        if (!message.getAuthorId().equals(userId)) {
            throw new ForbiddenException("forbidden", "Редактировать может только автор");
        }

        message.setText(requireText(request != null ? request.getText() : null));
        return messageMapper.toResponse(messagesRepository.update(message));
    }

    @Transactional
    public void delete(Long userId, List<String> roles, Long id) {
        Message message = messagesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("message_not_found", "Сообщение не найдено"));

        boolean isAuthor = message.getAuthorId().equals(userId);
        boolean isAdmin = roles != null && roles.contains("administrator");
        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException("forbidden", "Удалить может автор или administrator");
        }

        messagesRepository.softDelete(id);
    }

    public ItemsResponse<MessageResponse> getByAuthor(Long authorId, Integer limit, Integer offset) {
        int lim = normalizeLimit(limit);
        int off = normalizeOffset(offset);
        List<MessageResponse> items = messagesRepository.findByAuthorId(authorId, lim, off).stream()
                .map(messageMapper::toResponse)
                .toList();
        return new ItemsResponse<>(items);
    }

    private static String requireText(String text) {
        if (text == null || text.isBlank()) {
            throw new BadRequestException("invalid_text", "text обязателен");
        }
        return text.trim();
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
