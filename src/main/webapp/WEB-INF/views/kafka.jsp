<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Kafka</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Kafka" src="resources/images/kafka.svg" />
        <div>
            <h2 class="font-weight-bold">Kafka</h2>
            <span class="badge badge-primary">Event streaming</span>
        </div>
    </div>

    <div id="kafkaMessage" class="mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Connection</div>
                <div class="card-body">
                    <div id="kafkaStatus" class="mb-3"></div>
                    <div class="action-row">
                        <button id="refreshKafka" class="btn btn-secondary">Refresh</button>
                        <button id="startKafka" class="btn btn-primary" style="display: none;">Start Kafka</button>
                        <button id="stopKafka" class="btn btn-danger" style="display: none;">Stop Kafka</button>
                    </div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Create topic</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="kafkaTopicName">Topic name</label>
                        <input id="kafkaTopicName" class="form-control" type="text" placeholder="orders-created" disabled>
                    </div>
                    <div class="form-group">
                        <label for="kafkaPartitions">Partitions</label>
                        <input id="kafkaPartitions" class="form-control" type="number" min="1" value="1" disabled>
                    </div>
                    <div class="form-group">
                        <label for="kafkaReplicationFactor">Replication factor</label>
                        <input id="kafkaReplicationFactor" class="form-control" type="number" min="1" value="1" disabled>
                    </div>
                    <button id="createKafkaTopic" class="btn btn-primary" disabled>Create Topic</button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Topics</div>
                <div class="card-body">
                    <div id="kafkaTopics"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="resources/js/kafka.js"></script>
</html>
