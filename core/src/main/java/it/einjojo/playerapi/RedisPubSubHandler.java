package it.einjojo.playerapi;

import com.google.protobuf.InvalidProtocolBufferException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import it.einjojo.playerapi.config.RedisConnectionConfiguration;
import it.einjojo.protocol.player.ConnectRequest;
import it.einjojo.protocol.player.ConnectResponse;
import it.einjojo.protocol.player.LoginNotify;
import it.einjojo.protocol.player.LogoutNotify;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * @author EinjoJo
 * <p>Lazy redis handler. Stays inactive as long as no consumers are registered.</p>
 * <p>Listens to playerapi:login and playerapi:logout channels and dispatch the received protobuf messages to the provided consumers.</p>
 */
public class RedisPubSubHandler extends RedisPubSubAdapter<byte[], byte[]> implements Closeable {
    private static final byte[] LOGIN_NOTIFY_CHANNEL = "plapi:li".getBytes();
    private static final byte[] LOGOUT_NOTIFY_CHANNEL = "plapi:lo".getBytes();
    private static final byte[] CONNECT_REQ_CHANNEL = "plapi:co".getBytes();
    private static final byte[] CONNECT_RES_CHANNEL = "plapi:rco".getBytes();
    private static final Logger log = LoggerFactory.getLogger(RedisPubSubHandler.class);
    private final RedisURI redisUri;
    private final Executor executor;
    private @Nullable List<Consumer<LoginNotify>> loginNotifyConsumers;
    private @Nullable List<Consumer<LogoutNotify>> logoutNotifyConsumers;
    private @Nullable Consumer<ConnectRequest> connectRequestConsumer;
    private @Nullable Consumer<ConnectResponse> connectResponseConsumer;
    private @Nullable RedisClient client;
    private @Nullable StatefulRedisPubSubConnection<byte[], byte[]> connection;


    /**
     * Constructor for RedisPubSubHandler.
     *
     * @param redisConnectionConfiguration config
     * @param executor                     avoid blocking Event-Loop in Pub Sub Listener
     */
    public RedisPubSubHandler(@NotNull RedisConnectionConfiguration redisConnectionConfiguration, Executor executor) {
        this.redisUri = redisConnectionConfiguration.createUri("playerapi");
        this.executor = executor;
    }


    public Closeable subscribeLogin(Consumer<LoginNotify> consumer) {
        openConnection(connection);
        if (loginNotifyConsumers == null) {
            loginNotifyConsumers = new LinkedList<>();
            connection.sync().subscribe(LOGIN_NOTIFY_CHANNEL);
            log.info("Subscribed to login notify channel");
        }
        loginNotifyConsumers.add(consumer);
        log.info("Registered login notify consumer");
        return () -> loginNotifyConsumers.remove(consumer);
    }


    public Closeable subscribeLogout(Consumer<LogoutNotify> consumer) {
        openConnection(connection);
        if (logoutNotifyConsumers == null) {
            logoutNotifyConsumers = new LinkedList<>();
            connection.sync().subscribe(LOGOUT_NOTIFY_CHANNEL);
            log.info("Subscribed to logout notify channel");
        }
        logoutNotifyConsumers.add(consumer);
        log.info("Registered logout notify consumer");
        return () -> logoutNotifyConsumers.remove(consumer);
    }

    @ApiStatus.Internal
    public void setConnectRequestConsumer(Consumer<ConnectRequest> consumer) {
        openConnection(connection);
        if (connectRequestConsumer == null) {
            connection.sync().subscribe(CONNECT_REQ_CHANNEL);
            log.info("Subscribed to connect request channel");
        }
        connectRequestConsumer = consumer;
    }

    @ApiStatus.Internal
    public void setConnectResponseConsumer(Consumer<ConnectResponse> consumer) {
        openConnection(connection);
        if (connectResponseConsumer == null) {
            connection.sync().subscribe(CONNECT_RES_CHANNEL);
            log.info("Subscribed to connect response channel");
        }
        connectResponseConsumer = consumer;
    }

    @Contract(value = "null -> fail;") // ide null-warning workaround
    private void openConnection(StatefulRedisPubSubConnection<byte[], byte[]> connection) {
        if (client == null) {
            client = RedisClient.create(redisUri);
            log.info("Created redis client");
        }
        if (connection != null) {
            return;
        }
        this.connection = client.connectPubSub(ByteArrayCodec.INSTANCE);
        this.connection.addListener(this);
        log.info("Opened connection to redis pub sub");
    }

    @Override
    public void message(byte[] channel, byte[] message) {
        // pass to executor to avoid any unpurposed blocking calls inside this Pub/Sub callback
        if (loginNotifyConsumers != null && Arrays.equals(channel, LOGIN_NOTIFY_CHANNEL)) {
            try {
                LoginNotify notify = LoginNotify.parseFrom(message);
                executor.execute(() -> {
                    for (Consumer<LoginNotify> consumer : loginNotifyConsumers) {
                        try {
                            consumer.accept(notify);
                        } catch (Exception e) {
                            log.error("Exception during login notify consumer processing", e);
                        }
                    }
                });
            } catch (InvalidProtocolBufferException e) {
                log.error("Failed to parse LoginNotify message", e);
            }
        } else if (logoutNotifyConsumers != null && Arrays.equals(channel, LOGOUT_NOTIFY_CHANNEL)) {
            try {
                LogoutNotify logoutNotify = LogoutNotify.parseFrom(message);
                executor.execute(() -> {
                    for (Consumer<LogoutNotify> consumer : logoutNotifyConsumers) {
                        try {
                            consumer.accept(logoutNotify);
                        } catch (Exception e) {
                            log.error("Exception during logout notify consumer processing", e);
                        }
                    }
                });
            } catch (InvalidProtocolBufferException e) {
                log.error("Failed to parse LogoutNotify message", e);
            }
        } else if (connectRequestConsumer != null && Arrays.equals(channel, CONNECT_REQ_CHANNEL)) {
            try {
                ConnectRequest connectRequest = ConnectRequest.parseFrom(message);
                executor.execute(() -> {
                    try {
                        connectRequestConsumer.accept(connectRequest);
                    } catch (Exception ex) {
                        log.error("Exception during connect request consumer processing", ex);
                    }
                });
            } catch (InvalidProtocolBufferException e) {
                log.error("Failed to parse ConnectRequest message", e);
            }
        } else if (connectResponseConsumer != null && Arrays.equals(channel, CONNECT_RES_CHANNEL)) {
            try {
                ConnectRequest connectRequest = ConnectRequest.parseFrom(message);
                executor.execute(() -> {
                    try {
                        connectRequestConsumer.accept(connectRequest);
                    } catch (Exception e) {
                        log.error("Exception during connect response consumer processing", e);
                    }
                });
            } catch (InvalidProtocolBufferException e) {
                log.error("Failed to parse ConnectRequest message", e);
            }
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
