package configs;

import enums.ClientTypes;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class HttpConfig implements ConnectionConfig {
    private String host;
    private int port;
    private int maxTotal = 2;
    private int maxPerHost = 2;
    private int socketTimeout = 10000;
    private int connectionTimeout = 10000;
    private int connectionRequestTimeout = 10000;

    @Override
    public ClientTypes getClientType() {
        return ClientTypes.http;
    }
}
