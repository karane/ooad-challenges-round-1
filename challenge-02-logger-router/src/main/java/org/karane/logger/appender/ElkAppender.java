package org.karane.logger.appender;

import org.karane.logger.domain.LogEntry;
import org.karane.logger.domain.LogLevel;

/**
 * Simulates shipping log entries to an ELK stack via HTTP POST.
 * The actual HTTP call is delegated to a sender so tests can inject a fake.
 */
public class ElkAppender implements LogAppender {

    public interface HttpSender {
        void send(String endpoint, String payload);
    }

    private final String endpoint;
    private final LogLevel minLevel;
    private final HttpSender sender;

    public ElkAppender(String endpoint, LogLevel minLevel, HttpSender sender) {
        this.endpoint = endpoint;
        this.minLevel = minLevel;
        this.sender = sender;
    }

    public ElkAppender(String endpoint, HttpSender sender) {
        this(endpoint, LogLevel.DEBUG, sender);
    }

    @Override
    public boolean accepts(LogLevel level) {
        return level.ordinal() >= minLevel.ordinal();
    }

    @Override
    public void append(LogEntry entry) {
        String payload = """
                {"timestamp":"%s","level":"%s","message":"%s"}"""
                .formatted(entry.timestamp(), entry.level(), entry.message().replace("\"", "\\\""));
        sender.send(endpoint, payload);
    }
}
