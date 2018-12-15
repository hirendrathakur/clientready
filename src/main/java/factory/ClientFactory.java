package factory;

import clients.Client;
import clients.impl.HttpClient;
import clients.impl.KafkaProducerClient;
import configs.ConnectionConfig;
import configs.HttpConfig;
import configs.KafkaProducerConfig;
import enums.ClientTypes;
import exceptions.ClientCreateException;
import exceptions.ClientFactoryException;

public class ClientFactory {

    private static final Object HTTP_LOCK = new Object();
    private static final Object KAFKA_LOCK = new Object();

    private static HttpClient httpClient;
    private static KafkaProducerClient kafkaProducerClient;

    public static Client getClient(ConnectionConfig connectionConfig) throws ClientFactoryException {
        try {
            switch (connectionConfig.getClientType()) {
                case http:
                    return getHttpClient((HttpConfig) connectionConfig);
                case kafka:
                    return getKafkaProducerClient((KafkaProducerConfig) connectionConfig);
                default:
                    throw new ClientFactoryException("Error getting client", ClientFactoryException.ErrorCode.CLIENT_NOT_FOUND);
            }
        } catch (Throwable e) {
            throw new ClientFactoryException("Error getting client", e, ClientFactoryException.ErrorCode.GET_CLIENT_FAILURE);
        }
    }

    private static HttpClient getHttpClient(HttpConfig httpConfig) {
        if (null == httpClient) {
            synchronized (HTTP_LOCK) {
                if (null == httpClient) {
                    httpClient = new HttpClient(httpConfig);
                }
            }
        }
        return httpClient;
    }

    private static KafkaProducerClient getKafkaProducerClient(KafkaProducerConfig kafkaProducerConfig) {
        if (null == kafkaProducerClient) {
            synchronized (KAFKA_LOCK) {
                if (null == kafkaProducerClient) {
                    kafkaProducerClient = new KafkaProducerClient(kafkaProducerConfig);
                }
            }
        }
        return kafkaProducerClient;
    }
}
