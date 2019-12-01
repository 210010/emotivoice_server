package org.emotivoice.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Autowired
    private HandlerInterceptor handlerInterceptor;

    @Bean
    public WebMvcConfigurer configurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:3000");
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(handlerInterceptor)
                        .addPathPatterns("/tts");
            }
        };
    }
}
