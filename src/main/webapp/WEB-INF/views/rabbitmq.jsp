<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>RabbitMQ</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="RabbitMQ" src="resources/images/rabbitmq.svg" />
        <div>
            <h2 class="font-weight-bold">RabbitMQ</h2>
            <span class="badge badge-primary">Message broker</span>
        </div>
    </div>

    <div id="rabbitMessage" class="mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Connection</div>
                <div class="card-body">
                    <div id="rabbitStatus" class="mb-3"></div>
                    <div class="action-row">
                        <button id="refreshRabbit" class="btn btn-secondary"
                                data-command="curl -u guest:guest http://localhost:15672/api/overview">Refresh</button>
                        <button id="startRabbit" class="btn btn-primary" style="display: none;"
                                data-command="docker compose up -d rabbitmq">Start RabbitMQ</button>
                        <button id="stopRabbit" class="btn btn-danger" style="display: none;"
                                data-command="docker compose stop rabbitmq">Stop RabbitMQ</button>
                    </div>
                </div>
            </div>

            <div class="card app-panel">
                <div class="card-header">Create Queue</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="rabbitQueueName">Queue name</label>
                        <input id="rabbitQueueName" class="form-control" type="text" placeholder="orders.created" disabled>
                    </div>
                    <div class="form-group">
                        <label for="rabbitQueueType">Queue type</label>
                        <select id="rabbitQueueType" class="form-control" disabled>
                            <option value="classic">Classic</option>
                            <option value="quorum">Quorum</option>
                        </select>
                    </div>
                    <div class="form-check">
                        <input id="rabbitDurable" class="form-check-input" type="checkbox" checked disabled>
                        <label class="form-check-label" for="rabbitDurable">Durable</label>
                    </div>
                    <div class="form-check mb-3">
                        <input id="rabbitAutoDelete" class="form-check-input" type="checkbox" disabled>
                        <label class="form-check-label" for="rabbitAutoDelete">Auto delete</label>
                    </div>
                    <button id="createRabbitQueue" class="btn btn-primary" disabled
                            data-command-template="curl -u guest:guest -X PUT http://localhost:15672/api/queues/%2F/{#rabbitQueueName} -H 'Content-Type: application/json' -d '{&quot;durable&quot;:true,&quot;auto_delete&quot;:false,&quot;arguments&quot;:{}}'">Create Queue</button>
                </div>
            </div>

            <div class="card app-panel">
                <div class="card-header">Publish Message</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="rabbitPublishQueue">Queue</label>
                        <select id="rabbitPublishQueue" class="form-control" disabled></select>
                    </div>
                    <div class="form-group">
                        <label for="rabbitPayload">Message</label>
                        <textarea id="rabbitPayload" class="form-control" rows="5" disabled></textarea>
                    </div>
                    <button id="publishRabbitMessage" class="btn btn-primary" disabled
                            data-command-template="curl -u guest:guest -X POST http://localhost:15672/api/exchanges/%2F/amq.default/publish -H 'Content-Type: application/json' -d '{&quot;properties&quot;:{},&quot;routing_key&quot;:&quot;{#rabbitPublishQueue}&quot;,&quot;payload&quot;:&quot;{#rabbitPayload}&quot;,&quot;payload_encoding&quot;:&quot;string&quot;}'">Publish</button>
                </div>
            </div>
        </div>

        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Queues</div>
                <div class="card-body">
                    <div id="rabbitQueues"></div>
                </div>
            </div>

            <div class="card app-panel">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>Messages</span>
                    <span id="rabbitSelectedQueue" class="badge badge-light">No queue selected</span>
                </div>
                <div class="card-body">
                    <div class="form-inline mb-3">
                        <label for="rabbitPeekCount" class="mr-2">Count</label>
                        <input id="rabbitPeekCount" class="form-control form-control-sm mr-2" type="number" min="1" max="50" value="10" disabled>
                        <button id="peekRabbitMessages" class="btn btn-outline-primary btn-sm" disabled
                                data-command-template="curl -u guest:guest -X POST http://localhost:15672/api/queues/%2F/{rabbitSelectedQueue}/get -H 'Content-Type: application/json' -d '{&quot;count&quot;:{#rabbitPeekCount},&quot;ackmode&quot;:&quot;ack_requeue_true&quot;,&quot;encoding&quot;:&quot;auto&quot;}'">Peek Messages</button>
                    </div>
                    <div id="rabbitMessages" class="empty-state">Select a queue to inspect messages.</div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/rabbitmq.js"></script>
</html>
