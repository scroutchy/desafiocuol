package br.com.desafio.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;

import static software.amazon.awssdk.regions.Region.SA_EAST_1;

@Configuration
public class AwsConfig {

    private final AwsCredentialsConfig awsCredentialsConfig;

    public AwsConfig(AwsCredentialsConfig awsCredentialsConfig) {
        this.awsCredentialsConfig = awsCredentialsConfig;
    }

    @Bean
    @Profile("!test")
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(SA_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }
}
