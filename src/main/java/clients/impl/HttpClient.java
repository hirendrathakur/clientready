package clients.impl;

import clients.Client;
import configs.ConnectionConfig;
import configs.HttpConfig;
import exceptions.HttpExecutionException;
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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class HttpClient extends Client {

    private CloseableHttpClient closeableHttpClient;
    private HttpConfig httpConfig;

    private static final Object POOL_LOCK = new Object();

    private static ExecutorService executorServicePool;

    private ExecutorService getExecutorServicePool() {
        if (null == executorServicePool) {
            synchronized (POOL_LOCK) {
                if (null == executorServicePool) {
                    executorServicePool = Executors.newFixedThreadPool(1);
                }
            }
        }
        return executorServicePool;
    }

    public HttpClient(HttpConfig httpConfig) {
        super(httpConfig);
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

    public <T> T doPost(Class<T> responseType, String path, Map<String, String> headerMap, Object payload) throws
            HttpExecutionException {
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
            throw new HttpExecutionException("Error while making http post request", e);
        }
        return httpResponse;
    }

    public Boolean doPost(String path, Map<String, String> headerMap, Object payload) throws
            HttpExecutionException {
        try {
            HttpPost httpPost = new HttpPost(getUri(path));
            for (String key : headerMap.keySet()) {
                httpPost.setHeader(new BasicHeader(key, headerMap.get(key)));
            }
            StringEntity stringEntity = new StringEntity(JSONObjectMapper.INSTANCE.getMapper()
                    .writeValueAsString(payload), StandardCharsets.UTF_8.toString());
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            execute(httpPost);
            return true;
        } catch (Exception e) {
            log.error("Error while preparing http client request: Payload: {} ", payload.toString(), e);
            throw new HttpExecutionException("Error while making http post request", e);
        }
    }

    public <T> Future<T> doPostAsync(Class<T> responseType, String path, Map<String, String> headerMap, Object payload) throws
            HttpExecutionException {
        return getExecutorServicePool().submit(() -> doPost(responseType, path, headerMap, payload));
    }

    public Future<Boolean> doPostAsync(String path, Map<String, String> headerMap, Object payload) throws
            HttpExecutionException {
        return getExecutorServicePool().submit(() -> doPost(path, headerMap, payload));
    }

    public <T> T doGet(Class<T> responseType, String path, Map<String, String> headerMap)
            throws HttpExecutionException {
        T httpResponse;
        try {
            HttpGet httpGet = new HttpGet(getUri(path));
            if (!headerMap.isEmpty()) {
                for (String key : headerMap.keySet()) {
                    httpGet.setHeader(new BasicHeader(key, headerMap.get(key)));
                }
            }
            httpResponse = execute(responseType, httpGet);
        } catch (Exception e) {
            log.error("Error while preparing http client request", e);
            throw new HttpExecutionException("Error while making http get request", e);
        }
        return httpResponse;
    }

    public <T> T doGet(Class<T> responseType, URI uri, Map<String, String> headerMap) throws HttpExecutionException {
        T httpResponse;
        try {
            HttpGet httpGet = new HttpGet(uri);
            if (!headerMap.isEmpty()) {
                for (String key : headerMap.keySet()) {
                    httpGet.setHeader(new BasicHeader(key, headerMap.get(key)));
                }
            }
            httpResponse = execute(responseType, httpGet);
        } catch (Exception e) {
            log.error("Error while preparing http client request", e);
            throw new HttpExecutionException("Error while making http get request", e);
        }
        return httpResponse;
    }

    public <T> T doGet(Class<T> responseType, String path, Map<String, String> queryParamsMap,
                       Map<String, String> headerMap) throws HttpExecutionException {
        T httpResponse;
        try {
            HttpGet httpGet = new HttpGet(getUri(path, queryParamsMap));
            if (!headerMap.isEmpty()) {
                for (String key : headerMap.keySet()) {
                    httpGet.setHeader(new BasicHeader(key, headerMap.get(key)));
                }
            }
            httpResponse = execute(responseType, httpGet);
        } catch (Exception e) {
            log.error("Error while preparing http client request", e);
            throw new HttpExecutionException("Error while making http get request", e);
        }
        return httpResponse;
    }

    public <T> T doGet(Class<T> responseType, String path) throws HttpExecutionException {
        T httpResponse;
        try {
            httpResponse = doGet(responseType, path, Collections.emptyMap());
        } catch (Exception e) {
            log.error("Error while preparing http client request", e);
            throw new HttpExecutionException("Error while making http get request", e);
        }
        return httpResponse;
    }

    public <T> T doGetAsync(Class<T> responseType, String path) throws HttpExecutionException {
        T httpResponse;
        try {
            httpResponse = doGet(responseType, path, Collections.emptyMap());
        } catch (Exception e) {
            log.error("Error while preparing http client request", e);
            throw new HttpExecutionException("Error while making http get request", e);
        }
        return httpResponse;
    }

    @SuppressWarnings("WeakerAccess")
    protected URI getUri(String path, Map<String, String> queryParamsMap) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder().setScheme(getScheme())
                .setHost(httpConfig.getHost())
                .setPort(httpConfig.getPort())
                .setPath(path);
        if (!queryParamsMap.isEmpty()) {
            for (String key : queryParamsMap.keySet()) {
                uriBuilder.setParameter(key, queryParamsMap.get(key));
            }
        }
        return uriBuilder.build();
    }

    private URI getUri(String path) throws URISyntaxException {
        return getUri(path, Collections.emptyMap());
    }

    private <T> T execute(Class<T> responseType, HttpRequestBase httpRequest) throws HttpExecutionException {
        try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpRequest)) {
            InputStream content = closeableHttpResponse.getEntity().getContent();
            return JSONObjectMapper.INSTANCE.getMapper()
                    .readValue(content, responseType);
        } catch (Exception e) {
            log.error("Error while executing http {} request", httpRequest.getMethod(), e);
            throw new HttpExecutionException("Error while executing http request", e);
        }
    }

    private void execute(HttpRequestBase httpRequest) throws HttpExecutionException {
        try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpRequest)) {
            InputStream content = closeableHttpResponse.getEntity().getContent();
        } catch (Exception e) {
            log.error("Error while executing http {} request", httpRequest.getMethod(), e);
            throw new HttpExecutionException("Error while executing http request", e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected String getScheme() {
        return "http";
    }
}
