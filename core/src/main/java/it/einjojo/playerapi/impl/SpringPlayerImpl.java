package it.einjojo.playerapi.impl;

import it.einjojo.playerapi.NetworkPlayer;

import java.util.Optional;
import java.util.UUID;

public class SpringPlayerImpl extends OfflineSpringPlayerImpl implements NetworkPlayer {
    private final String serverName;
    private final String proxyName;
    private final long sessionId;

    public SpringPlayerImpl(UUID uniqueId, String name, long firstPlayed, long lastPlayed, long playtime, boolean online, String serverName, String proxyName, long sessionId) {
        super(uniqueId, name, firstPlayed, lastPlayed, playtime, online);
        this.serverName = serverName;
        this.proxyName = proxyName;
        this.sessionId = sessionId;
    }

    @Override
    public Optional<String> getConnectedServerName() {
        return Optional.ofNullable(serverName);
    }

    @Override
    public String getConnectedProxyName() {
        return proxyName;
    }

    @Override
    public long getSessionId() {
        return sessionId;
    }


}
