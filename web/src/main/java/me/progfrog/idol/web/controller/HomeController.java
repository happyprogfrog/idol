package me.progfrog.idol.web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import me.progfrog.idol.web.dto.AllowedUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

@Controller
public class HomeController {

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/home")
    public String getHome(@RequestParam(name = "queue", defaultValue = "default") String queue,
                          @RequestParam(name = "user-id") Long userId,
                          HttpServletRequest request) {

        var cookies = request.getCookies();
        var cookieName = "user-queue-%s-token".formatted(queue);

        var token = "";
        if (cookies != null) {
            var cookie = Arrays.stream(cookies).filter(i -> i.getName().equalsIgnoreCase(cookieName)).findFirst();
            token = cookie.orElse(new Cookie(cookieName, "")).getValue();
        }

        var uri = UriComponentsBuilder
                .fromUriString("http://127.0.0.1:9010")
                .path("/api/v1/queue/allowed")
                .queryParam("queue", queue)
                .queryParam("user-id", userId)
                .queryParam("token", token)
                .encode()
                .build()
                .toUri();

        ResponseEntity<AllowedUserResponse> response = restTemplate.getForEntity(uri, AllowedUserResponse.class);
        if (response.getBody() == null || !response.getBody().isAllowed()) {
            // 입장 가능 상태가 아니라면, 대기용 웹 페이지로 리다이렉트
            return "redirect:http://127.0.0.1:9010/waiting-room?user-id=%d&redirect-url=%s".formatted(
                    userId, "http://127.0.0.1:9000/home?user-id=%d".formatted(userId));
        }

        // 입장 가능 상태라면 해당 페이지를 진입
        return "home";
    }
}