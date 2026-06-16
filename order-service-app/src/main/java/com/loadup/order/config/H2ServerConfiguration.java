package com.loadup.order.config;

import org.h2.tools.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
@ConditionalOnProperty(prefix = "app.h2.tcp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class H2ServerConfiguration {

    private final String tcpPort;

    public H2ServerConfiguration(@Value("${app.h2.tcp.port:9094}") String tcpPort) {
        this.tcpPort = tcpPort;
    }

    @Bean(destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        return Server.createTcpServer(
            "-tcp",
            "-tcpPort", tcpPort,
            "-tcpAllowOthers",
            "-ifNotExists"
        ).start();
    }
}
