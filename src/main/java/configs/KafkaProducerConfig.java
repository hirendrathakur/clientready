package configs;

import enums.ClientTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KafkaProducerConfig implements ConnectionConfig {
    private String brokerConnectionString;
    private String compressionType;
    private int producersCount;
    private int requestTimeout;
    private int maxBlockMS;
    private int maxIdleTime;
    private int batchSize;
    private int lingerTimeInMs;
    private int retry;
    private int retryBackoffMs;
    private String topicName;
    private int maxBytesInBuffer;
    private String ackConfig;
    private String keySerializerClass = "org.apache.kafka.common.serialization.ByteArraySerializer";
    private String valueSerializerClass = "org.apache.kafka.common.serialization.ByteArraySerializer";

    @Override
    public ClientTypes getClientType() {
        return ClientTypes.kafka;
    }
}
