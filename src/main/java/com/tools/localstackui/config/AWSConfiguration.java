package com.tools.localstackui.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfiguration {

  String accessKey = "SOMEACCESSKEY";

  String secretKey = "SOMESECRETKEY";

  @Bean
  public AWSCredentials awsCredentials() {
    return new BasicAWSCredentials(accessKey, secretKey);
  }

  @Bean
  public AmazonSQS amazonSQS() {
    return AmazonSQSClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
        .withEndpointConfiguration(
            new EndpointConfiguration("http://localhost:4576", Regions.US_WEST_2.getName()))
        .build();
  }

  @Bean
  public AmazonSNS amazonSNS() {

    return AmazonSNSClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
        .withEndpointConfiguration(
            new EndpointConfiguration("http://localhost:4575", Regions.US_WEST_2.getName()))
        .build();
  }
}
