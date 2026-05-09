package org.karane.logger.appender;

import org.karane.logger.domain.LogEntry;
import org.karane.logger.domain.LogLevel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncAppenderDecorator implements LogAppender {

    private final LogAppender delegate;
    private final ExecutorService executor;

    public AsyncAppenderDecorator(LogAppender delegate) {
        this.delegate = delegate;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "async-logger");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public boolean accepts(LogLevel level) {
        return delegate.accepts(level);
    }

    @Override
    public void append(LogEntry entry) {
        executor.submit(() -> delegate.append(entry));
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
