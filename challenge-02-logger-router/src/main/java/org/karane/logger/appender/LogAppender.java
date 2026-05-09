package org.karane.logger.appender;

import org.karane.logger.domain.LogEntry;
import org.karane.logger.domain.LogLevel;

public interface LogAppender {

    void append(LogEntry entry);

    default boolean accepts(LogLevel level) {
        return true;
    }
}
