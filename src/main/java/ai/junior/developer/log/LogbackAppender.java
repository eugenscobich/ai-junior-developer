package ai.junior.developer.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogbackAppender extends AppenderBase<ILoggingEvent> {

    @Getter
    private final Map<String, Queue<String>> logMessages = new ConcurrentHashMap<>();

    @Override
    protected void append(ILoggingEvent event) {
        var threadId = event.getMDCPropertyMap().get("threadId");
        var runId = event.getMDCPropertyMap().get("runId");

        if (threadId != null && runId != null) {
            String formattedMessage = event.getFormattedMessage();
            logMessages.computeIfAbsent(threadId + "_" + runId, k -> new ConcurrentLinkedQueue<>()).add(formattedMessage);
        }
    }

    @PostConstruct
    public void configRootLogger() {
        var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        setContext(context);
        start();
        var root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(this);
    }

    @PreDestroy
    public void unregisterAppender() {
        var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        var root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        stop();
        root.detachAppender(this);
    }
}
