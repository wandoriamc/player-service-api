package it.einjojo.playerapi;

import java.util.Optional;

public interface NetworkPlayer extends OfflineNetworkPlayer {
    /**
     * getter
     *
     * @return the server name of the player.
     */
    Optional<String> getConnectedServerName();

    /**
     * getter
     *
     * @return the proxy name of the player.
     */
    String getConnectedProxyName();


    /**
     * getter
     *
     * @return the time in milliseconds since the player has joined.
     */
    default long getSessionTime() {
        return System.currentTimeMillis() - getLastPlayed();
    }

    /**
     * getter
     *
     * @return the session id of the player.
     */
    long getSessionId();
}
