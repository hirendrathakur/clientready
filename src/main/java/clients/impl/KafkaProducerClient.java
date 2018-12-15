package clients.impl;

import clients.Client;
import configs.ConnectionConfig;
import configs.KafkaProducerConfig;
import exceptions.KafkaProducerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.record.CompressionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class KafkaProducerClient extends Client {

    private LinkedBlockingQueue<Producer<byte[], byte[]>> producers;

    public KafkaProducerClient(ConnectionConfig connectionConfig) {
        super(connectionConfig);
    }

    @Override
    protected void init(ConnectionConfig connectionConfig) {
        if (connectionConfig instanceof KafkaProducerConfig) {
            KafkaProducerConfig kafkaConfig = (KafkaProducerConfig) connectionConfig;
            log.info("Received KafkaProducerConfig " + connectionConfig);
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokerConnectionString());
            props.put(ProducerConfig.ACKS_CONFIG, kafkaConfig.getAckConfig());
            props.put(ProducerConfig.RETRIES_CONFIG, kafkaConfig.getRetry());
            props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, kafkaConfig.getRetryBackoffMs());
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaConfig.getBatchSize());
            props.put(ProducerConfig.LINGER_MS_CONFIG, kafkaConfig.getLingerTimeInMs());
            props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaConfig.getRequestTimeout());
            props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, kafkaConfig.getMaxBlockMS());
            props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, kafkaConfig.getMaxIdleTime());
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaConfig.getMaxBytesInBuffer());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaConfig.getKeySerializerClass());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaConfig.getValueSerializerClass());
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.forName(kafkaConfig.getCompressionType()).name);
            log.info("Received KafkaProducerConfig producersCount : " + kafkaConfig.getProducersCount());
            producers = new LinkedBlockingQueue<>(kafkaConfig.getProducersCount());
            for (int i = 0; i < kafkaConfig.getProducersCount(); i++) {
                producers.add(new KafkaProducer<>(props));
            }
        } else {
            log.error("Wrong configs provided");
        }
    }

    @Override
    protected void tearDown() {
        try {
            producers.forEach(Producer::close);
        } catch (Exception e) {
            log.error("Error closing kafka producer client");
        }
    }

    private Producer<byte[], byte[]> getProducer() throws Exception {
        try {
            return producers.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Error getting producer");
            throw e;
        }
    }

    private void returnProducer(Producer<byte[], byte[]> producer) throws Exception {
        try {
            producers.put(producer);
        } catch (InterruptedException e) {
            log.error("Error returning producer");
            throw e;
        }
    }

    public void sendToKafkaSync(List<ProducerRecord<byte[], byte[]>> producerRecords) throws KafkaProducerException {
        try {
            Producer<byte[], byte[]> producer = getProducer();
            try {
                List<Future<RecordMetadata>> futureList = producerRecords.stream().map(producer::send).collect(Collectors.toList());
                List<ProducerRecord<byte[], byte[]>> failedRecords = new ArrayList<>();
                int size = producerRecords.size();
                for (int i = 0; i < size; i++) {
                    try {
                        futureList.get(i).get();
                    } catch (InterruptedException | ExecutionException e) {
                        failedRecords.add(producerRecords.get(i));
                    }
                }

                if (!failedRecords.isEmpty()) {
                    throw new KafkaProducerException("Failed records", failedRecords);
                }
            } finally {
                returnProducer(producer);
            }
        } catch (Exception e) {
            log.error("error getting producer");
            throw new KafkaProducerException("Error getting producer", e);
        }
    }

    public void sendToKafkaAsync(List<ProducerRecord<byte[], byte[]>> producerRecords) throws KafkaProducerException {
        try {
            Producer<byte[], byte[]> producer = getProducer();
            try {
                producerRecords.forEach(producer::send);
            } finally {
                returnProducer(producer);
            }
        } catch (Exception e) {
            log.error("error getting producer");
            throw new KafkaProducerException("error getting producer", e);
        }
    }
}
