<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
    <title>${topicName}</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/gcp/pubsub" title="Pub/Sub">
        <img class="img-fluid" alt="Pub/Sub" src="/resources/images/pubsub.svg" />
        Pub/Sub</a>

    <div class="page-header">
        <img class="img-fluid" alt="Pub/Sub" src="/resources/images/pubsub.svg" />
        <div>
            <h2 class="font-weight-bold">Pub/Sub Topic</h2>
            <span class="resource-name">${topicName}</span>
        </div>
    </div>

    <input type="hidden" id="topicName" value="${topicName}">

    <div class="row">
        <div class="col-lg-12 mb-4">
            <div class="card app-panel">
                <div class="card-header">Topic Details</div>
                <div class="card-body">
                    <div><strong>Topic:</strong> <span class="code-cell">${topicName}</span></div>
                    <div><strong>URL:</strong> <span class="code-cell">${topicAddress}</span></div>
                    <div><strong>CreatedOn:</strong> <span class="code-cell">${topicCreatedOn}</span></div>
                </div>
            </div>
        </div>
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Send Message</div>
                <div class="card-body">
                    <div id="topicSendStatus" class="mb-3"></div>
                    <div class="form-group">
                        <label for="topicMessageBody">Message</label>
                        <textarea id="topicMessageBody" class="form-control compact-textarea" rows="7"></textarea>
                    </div>
                    <button id="sendTopicMessage" type="button" class="btn btn-primary"
                            data-command-template="docker compose exec gcp-pubsub env PUBSUB_EMULATOR_HOST=localhost:8681 CLOUDSDK_API_ENDPOINT_OVERRIDES_PUBSUB=http://localhost:8681/ gcloud pubsub topics publish {#topicName} --message='{#topicMessageBody}' --project=localstack-ui">Send</button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">
                    <span>Subscriptions</span>
                    <button id="refreshTopicSubscriptions" type="button" class="btn btn-secondary btn-sm"
                            data-command-template="docker compose exec gcp-pubsub env PUBSUB_EMULATOR_HOST=localhost:8681 CLOUDSDK_API_ENDPOINT_OVERRIDES_PUBSUB=http://localhost:8681/ gcloud pubsub subscriptions list --filter='topic:{#topicName}' --project=localstack-ui">
                        Refresh
                    </button>
                </div>
                <div class="card-body">
                    <table class="table table-striped table-sm">
                        <thead>
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col">Subscription</th>
                            <th scope="col">URL</th>
                            <th scope="col">CreatedOn</th>
                        </tr>
                        </thead>
                        <tbody id="topicSubscriptionRows">
                        <tr>
                            <td colspan="4" class="text-muted">Loading subscriptions...</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/gcpPubSubTopic.js"></script>
</html>
