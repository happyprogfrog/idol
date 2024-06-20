package me.progfrog.idol.flow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /**
     * 사용자를 대기열에 등록
     * redis sorted set
     * key: userId, value: unix timestamp
     *
     * @param userId 사용자 ID
     * @return rank 대기 번호
     */
    public Mono<Long> registerWaitQueue(final Long userId) {
        var unixTimestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add("user-queue", userId.toString(), unixTimestamp)
                .filter(i -> i)
                .switchIfEmpty(Mono.error(new Exception("이미 대기열에 등록된 사용자 입니다.")))
                .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank("user-queue", userId.toString()))
                .map(rank -> rank >= 0 ? rank + 1 : rank);
    }
}
