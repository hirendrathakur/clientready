package factory;

import clients.Client;
import clients.impl.HttpClient;
import configs.ConnectionConfig;
import configs.HttpConfig;

public class ClientFactory {

    private static final Object HTTP_LOCK = new Object();

    private static HttpClient httpClient;

    public static Client getClient(ConnectionConfig connectionConfig) {
        if (connectionConfig instanceof HttpConfig) {
            return getHttpClient(connectionConfig);
        } else return null;
    }

    private static HttpClient getHttpClient(ConnectionConfig httpConfig) {
        if (null == httpClient) {
            synchronized (HTTP_LOCK) {
                if (null == httpClient) {
                    httpClient = new HttpClient(httpConfig);
                }
            }
        }
        return httpClient;
    }
}
