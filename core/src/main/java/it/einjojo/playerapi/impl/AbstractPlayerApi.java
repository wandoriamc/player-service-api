package it.einjojo.playerapi.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import it.einjojo.playerapi.*;
import it.einjojo.protocol.player.*;

import java.io.Closeable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Abstract implementation of the PlayerApi interface that provides common functionality for accessing player data.
 * This class uses gRPC to communicate with a player service and provides methods to retrieve player information
 */
public abstract class AbstractPlayerApi implements PlayerApi {
    protected PlayerServiceGrpc.PlayerServiceFutureStub playerServiceStub;
    protected final Executor executor;
    private static final Empty EMPTY = Empty.getDefaultInstance();

    /**
     * Constructor for AbstractPlayerApi.
     *
     * @param channel  the gRPC channel to communicate with the player service
     * @param executor the executor to run the callbacks on
     */
    public AbstractPlayerApi(ManagedChannel channel, Executor executor) {
        this.playerServiceStub = PlayerServiceGrpc.newFutureStub(channel);
        this.executor = executor;
    }

    @Override
    public CompletableFuture<List<NetworkPlayer>> getOnlinePlayers() {
        return null;
    }

    @Override
    public CompletableFuture<List<String>> getOnlinePlayerNames() {
        return null;
    }

    @Override
    public void connectPlayerToServer(UUID uuid, String serviceName) {

    }

    @Override
    public LocalOnlinePlayerAccessor getLocalOnlinePlayerAccessor() {
        return null;
    }

    @Override
    public CompletableFuture<OfflineNetworkPlayer> getOfflinePlayer(String playerName) {
        var future = playerServiceStub.getOfflinePlayerByName(PlayerNameRequest.newBuilder().setName(playerName).build());
        return createCallback(future, PlayerMapper::readOfflineResponse);
    }

    @Override
    public CompletableFuture<OfflineNetworkPlayer> getOfflinePlayer(UUID playerUUID) {
        var future = playerServiceStub.getOfflinePlayerByUniqueId(PlayerIdRequest.newBuilder().setUniqueId(playerUUID.toString()).build());
        return createCallback(future, PlayerMapper::readOfflineResponse);
    }

    @Override
    public CompletableFuture<Boolean> isPlayerOnline(String playerName) {
        if (getLocalOnlinePlayerAccessor().isOnline(playerName))
            return CompletableFuture.completedFuture(Boolean.TRUE);
        return getOfflinePlayer(playerName).thenApply(OfflineNetworkPlayer::isOnline);
    }

    @Override
    public CompletableFuture<Boolean> isPlayerOnline(UUID playerUUID) {
        if (getLocalOnlinePlayerAccessor().isOnline(playerUUID))
            return CompletableFuture.completedFuture(Boolean.TRUE);
        return getOfflinePlayer(playerUUID).thenApply(OfflineNetworkPlayer::isOnline);
    }

    @Override
    public CompletableFuture<NetworkPlayer> getOnlinePlayer(String playerName) {
        ListenableFuture<GetOnlinePlayerResponse> future = playerServiceStub.getOnlinePlayerByName(PlayerNameRequest.newBuilder().setName(playerName).build());
        return createCallback(future, PlayerMapper::readOnlineResponse);
    }

    @Override
    public CompletableFuture<NetworkPlayer> getOnlinePlayer(UUID playerUUID) {
        ListenableFuture<GetOnlinePlayerResponse> future = playerServiceStub.getOnlinePlayerByUniqueId(PlayerIdRequest.newBuilder().setUniqueId(playerUUID.toString()).build());
        return createCallback(future, PlayerMapper::readOnlineResponse);
    }

    @Override
    public CompletableFuture<UUID> getUniqueId(String playerName) {
        ListenableFuture<UniqueIdLookupResponse> future = playerServiceStub.getUniqueIdByName(PlayerNameRequest.newBuilder().setName(playerName).build());
        return createCallback(future, UniqueIdLookupResponse::getUniqueId).thenApply(UUID::fromString);
    }

    protected <Type, ResultType> CompletableFuture<Type> createCallback(ListenableFuture<ResultType> listenableFuture, Function<ResultType, Type> mapper) {
        CompletableFuture<Type> completableFuture = new CompletableFuture<>();
        listenableFuture.addListener(() -> {
            try {
                completableFuture.complete(mapper.apply(listenableFuture.get()));
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        }, executor);
        return completableFuture;
    }

    @Override
    public Closeable subscribeLogin(Consumer<NetworkPlayer> playerConsumer) {
        return getRedisPubSubHandler().subscribeLogin(((notify) -> {
            playerConsumer.accept(PlayerMapper.toLocal(notify.getPlayer()));
        }));
    }

    @Override
    public Closeable subscribeLogout(Consumer<OfflineNetworkPlayer> offlinePlayerConsumer) {
        return getRedisPubSubHandler().subscribeLogout(((notify) -> {
            offlinePlayerConsumer.accept(PlayerMapper.toLocal(notify.getPlayer()));
        }));
    }

    abstract RedisPubSubHandler getRedisPubSubHandler();
}
