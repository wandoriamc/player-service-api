package it.einjojo.playerapi.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import it.einjojo.playerapi.NetworkPlayer;
import it.einjojo.playerapi.VelocityPlayerApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ConnectionListener {
    private final VelocityPlayerApi playerApi;
    private final Logger logger;

    public ConnectionListener(VelocityPlayerApi playerApi, Logger logger) {
        this.playerApi = playerApi;
        this.logger = logger;
    }

    @Subscribe
    public void handleLogin(PostLoginEvent event) {
        var player = event.getPlayer();
        try {
            NetworkPlayer springPlayer = playerApi.handleLogin(player).get(3, TimeUnit.SECONDS);
            logger.info("Successfully handled login for player {}: {}", player.getUsername(), springPlayer);
        } catch (Exception e) {
            String refId = UUID.randomUUID().toString().split("-")[0];
            logger.error("REF:{} Failed to handle login for player {}: {}", refId, player.getUsername(), e.getMessage(), e);
            event.getPlayer().disconnect(Component.text("login to player service failed", NamedTextColor.RED).appendNewline().append(Component.text(refId, NamedTextColor.GRAY)));

        }
    }

    @Subscribe
    public void handleDisconnect(DisconnectEvent event) {
        final String playerName = event.getPlayer().getUsername();
        playerApi.handleLogout(event.getPlayer()).thenAccept(success -> {
            if (success) {
                logger.info("Successfully handled logout for player {}", playerName);
            } else {
                logger.warn("Logout for player {} was not successful", playerName);
            }
        }).exceptionally(e -> {
            logger.error("Failed to handle logout for player {}: {}", playerName, e.getMessage(), e);
            return null;
        });
    }

    @Subscribe
    public void changeServer(ServerPostConnectEvent event) {
        playerApi.handleServerChange(event.getPlayer()).thenAccept(success -> {
            if (success) {
                logger.info("Successfully handled server change for player {}", event.getPlayer().getUsername());
            } else {
                event.getPlayer().disconnect(Component.text("server change to player service failed", NamedTextColor.RED));
                logger.warn("Server change for player {} was not successful", event.getPlayer().getUsername());
            }
        }).exceptionally(e -> {
            String refId = UUID.randomUUID().toString().split("-")[0];
            logger.error("REF:{} Failed to handle server change for player {}: {}", refId, event.getPlayer().getUsername(), e.getMessage(), e);
            event.getPlayer().disconnect(Component.text("server change to player service failed exceptionally", NamedTextColor.RED).appendNewline().append(Component.text(refId, NamedTextColor.GRAY)));
            return null;
        });
    }
}
