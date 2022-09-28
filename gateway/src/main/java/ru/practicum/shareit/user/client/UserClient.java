package ru.practicum.shareit.user.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.baseClient.BaseClient;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
    }

    public ResponseEntity<Object> create(UserCreateDto userCreateDto) {
        return post("", userCreateDto);
    }

    public ResponseEntity<Object> update(long userId, UserDto userDto) {
        return patch("/" + userId, userId, userDto);
    }

    public ResponseEntity<Object> delete(long userId) {
        return delete("/" + userId, userId);
    }

    public ResponseEntity<Object> get(long userId) {
        return get("/" + userId, userId);
    }

    public ResponseEntity<Object> getAll() {
        return get("");
    }
}