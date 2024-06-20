package me.progfrog.idol.flow.service;

import lombok.RequiredArgsConstructor;
import me.progfrog.idol.flow.exception.ErrorCode;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    private final String USER_QUEUE_ALLOW_KEY = "users:queue:%s:allow";

    /**
     * 사용자를 대기 큐에 등록
     * redis sorted set
     * key: userId, value: unix timestamp
     *
     * @param queue 대기 큐 이름
     * @param userId 사용자 ID
     * @return rank 대기 번호
     */
    public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
        var unixTimestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimestamp)
                .filter(isAdded -> isAdded)
                .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()))
                .flatMap(isAdded -> reactiveRedisTemplate.opsForZSet()
                        .rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
                .map(rank -> rank >= 0 ? rank + 1 : rank);
    }

    /**
     * 사용자를 입장 가능 상태로 전환
     * 1. 대기 큐에서 사용자 제거
     * 2. 입장 큐에 해당 사용자를 추가
     *
     * @param queue 대기 큐 이름
     * @param count 대기 큐에서 가져올 사용자 수
     * @return 입장 큐에 등록된 사용자 수
     */
    public Mono<Long> allowUser(final String queue, final Long count) {
        var unixTimestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), count)
                .flatMap(queueEntry -> Optional.ofNullable(queueEntry.getValue())
                        .map(userId -> reactiveRedisTemplate.opsForZSet()
                                .add(USER_QUEUE_ALLOW_KEY.formatted(queue), userId, unixTimestamp))
                        .orElse(Mono.empty()))
                .count();
    }

    /**
     * 사용자가 입장 가능한 상태인지 조회
     *
     * @param queue 입장 큐 이름
     * @param userId 사용자 ID
     * @return 입장 가능 여부
     */
    public Mono<Boolean> isAllowed(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_ALLOW_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0);
    }
}
