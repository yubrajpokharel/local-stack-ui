# local-stack-ui

LocalStack UI for working with SNS topics, SQS queues, S3 buckets, GCP Pub/Sub, GCP Cloud Storage, GCP Firestore, Redis values, MongoDB, Kafka, and lightweight mock HTTP services from one local Spring Boot app.

## Features

- SNS: create/delete topics, view subscriptions, publish messages to topics.
- SQS: create/delete Standard or FIFO queues, subscribe queues to SNS topics, view queue messages without consuming them, send messages directly to a queue.
- S3: create/delete buckets, view/upload/delete bucket contents.
- AWS LocalStack: check status and start/stop the LocalStack Docker service from AWS pages.
- GCP Pub/Sub: check status, start/stop the emulator, create topics/subscriptions, publish messages.
- GCP Cloud Storage: check status, start/stop the emulator, create/delete buckets, list/upload/delete objects.
- GCP Firestore: check status, start/stop the emulator, create/list/delete documents.
- Redis: check health, list keys, view/set/delete string values.
- MongoDB: check status, start the Docker service on demand, list databases.
- Kafka: check status, start/stop the Docker service on demand, list and create topics.
- Mock HTTP: start/stop local mock HTTP servers with a configurable port and response body.

## Prerequisites

- Docker and Docker Compose
- Java 23
- Maven

## Run Locally

Start LocalStack, Redis, MongoDB, Kafka, and GCP emulators:

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
curl http://localhost:8085/mongodb/status
curl http://localhost:8085/kafka/status
curl http://localhost:8085/rabbitmq/status
curl http://localhost:8085/gcp/pubsub/status
curl http://localhost:8085/gcp/storage/status
curl http://localhost:8085/gcp/firestore/status
```

## UI Pages

```text
/             service dashboard
/messaging    SNS and SQS management
/s3           S3 bucket management
/redis        Redis key/value viewer
/mongodb      MongoDB status and database viewer
/kafka        Kafka status and topic viewer
/rabbitmq     RabbitMQ queue and message viewer
/gcp/pubsub   GCP Pub/Sub emulator
/gcp/storage  GCP Cloud Storage emulator
/gcp/firestore GCP Firestore emulator
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
localstack.endpoint=http://localhost:4566
localstack.docker.service=localstack
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.mongodb.uri=mongodb://localhost:27017/localstackui
kafka.bootstrap.servers=localhost:9092
kafka.docker.service=kafka
kafka.topic.command=/opt/kafka/bin/kafka-topics.sh
kafka.producer.command=/opt/kafka/bin/kafka-console-producer.sh
kafka.offsets.command=/opt/kafka/bin/kafka-get-offsets.sh
rabbitmq.host=localhost
rabbitmq.port=5672
rabbitmq.management.endpoint=http://localhost:15672
rabbitmq.username=guest
rabbitmq.password=guest
rabbitmq.vhost=/
rabbitmq.docker.service=rabbitmq
gcp.project.id=localstack-ui
gcp.pubsub.host=localhost
gcp.pubsub.port=8681
gcp.pubsub.docker.service=gcp-pubsub
gcp.pubsub.disable.credentials=true
gcp.pubsub.api.endpoint=http://localhost:8681/
gcp.storage.endpoint=http://localhost:4443
gcp.storage.docker.service=gcp-storage
gcp.firestore.host=localhost
gcp.firestore.port=8787
gcp.firestore.endpoint=http://localhost:8787
gcp.firestore.docker.service=gcp-firestore
gcp.firestore.auth.token=owner
mock.servers=
```

Local services are defined in `docker-compose.yml`:

```text
LocalStack: http://localhost:4566
Redis:      localhost:6379
MongoDB:    mongodb://localhost:27017
Kafka:     kafka://localhost:9092
RabbitMQ:  amqp://localhost:5672, management http://localhost:15672
GCP Pub/Sub:       pubsub://localhost:8681
GCP Cloud Storage: http://localhost:4443
GCP Firestore:     firestore://localhost:8787
```

## AWS LocalStack Examples

Check AWS LocalStack status:

```bash
curl http://localhost:8085/localstack/status
```

Start AWS services through the app:

```bash
curl -X POST http://localhost:8085/localstack/start
```

Stop AWS services through the app:

```bash
curl -X POST http://localhost:8085/localstack/stop
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

Create a Standard queue:

```bash
curl -X POST "http://localhost:8085/sqs/createQueue/orders-queue?type=Standard"
```

Create a FIFO queue:

```bash
curl -X POST "http://localhost:8085/sqs/createQueue/orders-events?type=FIFO"
```

FIFO queues are created with `.fifo` appended if the name does not already end with `.fifo`, and content-based deduplication is enabled.

List queue details, including type:

```bash
curl http://localhost:8085/sqs/details
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

Send a message directly to a FIFO queue:

```bash
curl -X POST \
  -H "Content-Type: text/plain" \
  --data "hello from fifo sqs" \
  "http://localhost:8085/sqs/sendMessage/orders-events.fifo?messageGroupId=orders"
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

Download an S3 object:

