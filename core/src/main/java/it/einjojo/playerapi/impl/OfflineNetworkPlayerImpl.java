package it.einjojo.playerapi.impl;

import it.einjojo.playerapi.OfflineNetworkPlayer;

import java.util.UUID;

public class OfflineNetworkPlayerImpl implements OfflineNetworkPlayer {
    private final UUID uniqueId;
    private final String name;
    private final long firstPlayed;
    private final long lastPlayed;
    private final long playtime;
    private final boolean online;

    public OfflineNetworkPlayerImpl(UUID uniqueId, String name, long firstPlayed, long lastPlayed, long playtime, boolean online) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.firstPlayed = firstPlayed;
        this.lastPlayed = lastPlayed;
        this.playtime = playtime;
        this.online = online;
    }


    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public long getFirstPlayed() {
        return firstPlayed;
    }

    @Override
    public long getLastPlayed() {
        return lastPlayed;
    }

    @Override
    public long getPlaytime() {
        return playtime;
    }



}
