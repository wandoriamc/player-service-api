package it.einjojo.playerapi.listener;

import com.google.common.util.concurrent.ListenableFuture;
import it.einjojo.playerapi.impl.AbstractPlayerApi;
import it.einjojo.protocol.player.LoginNotify;
import it.einjojo.protocol.player.LoginRequest;
import it.einjojo.protocol.player.LogoutRequest;
import it.einjojo.protocol.player.UpdateConnectionRequest;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * When the server is not running on a proxied system, the paper server should log in and log out the player.
 * This is useful for single testing servers, which require the plugin as a dependency.
 */
public class PaperConnectionHandler implements Listener {
    private static final Logger log = LoggerFactory.getLogger(PaperConnectionHandler.class);
    private final MiniMessage miniMessage;
    private final AbstractPlayerApi playerApi;
    private final Executor executor;
    private final String serverName;

    /**
     * Constructor for PaperConnectionHandler.
     *
     * @param plugin     register events
     * @param playerApi  api for login / logout operations
     * @param executor   for grpc future callbacks
     * @param serverName the server name where the player should be connected to.
     */
    public PaperConnectionHandler(JavaPlugin plugin,
                                  AbstractPlayerApi playerApi,
                                  Executor executor,
                                  String serverName) {
        this.playerApi = playerApi;
        this.executor = executor;
        this.serverName = serverName;
        this.miniMessage = MiniMessage.builder()
                .editTags(builder -> {
                    builder.tag("prefix", Tag.inserting(MiniMessage.miniMessage().deserialize("<#818cf8>ᴘʟᴀʏᴇʀᴀᴘɪ <#eef2ff>")));
                })
                .build();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void loginPlayer(PlayerJoinEvent event) {
        event.getPlayer().sendActionBar(miniMessage.deserialize("<prefix>Logging in..."));
        String skinTexture = "";
        String skinSignature = "";
        for (var property : event.getPlayer().getPlayerProfile().getProperties()) {
            if (property.getName().equals("textures")) {
                skinTexture = property.getValue();
                skinSignature = property.getSignature();
                break;
            }
        }
        ListenableFuture<LoginNotify> loginFuture = playerApi.playerServiceStub.login(LoginRequest.newBuilder()
                .setProxyName("-")
                .setUsername(event.getPlayer().getName())
                .setUniqueId(event.getPlayer().getUniqueId().toString())
                .setSkinTexture(skinTexture)
                .setSkinSignature(skinSignature)
                .build());
        loginFuture.addListener(() -> updateServer(event.getPlayer()), executor);
    }

    private void updateServer(Player player) {
        log.info("Successfully logged in player {}", player.getName());
        player.sendActionBar(miniMessage.deserialize("<prefix>Angemeldet! Setze Server auf " + serverName + "..."));
        var future = playerApi.playerServiceStub.updateConnection(UpdateConnectionRequest.newBuilder()
                .setUniqueId(player.getUniqueId().toString())
                .setConnectedServerName(serverName)
                .build());
        future.addListener(() -> player.sendActionBar(miniMessage.deserialize("<prefix>Du bist jetzt online auf " + serverName)), executor);
    }

    @EventHandler
    private void logoutPlayer(PlayerQuitEvent event) {
        playerApi.playerServiceStub.logout(LogoutRequest.newBuilder()
                .setUniqueId(event.getPlayer().getUniqueId().toString())
                .build()).addListener(() -> log.info("Successfully logged out player {}", event.getPlayer().getName()), executor);
    }

}
