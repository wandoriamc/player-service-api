package it.einjojo.playerapi.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for the plugin, including gRPC and Redis settings.
 *
 * @param gsonAddress the address of the gRPC server
 * @param gsonPort    the port of the gRPC server
 * @param redis       the redis configuration
 */
public record PluginConfig(String gsonAddress, int gsonPort, RedisConnectionConfiguration redis) {
    public static PluginConfig load(Path folder) {
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path configFile = folder.resolve("config.json");
        if (!configFile.toFile().exists()) {
            return createDefault(configFile);
        }
        try (BufferedReader bufferedReader = Files.newBufferedReader(configFile); JsonReader reader = new JsonReader(bufferedReader)) {
            return gson.fromJson(reader, PluginConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file: " + configFile, e);
        }
    }

    public static PluginConfig createDefault(Path configFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        PluginConfig defaultConfig = new PluginConfig("localhost", 9090, new RedisConnectionConfiguration(
                "localhost", 6379, "default", "default", false
        ));
        try {
            Files.createFile(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(configFile); JsonWriter jsonWriter = gson.newJsonWriter(writer)) {
            gson.toJson(defaultConfig, PluginConfig.class, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return defaultConfig;
    }


    public ManagedChannel createChannel() {
        return ManagedChannelBuilder.forAddress(gsonAddress, gsonPort)
                .disableRetry()
                .usePlaintext()
                .build();
    }


}
