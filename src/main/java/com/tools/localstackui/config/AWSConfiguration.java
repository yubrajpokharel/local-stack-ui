package com.tools.localstackui.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.http.apache.client.impl.SdkHttpClient;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfiguration {

  String accessKey = "SOMEACCESSKEY";

  String secretKey = "SOMESECRETKEY";

  @Value("${aws.region.name}")
  String region;

  @Bean
  public AWSCredentials awsCredentials() {
    return new BasicAWSCredentials(accessKey, secretKey);
  }

  @Bean
  public AmazonSQS amazonSQS() {
    return AmazonSQSClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
        .withEndpointConfiguration(
            new EndpointConfiguration("http://localhost:4566/_aws/sqs", region))
        .build();
  }

  @Bean
  public AmazonSNS amazonSNS() {
    return AmazonSNSClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
        .withEndpointConfiguration(
            new EndpointConfiguration("http://localhost:4566/_aws/sns", region))
        .build();
  }

  @Bean
  public AmazonS3 amazonS3() {
    return AmazonS3ClientBuilder
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
        .withEndpointConfiguration(
            new EndpointConfiguration("http://localhost:4566/_aws/s3", region)
        )
        .withPathStyleAccessEnabled(true)
        .build();
  }
}
