package clients;

import configs.ConnectionConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Client {

    public Client(ConnectionConfig connectionConfig) {
        try {
            init(connectionConfig);
            Runtime.getRuntime().addShutdownHook(new Thread(this::tearDown));
        } catch (Exception e) {
            log.error("Error while initialising {}", this.getClass().getSimpleName());
        }
    }

    protected abstract void init(ConnectionConfig connectionConfig);

    protected abstract void tearDown();

}
