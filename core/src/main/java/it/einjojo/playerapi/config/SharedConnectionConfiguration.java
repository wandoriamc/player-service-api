package it.einjojo.playerapi.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Reader class for connections.json, reduced to redis
 */
public record SharedConnectionConfiguration(RedisConnectionConfiguration redis) {
    private static final Path configFile = Paths.get("connections.json");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();


    public static Optional<SharedConnectionConfiguration> load() {

        try {
            if (!Files.exists(configFile)) {
                return Optional.empty();
            }
            String json = new String(Files.readAllBytes(configFile));
            return Optional.ofNullable(GSON.fromJson(json, SharedConnectionConfiguration.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load connections.json", e);
        }
    }


}