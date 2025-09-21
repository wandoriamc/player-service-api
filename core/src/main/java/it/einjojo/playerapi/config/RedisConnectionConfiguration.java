package it.einjojo.playerapi.config;

import io.lettuce.core.RedisURI;

public record RedisConnectionConfiguration(String host, int port, String username, String password,
                                           boolean ssl) {

        public RedisURI createUri(String clientName) {
            return RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withAuthentication(username, password)
                    .withClientName(clientName)
                    .withSsl(ssl)
                    .build();
        }
    }