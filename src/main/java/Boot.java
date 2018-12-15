import clients.impl.HttpClient;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Timer;
import configs.HttpConfig;
import factory.ClientFactory;
import lombok.extern.slf4j.Slf4j;
import models.CosmosResponse;
import org.apache.commons.cli.*;
import utils.JSONObjectMapper;
import utils.MetricsRegistry;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Boot {

    private static ExecutorService executorServicePool;
    private static Map<String, String> headerMap = new HashMap<>();
    private static final Lock lock = new ReentrantLock();

    public static void main(String[] args) throws Exception {
        log.info("Starting");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(getOptions(), args);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        String configPath = cmd.getOptionValue("configPath");
        int threadCount = Integer.parseInt(cmd.getOptionValue('t'));

        headerMap.put("x-api-key", "r9qA4fF2prQ7qgX0O9NSdFl6A3q50QxB");
        HttpConfig httpConfig = JSONObjectMapper.INSTANCE.getYamlMapper().readValue(new File(configPath), HttpConfig.class);
        executorServicePool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorServicePool.submit(new HttpClientThread(httpConfig));
        }
        startReport();
        Runtime.getRuntime().addShutdownHook(new ShutDownThread());
        //awaitShutdown();
    }

    private static Options getOptions() {
        Options opts = new Options();
        opts.addRequiredOption("configPath", "Config Path", true,
                "Path containing yaml file of the configs");
        opts.addOption("t", "thread count ", true, "Thread Count");
        return opts;
    }

    private static class HttpClientThread implements Runnable {

        private HttpConfig httpConfig;
        private static final String path = "/productService/getCustomProductDetails";

        HttpClientThread(HttpConfig httpConfig) {
            this.httpConfig = httpConfig;
        }

        @Override
        public void run() {
            HttpClient httpClient = (HttpClient) ClientFactory.getClient(httpConfig);
            String payload = "{\n" +
                    "    \"channelContext\": {\n" +
                    "        \"fkApp\": {\n" +
                    "            \"type\": \"Retail\"\n" +
                    "        }\n" +
                    "    },\n" +
                    "    \"ids\": [\n" +
                    "        \"ACCEZT6VN9GZNAVK\"\n" +
                    "    ],\n" +
                    "    \"locationContext\": {},\n" +
                    "    \"serviceContext\": {\n" +
                    "        \"npsServices\": [\n" +
                    "            \"PNP_LITE\",\n" +
                    "            \"ATHENA\"\n" +
                    "        ],\n" +
                    "        \"npsViews\": [\n" +
                    "            \"LISTING_INFO\"\n" +
                    "        ],\n" +
                    "        \"orderListings\": true,\n" +
                    "        \"preferredListingIds\": {\n" +
                    "            \"ACCEZT6VN9GZNAVK\": \"LSTACCEZT6VN9GZNAVK7PNXWH\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";


            while (true) {
                try (Timer.Context context = MetricsRegistry.timerContext(this.getClass(), "post")) {
                    httpClient.doPost(path, new HashMap<>(), JSONObjectMapper.INSTANCE.getMapper().readValue(payload, Map.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static class ShutDownThread extends Thread {
        @Override
        public void run() {
            executorServicePool.shutdown();
            try {
                executorServicePool.awaitTermination(2, TimeUnit.SECONDS);
                executorServicePool.shutdownNow();
            } catch (InterruptedException e) {
                //Re-cancelling since current thread was also interrupted
                executorServicePool.shutdownNow();
                // Interrupted while interrupting; Giving up.
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    private static void awaitShutdown() {
        lock.lock();
        lock.newCondition().awaitUninterruptibly();
    }

    private static void startReport() {
        ConsoleReporter reporter;
        reporter = ConsoleReporter.forRegistry(MetricsRegistry.INSTANCE.getRegistry())
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(2, TimeUnit.SECONDS);
    }
}
