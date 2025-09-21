package it.einjojo.playerapi;

import java.util.Collection;
import java.util.UUID;

/**
 * Accesses the implementation's player-container and can determine whether a player is online locally or not.
 */
public interface LocalOnlinePlayerAccessor {

    /**
     * checks whether a player is online or not.
     *
     * @param uuid the player's UUID
     * @return true if the player is online, false otherwise
     */
    boolean isOnline(UUID uuid);

    boolean isOnline(String name);

    Collection<String> getPlayerNames();

}
