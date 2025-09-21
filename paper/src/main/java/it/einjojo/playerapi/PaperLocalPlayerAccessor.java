package it.einjojo.playerapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class PaperLocalPlayerAccessor implements LocalOnlinePlayerAccessor {


    @Override
    public boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    @Override
    public boolean isOnline(String name) {
        return Bukkit.getPlayer(name) != null;
    }

    @Override
    public Collection<String> getPlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
    }
}
