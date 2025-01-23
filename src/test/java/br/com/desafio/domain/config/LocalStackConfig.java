package br.com.desafio.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Configuration
public class LocalStackConfig {

    private static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse("localstack/localstack:1.4.0");

    @Bean
    @Profile("test")
    public LocalStackContainer localStackContainer() {
        LocalStackContainer localStack = new LocalStackContainer(LOCALSTACK_IMAGE)
                .withServices(SQS);
        localStack.start();
        return localStack;
    }

    @Bean
    @Profile("test")
    public SqsClient sqsClient(LocalStackContainer localStackContainer) {
        return SqsClient.builder()
                .endpointOverride(localStackContainer.getEndpointOverride(SQS))
                .region(Region.of(localStackContainer.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())
                ))
                .build();
    }
}
