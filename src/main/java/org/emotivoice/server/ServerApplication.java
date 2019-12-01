package org.emotivoice.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public SSHManager sshManager(
            @Value("${model_server.hostname}") String hostname,
            @Value("${model_server.port:}") String port,
            @Value("${model_server.username}") String username,
            @Value("${model_server.password}") String password
    ) {
        if(port.isEmpty()) return new SSHManager(hostname, username, password);
        else return new SSHManager(hostname, Integer.parseInt(port), username, password);
    }
}
