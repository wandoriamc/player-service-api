package it.einjojo.playerapi;


import io.grpc.ManagedChannel;
import io.lettuce.core.RedisClient;
import it.einjojo.playerapi.config.PluginConfig;
import it.einjojo.playerapi.config.RedisConnectionConfiguration;
import it.einjojo.playerapi.config.SharedConnectionConfiguration;
import it.einjojo.playerapi.listener.PaperConnectionHandler;
import it.einjojo.playerapi.util.DefaultServerNameProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

        channel = config.createChannel();
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
        log.info("Shutting down.");
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
            getSLF4JLogger().info("gRPC channel has been shut down.");
        } else {
            getSLF4JLogger().warn("gRPC channel was already shut down or not initialized.");
        }
        if (!executor.isShutdown()) {
            executor.shutdownNow();
            getSLF4JLogger().info("Executor service has been shut down.");
        } else {
            getSLF4JLogger().warn("Executor service was already shut down or not initialized.");
        }

    }
}
