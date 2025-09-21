package it.einjojo.playerapi;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.einjojo.playerapi.LocalOnlinePlayerAccessor;

import java.util.Collection;
import java.util.UUID;

public class VelocityLocalPlayerAccessor implements LocalOnlinePlayerAccessor {
    private final ProxyServer server;

    public VelocityLocalPlayerAccessor(ProxyServer server) {
        this.server = server;
    }


    @Override
    public boolean isOnline(UUID uuid) {
        return server.getPlayer(uuid).isPresent();
    }

    @Override
    public boolean isOnline(String name) {
        return server.getPlayer(name).isPresent();
    }

    @Override
    public Collection<String> getPlayerNames() {
        return server.getAllPlayers().stream()
                .map(Player::getUsername)
                .toList();

    }
}
