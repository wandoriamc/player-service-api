# Network Player-API 🌟

Platform api to get information about the players of Wandoria. This uses **GRPC** and **Redis** to exchange
information.  
Currently implemented on paper and velocity.

---

## Integration 📈

To use the api locally, follow these steps to clone the specific version and publish it to your local Maven repository.

### 1. Build and Publish

Execute these commands sequentially to prepare the API library:

```bash
# 1. Clone the repository
git clone https://github.com/wandoriamc/player-service-api.git
cd player-service-api

# 2. Build and install the API to your local Maven cache (~/.m2/repository)
./gradlew publishToMavenLocal
```

### 2. Project Dependency (Your `build.gradle.kts`)

When adding the dependency to **your project**, you **must omit the patch version** (`Major.Minor.Patch`) as required.

```kotlin
// build.gradle.kts
repositories {
    // REQUIRED: Allows Gradle to find the library in your local cache
    mavenLocal()
}

dependencies {
    // Dependency must use the MAJOR.MINOR version (1.4, not 1.4.3)
    compileOnly("it.einjojo.playerapi:api:1.4")
}
```

### 3. Use it.

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

- Executing connection requests is done with redis pub/sub.
  Implementation is lazy and will not be initialized until the first connection request is made.

- Obtaining Redis Connection happens in the following order:
    1. Look in Bukkit-Service-API
    2. Read SharedConnectionConfig (`~/connections.json`, same level as eula.txt)
    3. Use own config (`plugins/PlayerApi/config.json`)

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
