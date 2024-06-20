package me.progfrog.idol.flow.controller;

import lombok.RequiredArgsConstructor;
import me.progfrog.idol.flow.dto.QueueStatusResponse;
import me.progfrog.idol.flow.service.UserQueueService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {

    private final UserQueueService userQueueService;

    /**
     * @param queue 입장 큐 이름
     * @param userId 사용자 ID
     * @return 사용자가 대기할 웹 페이지
     */
    @GetMapping("/waiting-room")
    Mono<Rendering> getWaitingRoomPage(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                       @RequestParam(name = "user-id") Long userId) {

        String redirectUrl = "http://www.naver.com";

        return userQueueService.isAllowed(queue, userId)
                .filter(isAllowed -> isAllowed)
                .flatMap(isAllowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
                .switchIfEmpty(userQueueService.registerWaitingQueueOrGetQueueStatus(queue, userId)
                        .map(dto -> {
                            QueueStatusResponse res = new QueueStatusResponse(dto);
                            return Rendering.view("waiting-room")
                                    .modelAttribute("queue", queue)
                                    .modelAttribute("userId", userId)
                                    .modelAttribute("queueFront", res.queueFront())
                                    .modelAttribute("queueBack", res.queueBack())
                                    .modelAttribute("progress", res.progress())
                                    .build();
                        }));
    }
}
