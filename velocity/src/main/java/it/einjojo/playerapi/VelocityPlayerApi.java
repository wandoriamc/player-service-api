package it.einjojo.playerapi;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.grpc.ManagedChannel;
import it.einjojo.playerapi.impl.AbstractPlayerApi;
import it.einjojo.playerapi.impl.PlayerMapper;
import it.einjojo.protocol.player.*;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class VelocityPlayerApi extends AbstractPlayerApi {
    private final LocalOnlinePlayerAccessor localOnlinePlayerAccessor;

    /**
     * Constructor for AbstractPlayerApi.
     *
     * @param channel  the gRPC channel to communicate with the player service
     * @param executor the executor to run the callbacks on
     */
    public VelocityPlayerApi(ManagedChannel channel, Executor executor, ProxyServer proxyServer) {
        super(channel, executor);
        this.localOnlinePlayerAccessor = new VelocityLocalPlayerAccessor(proxyServer);
    }

    @Override
    public LocalOnlinePlayerAccessor getLocalOnlinePlayerAccessor() {
        return localOnlinePlayerAccessor;
    }

    @Override
    public Closeable subscribeLogin(Consumer<NetworkPlayer> playerConsumer) {
        return null;
    }

    @Override
    public Closeable subscribeLogout(Consumer<OfflineNetworkPlayer> offlinePlayerConsumer) {
        return null;
    }

    public CompletableFuture<NetworkPlayer> handleLogin(Player player) {
        String skinTexture = null;
        String skinSignature = null;
        for (var prop : player.getGameProfile().getProperties()) {
            if (prop.getName().equals("textures" )) {
                skinTexture = prop.getValue();
                skinSignature = prop.getSignature();
                break;
            }
        }
        var future = super.playerServiceStub.login(LoginRequest.newBuilder()
                .setUniqueId(player.getUniqueId().toString())
                .setUsername(player.getUsername())
                .setProxyName("velocity" )
                .setSkinTexture(skinSignature)
                .build());
        return createCallback(future, (response) -> PlayerMapper.toLocal(response.getPlayer()));
    }

    public CompletableFuture<Boolean> handleLogout(Player player) {
        var future = super.playerServiceStub.logout(LogoutRequest.newBuilder().setUniqueId(player.getUniqueId().toString()).build());
        return createCallback(future, LogoutNotify::getSuccess);
    }


    public CompletableFuture<Boolean> handleServerChange(Player player) {
        String serverName = player.getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse(null);
        var future = super.playerServiceStub.updateConnection(UpdateConnectionRequest.newBuilder()
                .setUniqueId(player.getUniqueId().toString())
                .setConnectedServerName(serverName)
                .build());
        return createCallback(future, UpdateConnectionResponse::getSuccess);
    }
}
