package kr.ai.janus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JanusBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JanusBackendApplication.class, args);
    }

}
