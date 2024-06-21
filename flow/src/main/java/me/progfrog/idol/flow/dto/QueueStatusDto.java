package me.progfrog.idol.flow.dto;

public record QueueStatusDto(
        Long userRank,
        Long totalQueueSize,
        Double progress
) {
}
