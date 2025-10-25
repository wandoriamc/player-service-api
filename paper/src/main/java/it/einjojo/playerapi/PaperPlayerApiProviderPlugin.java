package it.einjojo.playerapi;


import io.grpc.ManagedChannel;
import io.lettuce.core.RedisClient;
import it.einjojo.playerapi.config.PluginConfig;
import it.einjojo.playerapi.config.RedisConnectionConfiguration;
import it.einjojo.playerapi.config.SharedConnectionConfiguration;
import it.einjojo.playerapi.impl.AbstractPlayerApi;
import it.einjojo.playerapi.listener.PaperConnectionHandler;
import it.einjojo.playerapi.util.DefaultServerNameProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Has config and supports the shared connection specification.
 * If running the server in online mode, the plugin will handle login and logout.
 */
public class PaperPlayerApiProviderPlugin extends JavaPlugin {
    private final Logger log = getSLF4JLogger();
    public static PaperPlayerApiProviderPlugin INSTANCE;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private ManagedChannel channel;


    /**
     * Performs redis connection tests and provides the paper api implementation.
     */
    @Override
    public void onEnable() {
        INSTANCE = this;
        PluginConfig config = PluginConfig.load(getDataPath());
        var sharedConfig = SharedConnectionConfiguration.load();
        RedisConnectionConfiguration redisConfig = sharedConfig.map(SharedConnectionConfiguration::redis).orElseGet(config::redis);
        try (var client = RedisClient.create(redisConfig.createUri("playerapi")); var con = client.connect()) {
            log.info("Pinging redis server {}... ", con.sync().ping());
        } catch (Exception ex) {
            log.error("{} | SharedConfig available: {} \n  ==> Your {} \n", ex.getMessage(), sharedConfig.isPresent(), redisConfig);
            getSLF4JLogger().info("Disabling PlayerApi plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        channel = config.createChannel(executor);
        var state = channel.getState(true);
        log.info("gRPC channel to PlayerApi server is in state: {}", state);
        channel.notifyWhenStateChanged(state, () -> {
            var newState = channel.getState(true);
            log.info("gRPC channel to PlayerApi server changed state: {}", newState);
        });
        PaperPlayerApi playerApi = new PaperPlayerApi(channel, executor, redisConfig);
        PlayerApiProvider.register(playerApi);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getSLF4JLogger().info("PlayerApi Paper plugin has been initialized.");
        if (getServer().getOnlineMode()) {
            getSLF4JLogger().info("Detected online mode. This Server will handle authentication for players.");
            new PaperConnectionHandler(this, playerApi, executor, new DefaultServerNameProvider().getServerName());
        }
    }


    @Override
    public void onDisable() {
        log.info("Shutting down...");
        ((AbstractPlayerApi) PlayerApiProvider.getInstance()).shutdown();
        // Shutdown executor first to stop new tasks
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    log.warn("Executor did not terminate in time, forcing shutdown...");
                    executor.shutdownNow();
                    executor.awaitTermination(2, TimeUnit.SECONDS);
                }
                getSLF4JLogger().info("Executor service has been shut down.");
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for executor shutdown.", e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } else {
            getSLF4JLogger().warn("Executor service was already shut down or not initialized.");
        }

        // Then shutdown gRPC channel gracefully
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown(); // Initiate graceful shutdown first
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("gRPC channel did not terminate gracefully, forcing shutdown...");
                    channel.shutdownNow();
                    if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.error("gRPC channel did not terminate even after force shutdown.");
                    }
                }
                getSLF4JLogger().info("gRPC channel has been shut down.");
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for gRPC channel shutdown.", e);
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } else {
            getSLF4JLogger().warn("gRPC channel was already shut down or not initialized.");
        }
        log.info("PlayerApi Paper plugin has been disabled.");
    }
}
