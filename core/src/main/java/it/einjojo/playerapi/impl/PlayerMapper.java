package it.einjojo.playerapi.impl;

import it.einjojo.playerapi.NetworkPlayer;
import it.einjojo.playerapi.OfflineNetworkPlayer;
import it.einjojo.protocol.player.GetOfflinePlayerResponse;
import it.einjojo.protocol.player.GetOnlinePlayerResponse;
import it.einjojo.protocol.player.OfflinePlayerDefinition;
import it.einjojo.protocol.player.OnlinePlayerDefinition;

import java.util.UUID;


public class PlayerMapper {

    public static OfflineNetworkPlayer toLocal(OfflinePlayerDefinition playerDefinition) {
        return new OfflineNetworkPlayerImpl(
                UUID.fromString(playerDefinition.getUniqueId()),
                playerDefinition.getUsername(),
                playerDefinition.getFirstLogin(),
                playerDefinition.getLastLogin(),
                playerDefinition.getOnlineTime(),
                playerDefinition.getOnline()
        );
    }

    public static NetworkPlayer toLocal(OnlinePlayerDefinition playerDefinition) {
        return new NetworkPlayerImpl(
                UUID.fromString(playerDefinition.getUniqueId()),
                playerDefinition.getUsername(),
                playerDefinition.getFirstLogin(),
                playerDefinition.getLastLogin(),
                playerDefinition.getOnlineTime(),
                true,
                playerDefinition.getConnectedServerName(),
                playerDefinition.getConnectedProxyName(),
                playerDefinition.getSessionId()
        );
    }

    public static NetworkPlayer readOnlineResponse(GetOnlinePlayerResponse getOnlinePlayerResponse) {
        return toLocal(getOnlinePlayerResponse.getPlayer());
    }

    public static OfflineNetworkPlayer readOfflineResponse(GetOfflinePlayerResponse getOfflinePlayerResponse) {
        return toLocal(getOfflinePlayerResponse.getPlayer());
    }
}
