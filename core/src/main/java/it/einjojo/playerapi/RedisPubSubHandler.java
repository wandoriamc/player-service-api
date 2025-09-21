package it.einjojo.playerapi;

import com.google.protobuf.InvalidProtocolBufferException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import it.einjojo.playerapi.config.RedisConnectionConfiguration;
import it.einjojo.protocol.player.LoginNotify;
import it.einjojo.protocol.player.LogoutNotify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class RedisPubSubHandler extends RedisPubSubAdapter<byte[], byte[]> implements Closeable {
    private static final byte[] LOGIN_NOTIFY_CHANNEL = "playerapi:login".getBytes();
    private static final byte[] LOGOUT_NOTIFY_CHANNEL = "playerapi:logout".getBytes();
    private static final Logger log = LoggerFactory.getLogger(RedisPubSubHandler.class);
    private final List<Consumer<LoginNotify>> loginNotifyConsumers = new LinkedList<>();
    private final List<Consumer<LogoutNotify>> logoutNotifyConsumers = new LinkedList<>();
    private final RedisURI redisUri;
    private RedisClient client = null;
    private StatefulRedisPubSubConnection<byte[], byte[]> connection = null;

    public RedisPubSubHandler(RedisConnectionConfiguration redisConnectionConfiguration) {
        redisUri = redisConnectionConfiguration.createUri("playerapi");
    }

    public Closeable subscribeLoginNotify(Consumer<LoginNotify> consumer) {
        if (client == null) {
            client = RedisClient.create(redisUri);
            connection = client.connectPubSub(ByteArrayCodec.INSTANCE);
            connection.addListener(this);
            connection.sync().subscribe(LOGIN_NOTIFY_CHANNEL, LOGOUT_NOTIFY_CHANNEL);
        }
        loginNotifyConsumers.add(consumer);
        return () -> loginNotifyConsumers.remove(consumer);
    }

    public Closeable subscribeLogoutNotify(Consumer<LogoutNotify> consumer) {
        if (client == null) {
            client = RedisClient.create(redisUri);
            connection = client.connectPubSub(ByteArrayCodec.INSTANCE);
            connection.sync().subscribe(LOGIN_NOTIFY_CHANNEL, LOGOUT_NOTIFY_CHANNEL);
        }
        logoutNotifyConsumers.add(consumer);
        return () -> logoutNotifyConsumers.remove(consumer);
    }

    @Override
    public void message(byte[] channel, byte[] message) {
        if (Arrays.equals(channel, LOGIN_NOTIFY_CHANNEL)) {
            try {
                LoginNotify notify = LoginNotify.parseFrom(message);
                loginNotifyConsumers.forEach(consumer -> consumer.accept(notify));
                log.info("Handled login notify");
            } catch (InvalidProtocolBufferException e) {
                log.error("Failed to parse LoginNotify message", e);
            }
        } else if (Arrays.equals(channel, LOGOUT_NOTIFY_CHANNEL)) {
            try {
                LogoutNotify logoutNotify = LogoutNotify.parseFrom(message);
                logoutNotifyConsumers.forEach(consumer -> consumer.accept(logoutNotify));
                log.info("Handled logout notify");
            } catch (InvalidProtocolBufferException e) {
                log.error("Failed to parse LogoutNotify message", e);
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
