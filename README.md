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

