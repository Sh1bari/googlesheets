package com.example.googlesheets.services;

import lombok.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Description:
 *
 * @author Vladimir Krasnov
 */
@Component
@RequiredArgsConstructor
public class RequestClientService {

    private final RestTemplate restTemplate;

    public String postRequest(String apiUrl, String apiKey, String jsonData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-key", apiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

        return restTemplate.postForObject(apiUrl, requestEntity, String.class);
    }
}
