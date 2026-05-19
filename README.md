# local-stack-ui

LocalStack UI for working with SNS topics, SQS queues, S3 buckets, Redis values, and lightweight mock HTTP services from one local Spring Boot app.

## Features

- SNS: create/delete topics, view subscriptions, publish messages to topics.
- SQS: create/delete queues, subscribe queues to SNS topics, view queue messages without consuming them, send messages directly to a queue.
- S3: create/delete buckets, view/upload/delete bucket contents.
- Redis: check health, list keys, view/set/delete string values.
- Mock HTTP: start/stop local mock HTTP servers with a configurable port and response body.

## Prerequisites

- Docker and Docker Compose
- Java 23
- Maven

## Run Locally

Start LocalStack and Redis:

```bash
docker compose up -d
```

Run the app:

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8085
```

Useful service health checks:

```bash
curl http://localhost:4566/_localstack/health
curl http://localhost:8085/redis/health
```

## UI Pages

```text
/             service dashboard
/messaging    SNS and SQS management
/s3           S3 bucket management
/redis        Redis key/value viewer
/mock-http    configurable mock HTTP services
```

Detail pages are linked from the list views:

```text
/sns-topics/{topicArn}
/sqs-message/{queueName}
/s3-buckets/{bucketName}
```

## Configuration

Main settings live in `src/main/resources/application.properties`:

```properties
server.port=8085
aws.region.name=us-east-1
spring.data.redis.host=localhost
spring.data.redis.port=6379
mock.servers=
```

Local services are defined in `docker-compose.yml`:

```text
LocalStack: http://localhost:4566
Redis:      localhost:6379
```

## SNS Examples

Create a topic:

```bash
curl -X POST http://localhost:8085/sns/createTopic/orders
```

List topics:

```bash
curl http://localhost:8085/sns-topics
```

Publish a message to a topic:

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  --data "hello from sns" \
  "http://localhost:8085/sns-topics/sendMessage/arn:aws:sns:us-east-1:000000000000:orders"
```

Delete a topic:

```bash
curl -X POST "http://localhost:8085/deleteTopic/arn:aws:sns:us-east-1:000000000000:orders"
```

## SQS Examples

Create a queue:

```bash
curl -X POST http://localhost:8085/sqs/createQueue/orders-queue
```

List queues:

```bash
curl http://localhost:8085/sqs
```

Send a message directly to a queue:

```bash
curl -X POST \
  -H "Content-Type: text/plain" \
  --data "hello from sqs" \
  http://localhost:8085/sqs/sendMessage/orders-queue
```

View messages in the UI:

```text
http://localhost:8085/sqs-message/orders-queue
```

The SQS message page uses visibility timeout `0`, so viewing messages does not consume or hide them.

Delete a queue:

```bash
curl -X POST http://localhost:8085/deleteQueue/orders-queue
```

## SNS to SQS Subscription Example

Subscribe a queue to a topic:

```bash
curl -X POST \
  "http://localhost:8085/subscribe/orders-queue/arn:aws:sns:us-east-1:000000000000:orders"
```

After publishing to the SNS topic, open the SQS queue page to view the delivered message.

## S3 Examples

Create a bucket:

```bash
curl -X POST http://localhost:8085/s3-buckets/create/demo-bucket
```

List buckets:

```bash
curl http://localhost:8085/s3-buckets
```

List bucket contents:

```bash
curl http://localhost:8085/s3-buckets/get/demo-bucket
```

Upload and delete files are available from the S3 bucket detail page:

```text
http://localhost:8085/s3-buckets/demo-bucket
```

Delete a bucket:

```bash
curl -X POST http://localhost:8085/s3-buckets/delete/demo-bucket
```

## Redis Examples

Check Redis health:

```bash
curl http://localhost:8085/redis/health
```

Set a value:

```bash
curl -X POST \
  -H "Content-Type: text/plain" \
  --data "bar" \
  http://localhost:8085/redis/value/foo
```

List keys:

```bash
curl http://localhost:8085/redis/keys
```

Get a value:

```bash
curl http://localhost:8085/redis/value/foo
```

Delete a value:

```bash
curl -X DELETE http://localhost:8085/redis/value/foo
```

## Mock HTTP Examples

You can manage mock HTTP services from:

```text
http://localhost:8085/mock-http
```

Start a mock server returning plain text:

```bash
curl -X POST \
  "http://localhost:8085/mock-http/start?name=downstream&port=8080&response=ok"
```

Call the mock:

```bash
curl http://localhost:8080
```

Start a mock server returning JSON:

```bash
curl -X POST \
  "http://localhost:8085/mock-http/start?name=state-machine&port=9000&response=%7B%22status%22:%22success%22%7D"
```

List running mock servers:

```bash
curl http://localhost:8085/mock-http/servers
```

Stop one mock server:

```bash
curl -X DELETE http://localhost:8085/mock-http/stop/8080
```

Stop all mock servers:

```bash
curl -X DELETE http://localhost:8085/mock-http/stop-all
```

You can also start mock servers automatically on app startup:

```bash
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--mock.servers='downstream|8080|ok;state-machine|9000|{\"status\":\"success\"}'"
```

## Troubleshooting

If SNS, SQS, or S3 calls fail with `Connect to localhost:4566 failed`, LocalStack is not running or not exposed on port `4566`:

```bash
docker compose up -d localstack
curl http://localhost:4566/_localstack/health
```

If Redis calls fail, start Redis:

```bash
docker compose up -d redis
curl http://localhost:8085/redis/health
```

If a mock HTTP server cannot start, the chosen port is probably already in use. Stop that process or choose a different port.
