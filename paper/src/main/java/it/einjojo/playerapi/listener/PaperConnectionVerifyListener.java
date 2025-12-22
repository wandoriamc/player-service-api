package it.einjojo.playerapi.listener;

import it.einjojo.protocol.player.GetOnlinePlayerResponse;
import it.einjojo.protocol.player.OnlinePlayerDefinition;
import it.einjojo.protocol.player.PlayerIdRequest;
import it.einjojo.protocol.player.PlayerServiceGrpc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.UUID;

/**
 * <p>On login, the session is verified against the player-service. </p>
 * <p>When an instance of Player is available, it writes some session-scoped metadata to the player object</p>
 *
 */
public class PaperConnectionVerifyListener implements Listener {
    private static final String SESSION_ID_METADATA_KEY = "session_id";
    private static final String LOGIN_MILLIS_METADATA_KEY = "login_millis";
    private static final String PROXY_METADATA_KEY = "proxy";
    private static final int WRITER_TTL = 2000;

    private final Logger logger;
    private final PlayerServiceGrpc.PlayerServiceBlockingV2Stub blockingService;
    private final LinkedList<MetadataWriter> metaWriterQueue = new LinkedList<>();
    private final JavaPlugin plugin;

    /**
     * Constructor for PaperConnectionVerifyListener.
     *
     * @param logger          logger
     * @param blockingService session verify
     * @param plugin          metadata ownership
     */
    public PaperConnectionVerifyListener(Logger logger, PlayerServiceGrpc.PlayerServiceBlockingV2Stub blockingService, JavaPlugin plugin) {
        this.logger = logger;
        this.blockingService = blockingService;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void preLoginSessionVerify(AsyncPlayerPreLoginEvent event) {
        try {
            GetOnlinePlayerResponse response = blockingService.getOnlinePlayerByUniqueId(PlayerIdRequest.newBuilder().setUniqueId(event.getUniqueId().toString()).build());
            OnlinePlayerDefinition onlinePlayerDefinition = response.getPlayer();
            // add to queue to be processed at the next stage where an instance of bukkit Player is available
            metaWriterQueue.add(new MetadataWriter(onlinePlayerDefinition, System.currentTimeMillis() + WRITER_TTL, plugin));
        } catch (Exception ex) {
            String refId = UUID.randomUUID().toString().split("-")[0];
            logger.error("Ref:{} Could not verify session for {}", refId, event.getUniqueId(), ex);
            event.kickMessage(Component.text("player-service verification failure: No session found ", NamedTextColor.RED).append(Component.text(refId, NamedTextColor.GRAY)));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void writeMetadata(PlayerJoinEvent event) {
        // take the fetched meta data from prelogin event and apply it to the player
        for (int i = 0; i < metaWriterQueue.size(); i++) {
            MetadataWriter writer = metaWriterQueue.poll();
            if (writer == null) {
                logger.warn("No meta setter found for player {}", event.getPlayer().getUniqueId());
                return;
            }
            if (writer.canApply(event.getPlayer())) {
                writer.apply(event.getPlayer());
                return;
            } else if (!writer.isExpired()) {
                metaWriterQueue.push(writer); // put back to the queue because it belongs another player
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void cleanMetadata(PlayerQuitEvent event) {
        event.getPlayer().removeMetadata(SESSION_ID_METADATA_KEY, plugin);
        event.getPlayer().removeMetadata(LOGIN_MILLIS_METADATA_KEY, plugin);
        event.getPlayer().removeMetadata(PROXY_METADATA_KEY, plugin);
    }


    /**
     * Writes to the bukkit player meta some constants that can be used without directly requiring the player-api.
     *
     * @param onlinePlayerDefinition definition
     */
    private record MetadataWriter(OnlinePlayerDefinition onlinePlayerDefinition, long expiry, JavaPlugin metaOwner) {

        public boolean isExpired() {
            return expiry < System.currentTimeMillis();
        }

        public boolean canApply(@NotNull Player player) {
            return player.getUniqueId().toString().equals(onlinePlayerDefinition.getUniqueId());
        }

        public void apply(@NotNull Player player) {
            player.setMetadata(SESSION_ID_METADATA_KEY, new FixedMetadataValue(metaOwner, onlinePlayerDefinition.getSessionId()));
            player.setMetadata(LOGIN_MILLIS_METADATA_KEY, new FixedMetadataValue(metaOwner, onlinePlayerDefinition.getLastLogin() * 1000));
            player.setMetadata(PROXY_METADATA_KEY, new FixedMetadataValue(metaOwner, onlinePlayerDefinition.getConnectedProxyName()));
        }

    }

}
