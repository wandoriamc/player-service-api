package it.einjojo.playerapi;


import io.grpc.ManagedChannel;
import it.einjojo.playerapi.config.PluginConfig;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PaperPlayerApiProviderPlugin extends JavaPlugin {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ManagedChannel channel;



    @Override
    public void onEnable() {
        PluginConfig config = PluginConfig.load(getDataPath());
        channel = config.createChannel();
        PaperPlayerApi playerApi = new PaperPlayerApi(channel, executor);
        PlayerApiProvider.register(playerApi);
        getSLF4JLogger().info("PlayerApi Velocity plugin has been initialized." );
    }


    @Override
    public void onDisable() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
            getSLF4JLogger().info("gRPC channel has been shut down." );
        } else {
            getSLF4JLogger().warn("gRPC channel was already shut down or not initialized." );
        }
        if (!executor.isShutdown()) {
            executor.shutdownNow();
            getSLF4JLogger().info("Executor service has been shut down." );
        } else {
            getSLF4JLogger().warn("Executor service was already shut down or not initialized." );
        }

    }
}
