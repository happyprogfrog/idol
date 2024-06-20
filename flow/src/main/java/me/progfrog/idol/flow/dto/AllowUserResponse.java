package me.progfrog.idol.flow.dto;

public record AllowUserResponse(
        Long requestCount,
        Long allowedCount
) {
}
