package org.karane.logger.domain;

import java.time.Instant;

public record LogEntry(Instant timestamp, LogLevel level, String message) {

    public static LogEntry of(LogLevel level, String message) {
        return new LogEntry(Instant.now(), level, message);
    }

    @Override
    public String toString() {
        return "[%s] %s - %s".formatted(timestamp, level, message);
    }
}
