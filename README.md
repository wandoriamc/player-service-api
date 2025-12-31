# Network Player-API 🌟

Platform api to get information about the players of Wandoria. This uses **GRPC** and **Redis** to exchange
information.  
Currently implemented on paper and velocity.

---

## Integration 📈

To use the api locally, follow these steps to clone the specific version and publish it to your local Maven repository.


### Use it.
```kotlin
// build.gradle.kts
repositories {
    maven("https://repo.einjojo.it/releases")
    maven("https://repo.einjojo.it/snapshots")
}

dependencies {
    // Dependency must use the MAJOR.MINOR version (1.5, not 1.5.0)
    compileOnly("it.einjojo.playerapi:api:1.5") // to be updated in the future
    
  // for snapshots use:
    compileOnly("it.einjojo:playerapi:1.5-6da76b7")
}
```
```java
public class UsageExample {
    static {
        PlayerApi api = PlayerApiProvider.getInstance(); // get the api instance on paper or velocity
        api.getOnlinePlayer(UUID.randomUUID()).thenAccept(player -> {
            long sessionMillis = player.getSessionTime();
        }).exceptionally(ex -> {
            // render error
            return null;
        });
    }
}
```

---

## Paper Plugin Behaviour

- The Paper implementation supports the _wandoria shared connection specification_ v1 but is also providing a redis
  config
  section in the plugin config.

- Executing connection requests is done with redis pub/sub.<br/>
  The implementation is **lazy**, and redis will not be initialized until the first connection request is made.

- Obtaining Redis Connection happens in the following order:
    1. Read SharedConnectionConfig (`~/connections.json`, same level as eula.txt)
    2. Use own config (`plugins/PlayerApi/config.json`)

- When a player logs in, the Bukkit Player objects metadata is modified:
  (see `PaperConnectionVerifyListener.java`)

    - `session_id`(long) the session id
    - `proxy`(string) name of the proxy (only useful in multi proxy environment)
    - `login_millis`(long) Timestamp millis when the player has logged onto the proxy

```java

/**
 * Paper only utility method 
 * Retrieves the Session ID from a Player object.
 *
 * @param player The Player to retrieve the Session ID from.
 * @return An Optional containing the Session ID, or an empty Optional if not found
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

---

### Setup / Use it on a server

**Requirements**: Have a grpc-Service that supports this "protocol" _(The service implementation is currently closed
source.)_

- Provide the grpc service address in the plugin config.
- Optional: provide the redis
  config section.
