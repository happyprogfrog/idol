package me.progfrog.idol.flow.controller;

import lombok.RequiredArgsConstructor;
import me.progfrog.idol.flow.dto.RegisterUserResponse;
import me.progfrog.idol.flow.service.UserQueueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {

    private final UserQueueService userQueueService;

    /**
     * 사용자를 대기열에 등록
     *
     * @param userId 사용자 ID
     * @return 대기 번호가 담긴 dto
     */
    @PostMapping
    public Mono<RegisterUserResponse> registerUser(@RequestParam(name = "user-id") Long userId) {

        return userQueueService.registerWaitQueue(userId)
                .map(RegisterUserResponse::new);
    }
}
