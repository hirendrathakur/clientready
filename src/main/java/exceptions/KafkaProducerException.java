package exceptions;

import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;

public class KafkaProducerException extends Exception {

    private List<ProducerRecord<byte[], byte[]>> failedRecords;

    public KafkaProducerException(String message, Throwable cause, List<ProducerRecord<byte[], byte[]>> failedRecords) {
        super(message, cause);
        this.failedRecords = failedRecords;
    }

    public KafkaProducerException(String message, List<ProducerRecord<byte[], byte[]>> failedRecords) {
        super(message);
        this.failedRecords = failedRecords;
    }

    public KafkaProducerException(String message, Throwable cause) {
        super(message, cause);
    }
}
