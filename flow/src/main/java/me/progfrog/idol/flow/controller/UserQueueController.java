package me.progfrog.idol.flow.controller;

import lombok.RequiredArgsConstructor;
import me.progfrog.idol.flow.dto.AllowUserResponse;
import me.progfrog.idol.flow.dto.AllowedUserResponse;
import me.progfrog.idol.flow.dto.QueueStatusResponse;
import me.progfrog.idol.flow.dto.RegisterUserResponse;
import me.progfrog.idol.flow.service.UserQueueService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {

    private final UserQueueService userQueueService;

    /**
     * 사용자를 대기 큐에 등록
     *
     * @param queue 대기 큐 이름
     * @param userId 사용자 ID
     * @return 대기 번호가 담긴 dto
     */
    @PostMapping
    public Mono<RegisterUserResponse> registerUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                   @RequestParam(name = "user-id") Long userId) {

        return userQueueService.registerWaitQueue(queue, userId)
                .map(RegisterUserResponse::new);
    }

    /**
     * 사용자를 입장 가능 상태로 전환
     *
     * @param queue 대기 큐 이름
     * @param count 대기 큐에서 가져올 사용자 수
     * @return 요청 수와 처리 수가 담긴 dto
     */
    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                             @RequestParam(name = "count") Long count) {
        return userQueueService.allowUser(queue, count)
                .map(allowedCount -> new AllowUserResponse(count, allowedCount));
    }

    /**
     * 사용자가 입장 가능한 상태인지 조회
     *
     * @param queue 입장 큐 이름
     * @param userId 사용자 ID
     * @return 입장 가능 여부가 담긴 dto
     */
    @GetMapping("/allowed")
    public Mono<AllowedUserResponse> isAllowedUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                   @RequestParam(name = "user-id") Long userId) {
        return userQueueService.isAllowed(queue, userId)
                .map(AllowedUserResponse::new);
    }

    /**
     * 사용자의 대기 번호 조회
     *
     * @param queue 대기 큐 이름
     * @param userId 사용자 ID
     * @return 대기 번호가 담긴 dto
     */
    @GetMapping("/progress")
    public Mono<QueueStatusResponse> getProgress(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                 @RequestParam(name = "user-id") Long userId) {
        return userQueueService.getQueueStatus(queue, userId)
                .map(QueueStatusResponse::new);
    }
}
