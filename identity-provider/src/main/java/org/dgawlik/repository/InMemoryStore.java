package org.dgawlik.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dgawlik.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryStore {
    private Map<String, List<User>> store;

    @Value("classpath:/users.json")
    Resource resource;

    @Autowired
    ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        var token = new TypeReference<Map<String, List<User>>>() {
        };
        store = mapper.readValue(resource.getURL(), token);
    }

    public User getByEmail(String email) {
        return store.get("users").stream().filter(u -> u.getEmail().equals(email)).findAny()
                .orElse(null);
    }
}
