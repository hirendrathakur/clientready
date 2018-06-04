package configs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HttpConfig extends ConnectionConfig {
    private String host;
    private int port;
    private int maxTotal = 200;
    private int maxPerHost = 50;
    private int socketTimeout = 2000;
    private int connectionTimeout = 2000;
    private int connectionRequestTimeout = 2000;
}
