package it.einjojo.playerapi;

import java.io.Closeable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * PlayerAPI can be obtained by {@link PlayerApiProvider} on any platform.
 */
public interface PlayerApi {

    /**
     * Gets a list of all online players.
     *
     * @return a list of online players.
     */
    CompletableFuture<List<NetworkPlayer>> getOnlinePlayers();

    /**
     * Gets the names of all online players.
     *
     * @return a list of player names.
     */
    CompletableFuture<List<String>> getOnlinePlayerNames();

    /**
     * Connects a player to a server.
     *
     * @param uuid        the player's UUID
     * @param serviceName the name of the server to connect to. If the string does not contain a separator ('-'), it connects to a random server of the group.
     */
    void connectPlayerToServer(UUID uuid, String serviceName);

    CompletableFuture<OfflineNetworkPlayer> getOfflinePlayer(String playerName);

    CompletableFuture<OfflineNetworkPlayer> getOfflinePlayer(UUID playerUUID);

    CompletableFuture<Boolean> isPlayerOnline(String playerName);

    CompletableFuture<Boolean> isPlayerOnline(UUID playerUUID);

    CompletableFuture<NetworkPlayer> getOnlinePlayer(String playerName);

    CompletableFuture<NetworkPlayer> getOnlinePlayer(UUID playerUUID);

    CompletableFuture<UUID> getUniqueId(String playerName);

    LocalOnlinePlayerAccessor getLocalOnlinePlayerAccessor();

    /**
     * Subscribe to all player logins
     *
     * @param playerConsumer the consumer that will be called when a player logs in.
     * @return a {@link Closeable} that can be used to unsubscribe from the event.
     */
    Closeable subscribeLogin(Consumer<NetworkPlayer> playerConsumer);

    /**
     * Subscribe to all player logouts
     *
     * @param offlinePlayerConsumer the consumer that will be called when a player logs out.
     * @return a {@link Closeable} that can be used to unsubscribe from the event.
     */
    Closeable subscribeLogout(Consumer<OfflineNetworkPlayer> offlinePlayerConsumer);

}
