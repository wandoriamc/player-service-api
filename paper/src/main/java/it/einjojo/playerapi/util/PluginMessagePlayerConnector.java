package it.einjojo.playerapi.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.einjojo.playerapi.PaperPlayerApiProviderPlugin;
import it.einjojo.playerapi.PlayerApiProvider;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PluginMessagePlayerConnector {
    private static final PluginMessagePlayerConnector INSTANCE = new PluginMessagePlayerConnector();
    private static final Logger log = LoggerFactory.getLogger(PluginMessagePlayerConnector.class);

    public static PluginMessagePlayerConnector getInstance() {
        return INSTANCE;
    }


    public void connectPlayerToServer(UUID uuid, String serviceName) {
        PlayerApiProvider.getInstance().getOnlinePlayer(uuid).thenAccept(player -> {
            Bukkit.getScheduler().runTask(PaperPlayerApiProviderPlugin.INSTANCE, () -> {
                var iterator = Bukkit.getOnlinePlayers().iterator();
                if (!iterator.hasNext()) {
                    log.warn("No players online which can be used for plugin messaging. Cannot connect player {} to service {} ", uuid, serviceName);
                    return;
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(player.getName());
                out.writeUTF(serviceName);
                iterator.next().sendPluginMessage(PaperPlayerApiProviderPlugin.INSTANCE, "BungeeCord", out.toByteArray());
            });
        }).exceptionally(ex -> {
            log.error("Failed to connect player {} to service {}", uuid, serviceName, ex);
            return null;
        });
    }
}