```bash
curl -OJ "http://localhost:8085/s3-buckets/download/demo-bucket?fileName=sample.txt"
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

## MongoDB Examples

You can manage MongoDB from:

```text
http://localhost:8085/mongodb
```

Check MongoDB status:

```bash
curl http://localhost:8085/mongodb/status
```

Start MongoDB on demand through the app:

```bash
curl -X POST http://localhost:8085/mongodb/start
```

List databases:

```bash
curl http://localhost:8085/mongodb/databases
```

You can also start MongoDB directly:

```bash
docker compose up -d mongodb
```

## Kafka Examples

You can manage Kafka from:

```text
http://localhost:8085/kafka
```

Check Kafka status:

```bash
curl http://localhost:8085/kafka/status
```

Start Kafka on demand through the app:

```bash
curl -X POST http://localhost:8085/kafka/start
```

Create a topic:

```bash
curl -X POST \
  "http://localhost:8085/kafka/topics?topicName=orders-created&partitions=1&replicationFactor=1"
```

List topics:

```bash
curl http://localhost:8085/kafka/topics
```

Stop Kafka through the app:

```bash
curl -X POST http://localhost:8085/kafka/stop
```

You can also start Kafka directly:

```bash
docker compose up -d kafka
```

## RabbitMQ Examples

You can manage RabbitMQ from:

```text
http://localhost:8085/rabbitmq
```

Check RabbitMQ status:

```bash
curl http://localhost:8085/rabbitmq/status
```

Start RabbitMQ on demand through the app:

```bash
curl -X POST http://localhost:8085/rabbitmq/start
```

Create a queue:

```bash
curl -X POST \
  "http://localhost:8085/rabbitmq/queues?queueName=orders.created&durable=true&autoDelete=false&queueType=classic"
```

Publish a message to the queue:

```bash
curl -X POST \
  -H "Content-Type: text/plain" \
  --data "hello from rabbitmq" \
  http://localhost:8085/rabbitmq/queues/orders.created/messages
```

Peek messages without deleting them:

```bash
curl "http://localhost:8085/rabbitmq/queues/orders.created/messages?count=10"
```

List queues:

```bash
curl http://localhost:8085/rabbitmq/queues
```

Stop RabbitMQ through the app:

```bash
curl -X POST http://localhost:8085/rabbitmq/stop
```

You can also start RabbitMQ directly:

```bash
docker compose up -d rabbitmq
```

## GCP Examples

You can manage GCP emulators from:

```text
http://localhost:8085/gcp/pubsub
http://localhost:8085/gcp/storage
http://localhost:8085/gcp/firestore
```

Start Pub/Sub, Cloud Storage, and Firestore through the app:

```bash
curl -X POST http://localhost:8085/gcp/pubsub/start
curl -X POST http://localhost:8085/gcp/storage/start
curl -X POST http://localhost:8085/gcp/firestore/start
```

Create a Pub/Sub topic and subscription:

```bash
curl -X POST "http://localhost:8085/gcp/pubsub/topics?topicName=orders-created"
curl -X POST \
  "http://localhost:8085/gcp/pubsub/subscriptions?subscriptionName=orders-worker&topicName=orders-created"
```

Publish a Pub/Sub message:

```bash
curl -X POST \
  -H "Content-Type: text/plain" \
  --data "hello from pubsub" \
  http://localhost:8085/gcp/pubsub/topics/orders-created/publish
```

Create a Cloud Storage bucket and upload a text object:

```bash
curl -X POST "http://localhost:8085/gcp/storage/buckets?bucketName=orders-data"
curl -X POST \
  -H "Content-Type: text/plain" \
  --data "hello from gcs" \
  "http://localhost:8085/gcp/storage/buckets/orders-data/objects?objectName=sample.txt"
```

Upload a file directly:

```bash
curl -X POST \
  -F "file=@/path/to/report.pdf" \
  -F "objectName=reports/report.pdf" \
  http://localhost:8085/gcp/storage/buckets/orders-data/objects/file
```

Download a Cloud Storage object:

```bash
curl -OJ "http://localhost:8085/gcp/storage/buckets/orders-data/objects/download?objectName=sample.txt"
```

List GCP resources:

```bash
curl http://localhost:8085/gcp/pubsub/topics
curl http://localhost:8085/gcp/pubsub/subscriptions
curl http://localhost:8085/gcp/storage/buckets
curl http://localhost:8085/gcp/storage/buckets/orders-data/objects
```

Create a Firestore document:

```bash
curl -X POST \
  -H "Content-Type: text/plain" \
  --data '{"status":"created","total":25}' \
  "http://localhost:8085/gcp/firestore/collections/orders/documents?documentName=order-1"
```

List Firestore collections and documents:

```bash
curl http://localhost:8085/gcp/firestore/collections
curl http://localhost:8085/gcp/firestore/collections/orders/documents
```

Delete a Firestore document:

```bash
curl -X DELETE http://localhost:8085/gcp/firestore/collections/orders/documents/order-1
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

If MongoDB calls fail, start MongoDB:

```bash
docker compose up -d mongodb
curl http://localhost:8085/mongodb/status
```

If Kafka calls fail, start Kafka:

```bash
docker compose up -d kafka
curl http://localhost:8085/kafka/status
```

If RabbitMQ calls fail, start RabbitMQ:

```bash
docker compose up -d rabbitmq
curl http://localhost:8085/rabbitmq/status
```

If GCP emulator calls fail, start the needed emulator:

```bash
docker compose up -d gcp-pubsub
docker compose up -d gcp-storage
docker compose up -d gcp-firestore
curl http://localhost:8085/gcp/pubsub/status
curl http://localhost:8085/gcp/storage/status
curl http://localhost:8085/gcp/firestore/status
```

If a mock HTTP server cannot start, the chosen port is probably already in use. Stop that process or choose a different port.
