package me.progfrog.idol.flow.service;

import me.progfrog.idol.flow.EmbeddedRedisConfig;
import me.progfrog.idol.flow.exception.ApplicationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(EmbeddedRedisConfig.class)
class UserQueueServiceTest {

    @Autowired
    private UserQueueService userQueueService;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @AfterEach
    void afterEach() {
        ReactiveRedisConnection redisConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        redisConnection.serverCommands().flushAll().subscribe();
    }

    @Test
    @DisplayName("registerWaitQueue: 대기 큐에 사용자 등록하기")
    void registerWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 102L))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    @DisplayName("alreadyRegisterWaitQueue: 대기 큐에 이미 등록된 사용자 다시 등록 시도하기")
    void alreadyRegisterWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectError(ApplicationException.class)
                .verify();
    }

    @Test
    @DisplayName("emptyAllowUser: 대기 큐가 비어있을 때 입장 큐에 사용자 넣기 시도")
    void emptyAllowUser() {
        StepVerifier.create(userQueueService.allowUser("default", 1L))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    @DisplayName("allowUser: 대기 큐에 사용자가 있을 때, 입장 큐에 사용자 넣기 시도")
    void allowUser() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.registerWaitQueue("default", 101L))
                        .then(userQueueService.registerWaitQueue("default", 102L))
                        .then(userQueueService.allowUser("default", 2L)))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    @DisplayName("allowUser2: 대기 큐에 존재하는 사용자 수보다 더 많이 입장 큐에 사용자 넣기 시도")
    void allowUser2() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.registerWaitQueue("default", 101L))
                        .then(userQueueService.registerWaitQueue("default", 102L))
                        .then(userQueueService.allowUser("default", 5L)))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    @DisplayName("allowUserAfterRegisterWaitQueue: 입장 큐 처리 후 다시 대기 큐에 사용자 등록하기")
    void allowUserAfterRegisterWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.registerWaitQueue("default", 101L))
                        .then(userQueueService.registerWaitQueue("default", 102L))
                        .then(userQueueService.allowUser("default", 3L))
                        .then(userQueueService.registerWaitQueue("default", 200L)))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    @DisplayName("isNotAllowed: 입장 허용 안된 사용자 1")
    void isNotAllowed() {
        StepVerifier.create(userQueueService.isAllowed("default", 100L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("isNotAllowed2: 입장 허용 안된 사용자 2")
    void isNotAllowed2() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.allowUser("default", 3L))
                        .then(userQueueService.isAllowed("default", 101L)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("isAllowed: 입장 허용된 사용자")
    void isAllowed() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                        .then(userQueueService.allowUser("default", 3L))
                        .then(userQueueService.isAllowed("default", 100L)))
                .expectNext(true)
                .verifyComplete();
    }
}
