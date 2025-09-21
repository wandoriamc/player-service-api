# Usage

Klone das Projekt und führe `./gradlew api:publishToMavenLocal` aus.

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

