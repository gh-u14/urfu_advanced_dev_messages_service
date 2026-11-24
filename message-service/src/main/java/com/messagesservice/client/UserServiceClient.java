package com.messagesservice.client;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.messagesservice.dto.AuthorDto;
import com.messagesservice.dto.ItemsResponse;
import com.messagesservice.exception.NotFoundException;

import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private static final ParameterizedTypeReference<ItemsResponse<AuthorDto>> AUTHORS_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient usersWebClient;

    public UserServiceClient(WebClient usersWebClient) {
        this.usersWebClient = usersWebClient;
    }

    public AuthorDto getUser(Long id) {
        return usersWebClient.get()
                .uri("/api/v1/users/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new NotFoundException("user_not_found", "Пользователь не найден")))
                .bodyToMono(AuthorDto.class)
                .block();
    }

    public List<AuthorDto> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String idsParam = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        ItemsResponse<AuthorDto> response = usersWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/users")
                        .queryParam("ids", idsParam)
                        .build())
                .retrieve()
                .bodyToMono(AUTHORS_TYPE)
                .block();

        if (response == null || response.getItems() == null) {
            return List.of();
        }
        return response.getItems();
    }
}
