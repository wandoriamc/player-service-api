package it.einjojo.playerapi;

import java.io.Closeable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * PlayerAPI
 */
public interface PlayerApi {

    CompletableFuture<OfflineNetworkPlayer> getOfflinePlayer(String playerName);

    CompletableFuture<OfflineNetworkPlayer> getOfflinePlayer(UUID playerUUID);

    CompletableFuture<Boolean> isPlayerOnline(String playerName);

    CompletableFuture<Boolean> isPlayerOnline(UUID playerUUID);

    CompletableFuture<NetworkPlayer> getOnlinePlayer(String playerName);

    CompletableFuture<NetworkPlayer> getOnlinePlayer(UUID playerUUID);

    CompletableFuture<UUID> getUniqueId(String playerName);

    LocalOnlinePlayerAccessor getLocalOnlinePlayerAccessor();

    Closeable subscribeLogin(Consumer<NetworkPlayer> playerConsumer);

    Closeable subscribeLogout(Consumer<OfflineNetworkPlayer> offlinePlayerConsumer);

}
