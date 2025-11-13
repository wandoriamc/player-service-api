# Usage

Klone das Projekt und führe `./gradlew api:publishToMavenLocal` aus.

```groovy
repositories {
    mavenLocal()
}

dependencies {
    compileOnly("it.einjojo.playerapi:api:1.1.0")
}
```

```java
public class UsageExample {
    static {
        PlayerApi api = PlayerApiProvider.getInstance();
        api.getOnlinePlayer(UUID.randomUUID()).thenAccept(player -> {
            long sessionMillis = player.getSessionTime();
        }).exceptionally(ex -> {
            // render error
            return null;
        });
    }
}
```

PubSub-Channels
---

`playerapi:login`
`playerapi:logout`

# Dev Docs

The Paper implementation supports the shared connection specification v1.0.0 but is also providing a redis config
section in the plugin config.

---

## Paper Specials

When a player logs in, the Bukkit Player objects metadata is modified:
(see `PaperConnectionVerifyListener.java`)

- `session_id`(long) the session id
- `login_millis`(long) Timestamp millis when the player has logged onto the proxy


```java

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
```