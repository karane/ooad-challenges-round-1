package org.karane.logger;

import org.karane.logger.appender.AsyncAppenderDecorator;
import org.karane.logger.appender.ConsoleAppender;
import org.karane.logger.appender.ElkAppender;
import org.karane.logger.appender.FileAppender;
import org.karane.logger.domain.LogLevel;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        Path logFile = Path.of("app.log");

        // Stub ELK sender — prints what would be POSTed
        ElkAppender.HttpSender consoleSender =
                (endpoint, payload) -> System.out.println("[ELK -> %s] %s".formatted(endpoint, payload));

        // Scenario 1: sync console + sync file
        System.out.println("=== Scenario 1: Sync Console + File ===");
        Logger syncLogger = Logger.builder()
                .withAppender(new ConsoleAppender(LogLevel.DEBUG))
                .withAppender(new FileAppender(logFile, LogLevel.WARN))
                .build();

        syncLogger.debug("Application starting up");
        syncLogger.info("Loading configuration");
        syncLogger.warn("Config value 'timeout' missing, using default");
        syncLogger.error("Failed to connect to database");

        System.out.println();

        // Scenario 2: async console + sync ELK
        System.out.println("=== Scenario 2: Async Console + Sync ELK ===");
        AsyncAppenderDecorator asyncConsole =
                new AsyncAppenderDecorator(new ConsoleAppender(LogLevel.INFO));

        Logger elkLogger = Logger.builder()
                .withAsyncAppender(new ConsoleAppender(LogLevel.INFO))
                .withAppender(new ElkAppender("http://elk:9200/logs", consoleSender))
                .build();

        elkLogger.debug("This debug line is filtered by INFO min-level");
        elkLogger.info("User login: alice");
        elkLogger.warn("High memory usage detected");
        elkLogger.error("Unhandled exception in request pipeline");

        // give async thread a moment to flush before JVM exits
        asyncConsole.shutdown();

        System.out.println();
        System.out.println("WARN+ entries written to: " + logFile.toAbsolutePath());
    }
}
