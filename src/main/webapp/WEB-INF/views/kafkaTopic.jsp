<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
    <title>${topicName}</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/kafka" title="Kafka">
        <img class="img-fluid" alt="Kafka" src="/resources/images/kafka.svg" />
        Kafka</a>

    <div class="page-header">
        <img class="img-fluid" alt="Kafka" src="/resources/images/kafka.svg" />
        <div>
            <h2 class="font-weight-bold">Kafka Topic</h2>
            <span class="resource-name">${topicName}</span>
        </div>
    </div>

    <input type="hidden" id="kafkaTopicName" value="${topicName}">

    <div class="row">
        <div class="col-lg-12 mb-4">
            <div class="card app-panel">
                <div class="card-header">Topic Details</div>
                <div class="card-body">
                    <div><strong>Topic:</strong> <span class="code-cell">${topicName}</span></div>
                    <div><strong>URL:</strong> <span id="kafkaTopicAddress" class="code-cell">${topicAddress}</span></div>
                    <div><strong>CreatedOn:</strong> <span class="code-cell">${topicCreatedOn}</span></div>
                    <div><strong>Partitions:</strong> <span id="kafkaTopicPartitionCount" class="code-cell">Loading...</span></div>
                    <div><strong>Total Messages:</strong> <span id="kafkaTopicMessageCount" class="code-cell">Loading...</span></div>
                </div>
            </div>
        </div>
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Send Message</div>
                <div class="card-body">
                    <div id="kafkaTopicSendStatus" class="mb-3"></div>
                    <div class="form-group">
                        <label for="kafkaTopicMessageBody">Message</label>
                        <textarea id="kafkaTopicMessageBody" class="form-control compact-textarea" rows="7"></textarea>
                    </div>
                    <button id="sendKafkaTopicMessage" type="button" class="btn btn-primary"
                            data-command-template="printf '%s\n' '{#kafkaTopicMessageBody}' | docker compose exec -T kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic {#kafkaTopicName}">Send</button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">
                    <span>Partitions</span>
                    <button id="refreshKafkaPartitions" type="button" class="btn btn-secondary btn-sm"
                            data-command-template="docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic {#kafkaTopicName}&#10;docker compose exec kafka /opt/kafka/bin/kafka-get-offsets.sh --bootstrap-server localhost:9092 --topic {#kafkaTopicName} --time -1">
                        Refresh
                    </button>
                </div>
                <div class="card-body">
                    <div id="kafkaPartitionSummary" class="mb-3"></div>
                    <div id="kafkaPartitionVisual"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/kafkaTopic.js"></script>
</html>
