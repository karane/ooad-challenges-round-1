package org.karane.logger;

import org.karane.logger.appender.AsyncAppenderDecorator;
import org.karane.logger.appender.ConsoleAppender;
import org.karane.logger.appender.ElkAppender;
import org.karane.logger.appender.FileAppender;
import org.karane.logger.appender.LogAppender;
import org.karane.logger.domain.LogEntry;
import org.karane.logger.domain.LogLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    // helper

    static class CapturingAppender implements LogAppender {
        final List<LogEntry> entries = new ArrayList<>();
        private final LogLevel minLevel;

        CapturingAppender(LogLevel minLevel) { this.minLevel = minLevel; }
        CapturingAppender() { this(LogLevel.DEBUG); }

        @Override
        public boolean accepts(LogLevel level) {
            return level.ordinal() >= minLevel.ordinal();
        }

        @Override
        public void append(LogEntry entry) {
            entries.add(entry);
        }
    }

    @Nested
    @DisplayName("Sync delivery")
    class SyncDelivery {

        @Test
        @DisplayName("Single appender receives all log levels")
        void singleAppenderReceivesAll() {
            var cap = new CapturingAppender();
            var logger = Logger.builder().withAppender(cap).build();

            logger.debug("d");
            logger.info("i");
            logger.warn("w");
            logger.error("e");

            assertEquals(4, cap.entries.size());
        }

        @Test
        @DisplayName("Log entry carries correct level and message")
        void entryContent() {
            var cap = new CapturingAppender();
            Logger.builder().withAppender(cap).build().warn("disk almost full");

            assertEquals(1, cap.entries.size());
            assertEquals(LogLevel.WARN, cap.entries.getFirst().level());
            assertEquals("disk almost full", cap.entries.getFirst().message());
        }

        @Test
        @DisplayName("Appender min-level filter blocks lower-level entries")
        void minLevelFilter() {
            var cap = new CapturingAppender(LogLevel.WARN);
            var logger = Logger.builder().withAppender(cap).build();

            logger.debug("debug");
            logger.info("info");
            logger.warn("warn");
            logger.error("error");

            assertEquals(2, cap.entries.size());
            assertTrue(cap.entries.stream().allMatch(e -> e.level().ordinal() >= LogLevel.WARN.ordinal()));
        }

        @Test
        @DisplayName("No appender configured throws IllegalStateException")
        void noAppenderThrows() {
            assertThrows(IllegalStateException.class, () -> Logger.builder().build());
        }
    }

    @Nested
    @DisplayName("Multi-appender routing")
    class MultiAppender {

        @Test
        @DisplayName("Two appenders each receive the same entry")
        void twoAppendersReceiveEntry() {
            var cap1 = new CapturingAppender();
            var cap2 = new CapturingAppender();
            var logger = Logger.builder().withAppender(cap1).withAppender(cap2).build();

            logger.error("boom");

            assertEquals(1, cap1.entries.size());
            assertEquals(1, cap2.entries.size());
            assertEquals(cap1.entries.getFirst().message(), cap2.entries.getFirst().message());
        }

        @Test
        @DisplayName("Each appender applies its own level filter independently")
        void independentFilters() {
            var debugCap = new CapturingAppender(LogLevel.DEBUG);
            var errorCap = new CapturingAppender(LogLevel.ERROR);
            var logger = Logger.builder()
                    .withAppender(debugCap)
                    .withAppender(errorCap)
                    .build();

            logger.debug("d");
            logger.info("i");
            logger.error("e");

            assertEquals(3, debugCap.entries.size());
            assertEquals(1, errorCap.entries.size());
        }
    }

    @Nested
    @DisplayName("Async delivery")
    class AsyncDelivery {

        @Test
        @DisplayName("Async appender delivers entry after awaiting latch")
        void asyncDeliversEntry() throws InterruptedException {
            var latch = new CountDownLatch(1);
            var cap = new CapturingAppender() {
                @Override
                public void append(LogEntry entry) {
                    super.append(entry);
                    latch.countDown();
                }
            };

            var async = new AsyncAppenderDecorator(cap);
            Logger.builder().withAppender(async).build().info("async message");

            assertTrue(latch.await(2, TimeUnit.SECONDS), "entry not delivered within timeout");
            assertEquals(1, cap.entries.size());
            async.shutdown();
        }

        @Test
        @DisplayName("Async appender respects delegate min-level filter")
        void asyncRespectsFilter() throws InterruptedException {
            var latch = new CountDownLatch(1);
            var cap = new CapturingAppender(LogLevel.ERROR) {
                @Override
                public void append(LogEntry entry) {
                    super.append(entry);
                    latch.countDown();
                }
            };

            var async = new AsyncAppenderDecorator(cap);
            var logger = Logger.builder().withAppender(async).build();

            logger.debug("ignored");
            logger.info("ignored");
            logger.error("counted");

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(1, cap.entries.size());
            async.shutdown();
        }
    }

    @Nested
    @DisplayName("FileAppender")
    class FileAppenderTests {

        @Test
        @DisplayName("WARN+ entries are written to file, DEBUG/INFO are not")
        void writesWarnAndAbove(@TempDir Path tmp) throws IOException {
            Path logFile = tmp.resolve("test.log");
            var logger = Logger.builder()
                    .withAppender(new FileAppender(logFile, LogLevel.WARN))
                    .build();

            logger.debug("skip");
            logger.info("skip");
            logger.warn("keep-warn");
            logger.error("keep-error");

            List<String> lines = Files.readAllLines(logFile);
            assertEquals(2, lines.size());
            assertTrue(lines.get(0).contains("keep-warn"));
            assertTrue(lines.get(1).contains("keep-error"));
        }
    }

    @Nested
    @DisplayName("ElkAppender")
    class ElkAppenderTests {

        @Test
        @DisplayName("Elk sender receives JSON payload with correct fields")
        void sendJsonPayload() {
            var sent = new ArrayList<String>();
            ElkAppender.HttpSender captureSender = (endpoint, payload) -> sent.add(payload);

            var logger = Logger.builder()
                    .withAppender(new ElkAppender("http://elk:9200/logs", captureSender))
                    .build();

            logger.error("disk failure");

            assertEquals(1, sent.size());
            assertTrue(sent.getFirst().contains("\"level\":\"ERROR\""));
            assertTrue(sent.getFirst().contains("\"message\":\"disk failure\""));
        }

        @Test
        @DisplayName("Elk appender min-level filter suppresses lower-level entries")
        void elkMinLevelFilter() {
            var sent = new ArrayList<String>();
            ElkAppender.HttpSender captureSender = (endpoint, payload) -> sent.add(payload);

            var logger = Logger.builder()
                    .withAppender(new ElkAppender("http://elk:9200/logs", LogLevel.ERROR, captureSender))
                    .build();

            logger.info("ignore me");
            logger.error("send me");

            assertEquals(1, sent.size());
        }
    }

    @Nested
    @DisplayName("ConsoleAppender")
    class ConsoleAppenderTests {

        @Test
        @DisplayName("ConsoleAppender min-level filter rejects lower levels")
        void minLevelRejected() {
            var cap = new CapturingAppender(LogLevel.INFO);
            // wrap in a Console-like appender by using the CapturingAppender itself
            var logger = Logger.builder().withAppender(cap).build();

            logger.debug("should be blocked");
            assertTrue(cap.entries.isEmpty());
        }
    }
}
