package it.einjojo.playerapi.util;

import org.bukkit.Utility;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Optional;


public class SessionMetadataUtil {
    private SessionMetadataUtil() { }

    /**
     * Retrieves the Session ID from a Player object.
     *
     * @param player The Player to retrieve the Session ID from.
     * @return An Optional containing the Session ID UUID, or an empty Optional if not found or not a UUID.
     */
    public static Optional<Long> getSessionId(Player player) {
        if (!player.hasMetadata("session_id")) {
            return Optional.empty();
        }
        List<MetadataValue> metadataValues = player.getMetadata("session_id");
        Optional<MetadataValue> valueOptional = metadataValues.stream()
                .findFirst();
        return valueOptional
                .flatMap(value -> Optional.of(value.asLong()));
    }

}