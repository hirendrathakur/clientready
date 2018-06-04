package utils;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;

public enum MetricsRegistry {
    INSTANCE;
    private MetricRegistry metricRegistry;

    MetricsRegistry() {
        metricRegistry = new MetricRegistry();
        JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        jmxReporter.start();
    }

    public MetricRegistry getRegistry() {
        return metricRegistry;
    }

    public static void markMeter(Class<?> callerClass, long count, String... names) {
        INSTANCE.getRegistry().meter(MetricRegistry.name(callerClass, names)).mark(count);
    }

    public static Timer.Context timerContext(Class<?> callerClass, String... names) {
        return INSTANCE.getRegistry().timer(MetricRegistry.name(callerClass, names)).time();
    }
}
