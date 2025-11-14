package it.einjojo.playerapi;

import io.lettuce.core.api.StatefulRedisConnection;
import it.einjojo.protocol.player.ConnectRequest;
import it.einjojo.protocol.player.ConnectResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@NullMarked
public class ConnectionRequestManager implements Consumer<ConnectResponse> {
    private static final SecureRandom random = new SecureRandom();
    private static final Logger log = LoggerFactory.getLogger(ConnectionRequestManager.class);
    private final StatefulRedisConnection<byte[], byte[]> connection;
    private final Map<Integer, CompletableFuture<ServerConnectResult>> pendingRequests = new ConcurrentHashMap<>();


    public ConnectionRequestManager(RedisPubSubHandler pubSubHandler) {
        pubSubHandler.setConnectResponseConsumer(this);
        this.connection = Objects.requireNonNull(pubSubHandler.getConnection());
    }

    /**
     * Callback invoked when a connection response is received.
     *
     * @param connectResponse the response to a connection request
     */
    @Override
    public void accept(ConnectResponse connectResponse) {
        var future = pendingRequests.get(connectResponse.getResponseKey());
        if (future == null) {
            log.warn("No future found for response key {}", connectResponse.getResponseKey());
            return;
        }

        ServerConnectResult res = switch (connectResponse.getResult()) {
            case SUCCESS -> ServerConnectResult.SUCCESS;
            case PLAYER_NOT_FOUND -> ServerConnectResult.PLAYER_NOT_FOUND;
            case SERVER_NOT_FOUND -> ServerConnectResult.SERVER_NOT_FOUND;
            case CONNECTION_ERROR, UNRECOGNIZED -> ServerConnectResult.ERROR;
        };
        future.complete(res);
    }

    public void fireAndForget(UUID uuid, String serviceName) {
        connection.sync().publish(RedisPubSubHandler.CONNECT_REQ_CHANNEL, ConnectRequest.newBuilder()
                .setUniqueId(uuid.toString())
                .setServerName(serviceName)
                .build().toByteArray());
    }

    public CompletableFuture<ServerConnectResult> newRequest(UUID uuid, String serviceName) {
        int responseKey = random.nextInt(Integer.MAX_VALUE);
        CompletableFuture<ServerConnectResult> future = new CompletableFuture<>();
        pendingRequests.put(responseKey, future);
        connection.sync().publish(RedisPubSubHandler.CONNECT_REQ_CHANNEL, ConnectRequest.newBuilder()
                .setUniqueId(uuid.toString())
                .setServerName(serviceName)
                .setResponseKey(responseKey)
                .build().toByteArray());
        return future.orTimeout(10, TimeUnit.SECONDS).whenComplete((result, throwable) -> pendingRequests.remove(responseKey));
    }
}
