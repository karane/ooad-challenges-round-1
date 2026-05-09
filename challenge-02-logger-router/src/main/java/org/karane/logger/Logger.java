package org.karane.logger;

import org.karane.logger.appender.LogAppender;
import org.karane.logger.domain.LogEntry;
import org.karane.logger.domain.LogLevel;

import java.util.List;

public class Logger {

    private final List<LogAppender> appenders;

    Logger(List<LogAppender> appenders) {
        this.appenders = List.copyOf(appenders);
    }

    public void log(LogLevel level, String message) {
        LogEntry entry = LogEntry.of(level, message);
        for (LogAppender appender : appenders) {
            if (appender.accepts(level)) {
                appender.append(entry);
            }
        }
    }

    public void debug(String message) { log(LogLevel.DEBUG, message); }
    public void info(String message)  { log(LogLevel.INFO,  message); }
    public void warn(String message)  { log(LogLevel.WARN,  message); }
    public void error(String message) { log(LogLevel.ERROR, message); }

    public static LoggerBuilder builder() {
        return new LoggerBuilder();
    }
}
