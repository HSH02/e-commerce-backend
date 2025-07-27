package com.ecommerce.global.aop.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogTracer {

    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";

    private final ThreadLocal<Integer> levelHolder = new ThreadLocal<>();

    public TraceStatus begin(String message) {
        syncLevel();
        Integer level = levelHolder.get();
        long startTimeMs = System.currentTimeMillis();
        log.info("{}{}", addSpace(START_PREFIX, level), message);
        return new TraceStatus(message, startTimeMs);
    }

    public void end(TraceStatus status) {
        complete(status, null);
    }

    public void exception(TraceStatus status, Exception e) {
        complete(status, e);
    }

    private void complete(TraceStatus status, Exception e) {
        long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.startTimeMs();
        double resultTimeSec = resultTimeMs / 1000.0;
        Integer level = levelHolder.get();

        if (e == null) {
            log.info("{}{} time={}s", addSpace(COMPLETE_PREFIX, level), status.message(), String.format("%.3f", resultTimeSec));
        } else {
            log.error("{}{} time={}s ex={}", addSpace(EX_PREFIX, level), status.message(), String.format("%.3f", resultTimeSec), e.toString());
        }

        releaseLevel();
    }

    private void syncLevel() {
        Integer level = levelHolder.get();
        if (level == null) {
            levelHolder.set(0);
        } else {
            levelHolder.set(level + 1);
        }
    }

    private void releaseLevel() {
        Integer level = levelHolder.get();
        if (level == null || level <= 0) {
            levelHolder.remove();
        } else {
            levelHolder.set(level - 1);
        }
    }

    private String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        if (level > 0) {
            sb.append("   ".repeat(level));
            sb.append("|");
        }
        sb.append(prefix);
        return sb.toString();
    }
}