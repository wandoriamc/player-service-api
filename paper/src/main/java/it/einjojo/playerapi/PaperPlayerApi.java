package it.einjojo.playerapi;

import io.grpc.ManagedChannel;
import it.einjojo.playerapi.config.RedisConnectionConfiguration;
import it.einjojo.playerapi.impl.AbstractPlayerApi;
import it.einjojo.playerapi.util.PluginMessagePlayerConnector;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Can be obtained by {@link PlayerApiProvider} on paper servers.
 */
public class PaperPlayerApi extends AbstractPlayerApi {
    private final LocalOnlinePlayerAccessor localOnlinePlayerAccessor;
    private final RedisPubSubHandler redisPubSubHandler;


    /**
     * Constructor for AbstractPlayerApi.
     *
     * @param channel  the gRPC channel to communicate with the player service
     * @param executor the executor to run the callbacks on
     */
    public PaperPlayerApi(ManagedChannel channel, Executor executor, RedisConnectionConfiguration redisConnectionConfiguration) {
        super(channel, executor);
        this.localOnlinePlayerAccessor = new PaperLocalPlayerAccessor();
        this.redisPubSubHandler = new RedisPubSubHandler(redisConnectionConfiguration);

    }

    @Override
    public LocalOnlinePlayerAccessor getLocalOnlinePlayerAccessor() {
        return localOnlinePlayerAccessor;
    }

    @Override
    protected RedisPubSubHandler getRedisPubSubHandler() {
        return redisPubSubHandler;
    }

    @Override
    public void connectPlayerToServer(UUID uuid, String serviceName) {
        PluginMessagePlayerConnector.getInstance().connectPlayerToServer(uuid, serviceName);
    }
}
