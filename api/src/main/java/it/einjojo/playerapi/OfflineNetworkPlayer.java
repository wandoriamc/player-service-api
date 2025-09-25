package it.einjojo.playerapi;

import java.util.UUID;

/**
 * An offline player has connected at least once to the server.
 */
public interface OfflineNetworkPlayer {


    UUID getUniqueId();

    String getName();

    boolean isOnline();

    long getFirstPlayed();

    long getLastPlayed();

    long getPlaytime();


}
