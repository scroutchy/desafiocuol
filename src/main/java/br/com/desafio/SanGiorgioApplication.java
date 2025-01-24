package br.com.desafio;

import br.com.desafio.domain.config.AwsCredentialsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties(AwsCredentialsConfig.class)
public class SanGiorgioApplication {
    public static void main(String[] args) {
        SpringApplication.run(SanGiorgioApplication.class, args);
    }
}