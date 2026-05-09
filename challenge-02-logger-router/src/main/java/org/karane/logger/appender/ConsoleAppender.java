package org.karane.logger.appender;

import org.karane.logger.domain.LogEntry;
import org.karane.logger.domain.LogLevel;

public class ConsoleAppender implements LogAppender {

    private final LogLevel minLevel;

    public ConsoleAppender(LogLevel minLevel) {
        this.minLevel = minLevel;
    }

    public ConsoleAppender() {
        this(LogLevel.DEBUG);
    }

    @Override
    public boolean accepts(LogLevel level) {
        return level.ordinal() >= minLevel.ordinal();
    }

    @Override
    public void append(LogEntry entry) {
        System.out.println(entry);
    }
}
