<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>GCP Pub/Sub</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="/resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Pub/Sub" src="/resources/images/pubsub.svg" />
        <div>
            <h2 class="font-weight-bold">GCP Pub/Sub</h2>
            <span class="badge badge-primary">Messaging emulator</span>
        </div>
    </div>

    <div id="pubsubMessage" class="mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Connection</div>
                <div class="card-body">
                    <div id="pubsubStatus" class="mb-3"></div>
                    <div class="action-row">
                        <button id="refreshPubSub" class="btn btn-secondary"
                                data-command="curl http://localhost:8085/gcp/pubsub/status">Refresh</button>
                        <button id="startPubSub" class="btn btn-primary" style="display: none;"
                                data-command="docker compose up -d gcp-pubsub">Start Pub/Sub</button>
                        <button id="stopPubSub" class="btn btn-danger" style="display: none;"
                                data-command="docker compose stop gcp-pubsub">Stop Pub/Sub</button>
                    </div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Create topic</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="pubsubTopicName">Topic name</label>
                        <input id="pubsubTopicName" class="form-control" type="text" placeholder="orders-created" disabled>
                    </div>
                    <button id="createPubSubTopic" class="btn btn-primary" disabled
                            data-command-template="docker compose exec gcp-pubsub env PUBSUB_EMULATOR_HOST=localhost:8681 CLOUDSDK_API_ENDPOINT_OVERRIDES_PUBSUB=http://localhost:8681/ gcloud pubsub topics create {#pubsubTopicName} --project=localstack-ui">Create Topic</button>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Create subscription</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="pubsubSubscriptionName">Subscription name</label>
                        <input id="pubsubSubscriptionName" class="form-control" type="text" placeholder="orders-worker" disabled>
                    </div>
                    <div class="form-group">
                        <label for="pubsubTopicOptions">Topic</label>
                        <select id="pubsubTopicOptions" class="form-control" disabled></select>
                    </div>
                    <button id="createPubSubSubscription" class="btn btn-primary" disabled
                            data-command-template="docker compose exec gcp-pubsub env PUBSUB_EMULATOR_HOST=localhost:8681 CLOUDSDK_API_ENDPOINT_OVERRIDES_PUBSUB=http://localhost:8681/ gcloud pubsub subscriptions create {#pubsubSubscriptionName} --topic={#pubsubTopicOptions} --project=localstack-ui">Create Subscription</button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Topics</div>
                <div class="card-body">
                    <div id="pubsubTopics"></div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Subscriptions</div>
                <div class="card-body">
                    <div id="pubsubSubscriptions"></div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Publish message</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="pubsubPublishTopic">Topic</label>
                        <select id="pubsubPublishTopic" class="form-control" disabled></select>
                    </div>
                    <div class="form-group">
                        <label for="pubsubMessageBody">Message</label>
                        <textarea id="pubsubMessageBody" class="form-control" rows="5" disabled></textarea>
                    </div>
                    <button id="publishPubSubMessage" class="btn btn-primary" disabled
                            data-command-template="docker compose exec gcp-pubsub env PUBSUB_EMULATOR_HOST=localhost:8681 CLOUDSDK_API_ENDPOINT_OVERRIDES_PUBSUB=http://localhost:8681/ gcloud pubsub topics publish {#pubsubPublishTopic} --message='{#pubsubMessageBody}' --project=localstack-ui">Publish Message</button>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/gcpPubSub.js"></script>
</html>
