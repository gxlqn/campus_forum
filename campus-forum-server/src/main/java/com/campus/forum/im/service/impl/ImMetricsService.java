package com.campus.forum.im.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;

@Service
public class ImMetricsService {

    private final MeterRegistry meterRegistry;

    public ImMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void increment(String name, String... tags) {
        meterRegistry.counter(name, tags).increment();
    }

    public void incrementWithReason(String name, String reason) {
        meterRegistry.counter(name, "reason", reason == null ? "unknown" : reason).increment();
    }

    public void distribution(String name, long value, String... tags) {
        meterRegistry.summary(name, tags).record(value);
    }

    public void duration(String name, Duration duration, String... tags) {
        if (duration == null || duration.isNegative()) {
            return;
        }
        meterRegistry.timer(name, tags).record(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void registerOnlineUsersGauge(Supplier<Number> supplier) {
        Gauge.builder("im.online.users", supplier, s -> s.get().doubleValue())
                .description("Current online IM users")
                .register(meterRegistry);
    }
}
