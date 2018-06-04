package clients.impl;

import clients.Client;
import configs.ConnectionConfig;
import configs.HttpConfig;
import exceptions.ClientExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import utils.JSONObjectMapper;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class HttpClient extends Client {

    private CloseableHttpClient closeableHttpClient;
    private HttpConfig httpConfig;

    private static final ExecutorService executorServicePool = Executors.newFixedThreadPool(1);

    public HttpClient(ConnectionConfig connectionConfig) {
        super(connectionConfig);
    }

    @Override
    protected void init(ConnectionConfig connectionConfig) {
        this.httpConfig = (HttpConfig) connectionConfig;
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(httpConfig.getMaxTotal());
        connectionManager.setDefaultMaxPerRoute(httpConfig.getMaxPerHost());
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(httpConfig.getSocketTimeout())
                .setConnectTimeout(httpConfig.getConnectionTimeout())
                .setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout())
                .build();
        this.closeableHttpClient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(connectionManager).build();
    }

    @Override
    protected void tearDown() {
        try {
            closeableHttpClient.close();
        } catch (Exception e) {
            log.error("Error while closing http client", e);
        }
    }

    public <T> T doPost(Class<T> responseType, String path, Map<String, String> headerMap, Object payload) throws ClientExecutionException {
        T httpResponse;
        try {
            HttpPost httpPost = new HttpPost(getUri(path));
            for (String key : headerMap.keySet()) {
                httpPost.setHeader(new BasicHeader(key, headerMap.get(key)));
            }
            StringEntity stringEntity = new StringEntity(JSONObjectMapper.INSTANCE.getMapper()
                    .writeValueAsString(payload), StandardCharsets.UTF_8.toString());
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            httpResponse = execute(responseType, httpPost);
        } catch (Exception e) {
            log.error("Error while preparing http client request: Payload: {} ", payload.toString(), e);
            throw new ClientExecutionException("Error while making http post request", e);
        }
        return httpResponse;
    }

    public <T> T doGet(Class<T> responseType, String path, Map<String, String> headerMap) throws ClientExecutionException {
        T httpResponse;
        try {
            HttpGet httpGet = new HttpGet(getUri(path));
            for (String key : headerMap.keySet()) {
                httpGet.setHeader(new BasicHeader(key, headerMap.get(key)));
            }
            httpResponse = execute(responseType, httpGet);
        } catch (Exception e) {
            log.error("Error while preparing http client request", e);
            throw new ClientExecutionException("Error while making http get request", e);
        }
        return httpResponse;
    }

    private URI getUri(String path) throws URISyntaxException {
        return new URIBuilder().setScheme("http")
                .setHost(httpConfig.getHost())
                .setPort(httpConfig.getPort())
                .setPath(path)
                .setParameter("keys", "test")
                .build();
    }

    private <T> T execute(Class<T> responseType, HttpRequestBase httpRequest) throws ClientExecutionException {
        try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpRequest)) {
            InputStream content = closeableHttpResponse.getEntity().getContent();
            return JSONObjectMapper.INSTANCE.getMapper()
                    .readValue(content, responseType);
        } catch (Exception e) {
            log.error("Error while executing http {} request", httpRequest.getMethod(), e);
            throw new ClientExecutionException("Error while executing http request", e);
        }
    }
}
