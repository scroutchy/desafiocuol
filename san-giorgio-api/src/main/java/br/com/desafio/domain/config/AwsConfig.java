package br.com.desafio.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;

import static software.amazon.awssdk.regions.Region.US_EAST_1;

@Configuration
public class AwsConfig {

    @Bean
    @Profile("!test")
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
