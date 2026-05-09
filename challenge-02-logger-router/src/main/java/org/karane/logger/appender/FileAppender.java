package org.karane.logger.appender;

import org.karane.logger.domain.LogEntry;
import org.karane.logger.domain.LogLevel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileAppender implements LogAppender {

    private final Path path;
    private final LogLevel minLevel;

    public FileAppender(Path path, LogLevel minLevel) {
        this.path = path;
        this.minLevel = minLevel;
    }

    public FileAppender(Path path) {
        this(path, LogLevel.DEBUG);
    }

    @Override
    public boolean accepts(LogLevel level) {
        return level.ordinal() >= minLevel.ordinal();
    }

    @Override
    public void append(LogEntry entry) {
        try (Writer w = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(entry.toString());
            w.write(System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
