package it.einjojo.playerapi;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.grpc.ManagedChannel;
import it.einjojo.playerapi.config.PluginConfig;
import it.einjojo.playerapi.config.RedisConnectionConfiguration;
import it.einjojo.playerapi.config.SharedConnectionConfiguration;
import it.einjojo.playerapi.listener.ConnectionListener;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(
        id = "playerapi-velocity",
        name = "PlayerApi Velocity",
        version = "1.0.0",
        description = "A plugin that provides PlayerApi functionality for Velocity.",
        authors = {"EinjoJo"},
        url = "https://einjojo.it/work/springx"

)
public class VelocityPlayerApiProviderPlugin {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ManagedChannel channel;

    @Inject
    public VelocityPlayerApiProviderPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PluginConfig config = PluginConfig.load(dataDirectory);
        channel = config.createChannel();
        RedisConnectionConfiguration redis = SharedConnectionConfiguration.load().map(SharedConnectionConfiguration::redis).orElseGet(config::redis);
        VelocityPlayerApi playerApi = new VelocityPlayerApi(channel, executor, server, redis);
        PlayerApiProvider.register(playerApi);
        server.getEventManager().register(this, new ConnectionListener(playerApi, logger));
        logger.info("PlayerApi Velocity plugin has been initialized.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
            logger.info("gRPC channel has been shut down.");
        } else {
            logger.warn("gRPC channel was already shut down or not initialized.");
        }
        if (!executor.isShutdown()) {
            executor.shutdownNow();
            logger.info("Executor service has been shut down.");
        } else {
            logger.warn("Executor service was already shut down or not initialized.");
        }


    }

}
