package com.vibevault.productservice.commons;

import com.vibevault.productservice.dtos.commons.UserDto;
import com.vibevault.productservice.dtos.exceptions.authentication.InvalidTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthenticationCommons {
    @Autowired
    private RestTemplate restTemplate;
    private final String userServiceUrl = "http://localhost:8081/auth/validate";

    public UserDto validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.postForObject(userServiceUrl, entity, UserDto.class);
        } catch (InvalidTokenException e) {
            throw new InvalidTokenException("Invalid token provided.", e);
        } catch (Exception e) {
            throw new InvalidTokenException("An error occurred while validating the token.", e);
        }
    }
}
