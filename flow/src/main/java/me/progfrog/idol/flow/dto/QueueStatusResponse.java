package me.progfrog.idol.flow.dto;

public record QueueStatusResponse(
        Long queueFront,
        Long queueBack,
        Double progress
) {
    public QueueStatusResponse(QueueStatusDto dto) {
        this(
                dto.userRank() > 0 ? dto.userRank() - 1 : dto.userRank(),
                dto.totalQueueSize() - dto.userRank(),
                dto.progress()
        );
    }
}
