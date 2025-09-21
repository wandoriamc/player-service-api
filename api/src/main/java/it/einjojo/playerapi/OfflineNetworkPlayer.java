package it.einjojo.playerapi;

import java.util.UUID;

public interface OfflineNetworkPlayer {

    UUID getUniqueId();

    String getName();

    boolean isOnline();

    long getFirstPlayed();

    long getLastPlayed();

    long getPlaytime();


}
