package it.einjojo.playerapi.util;

import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;


public class DefaultServerNameProvider {
    private static final Logger log = LoggerFactory.getLogger(DefaultServerNameProvider.class);
    private final String serverName;

    public DefaultServerNameProvider() {
        String name = "unknown-" + UUID.randomUUID().toString().split("-")[0];
        try {
            var method = Bukkit.getServer().getClass().getDeclaredMethod("getServerName");
            name = (String) method.invoke(Bukkit.getServer());
        } catch (NoSuchMethodException e) {
            log.warn("This server version does not support getServerName() in Server-API. Consider using a PURPUR-fork");
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Could not invoke server name from Bukkit");
        }
        log.info("Detected server name: {}", name);
        this.serverName = name;
    }

    public String getServerName() {
        return serverName;
    }
}
