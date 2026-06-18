package it.einjojo.playerapi;

import io.grpc.ManagedChannel;
import it.einjojo.playerapi.config.RedisConnectionConfiguration;
import it.einjojo.playerapi.impl.AbstractPlayerApi;
import it.einjojo.playerapi.util.SessionMetadataUtil;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Can be obtained by {@link PlayerApiProvider} on paper servers.
 */
@NullMarked
public class PaperPlayerApi extends AbstractPlayerApi {
    private final LocalOnlinePlayerAccessor localOnlinePlayerAccessor;
    private final RedisPubSubHandler redisPubSubHandler;
    private @Nullable ConnectionRequestManager connectionRequestManager;


    /**
     * Constructor for AbstractPlayerApi.
     *
     * @param channel  the gRPC channel to communicate with the player service
     * @param executor the executor to run the callbacks on
     */
    public PaperPlayerApi(ManagedChannel channel, Executor executor, RedisConnectionConfiguration redisConnectionConfiguration) {
        super(channel, executor);
        this.localOnlinePlayerAccessor = new PaperLocalPlayerAccessor();
        this.redisPubSubHandler = new RedisPubSubHandler(redisConnectionConfiguration, executor);

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
    public CompletableFuture<ServerConnectResult> connectPlayer(UUID uuid, String serviceName) {
        return resolveProxyName(uuid)
                .thenCompose(proxyName -> {
                    if (proxyName == null && !getLocalOnlinePlayerAccessor().isOnline(uuid)) {
                        return CompletableFuture.completedFuture(ServerConnectResult.PLAYER_NOT_FOUND);
                    }
                    return getConnectionRequestManager().sendWithResponse(uuid, serviceName, proxyName);
                });
    }

    public ConnectionRequestManager getConnectionRequestManager() {
        if (connectionRequestManager == null) {
            connectionRequestManager = new ConnectionRequestManager(getRedisPubSubHandler());
        }
        return connectionRequestManager;
    }

    @Override
    public void connectPlayerToServer(UUID uuid, String serviceName) {
        resolveProxyName(uuid).thenAccept(proxyName ->
                getConnectionRequestManager().sendFireAndForget(uuid, serviceName, proxyName)
        );
    }

    private CompletableFuture<@Nullable String> resolveProxyName(UUID uuid) {
        var localPlayer = Bukkit.getPlayer(uuid);
        if (localPlayer != null) {
            var metadataProxyName = SessionMetadataUtil.getProxyName(localPlayer);
            if (metadataProxyName.isPresent()) {
                return CompletableFuture.completedFuture(metadataProxyName.get());
            }
        }
        return getOnlinePlayer(uuid).thenApply(player -> player == null ? null : player.getConnectedProxyName());
    }

    @Override
    public void shutdown() {
        // Close ConnectionRequestManager if it was initialized
        if (connectionRequestManager != null) {
            connectionRequestManager.close();
        }
        // Call parent shutdown to close RedisPubSubHandler
        super.shutdown();
    }
}
