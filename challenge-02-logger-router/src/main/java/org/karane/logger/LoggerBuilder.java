package org.karane.logger;

import org.karane.logger.appender.AsyncAppenderDecorator;
import org.karane.logger.appender.LogAppender;

import java.util.ArrayList;
import java.util.List;

public class LoggerBuilder {

    private final List<LogAppender> appenders = new ArrayList<>();

    public LoggerBuilder withAppender(LogAppender appender) {
        appenders.add(appender);
        return this;
    }

    public LoggerBuilder withAsyncAppender(LogAppender appender) {
        appenders.add(new AsyncAppenderDecorator(appender));
        return this;
    }

    public Logger build() {
        if (appenders.isEmpty()) {
            throw new IllegalStateException("Logger requires at least one appender");
        }
        return new Logger(List.copyOf(appenders));
    }
}
