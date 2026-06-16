<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
    <title>${subscriptionName}</title>
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
            <h2 class="font-weight-bold">Pub/Sub Subscription</h2>
            <span class="resource-name">${subscriptionName}</span>
        </div>
    </div>

    <input type="hidden" id="subscriptionName" value="${subscriptionName}">
    <input type="hidden" id="subscriptionTopic" value="">

    <div class="row">
        <div class="col-lg-12 mb-4">
            <div class="card app-panel">
                <div class="card-header">Subscription Details</div>
                <div class="card-body">
                    <div><strong>Subscription:</strong> <span class="code-cell">${subscriptionName}</span></div>
                    <div><strong>Topic:</strong> <span id="subscriptionTopicDisplay" class="code-cell">Loading...</span></div>
                    <div><strong>URL:</strong> <span class="code-cell">${subscriptionAddress}</span></div>
                    <div><strong>CreatedOn:</strong> <span class="code-cell">${subscriptionCreatedOn}</span></div>
                </div>
            </div>
        </div>
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Send Message</div>
                <div class="card-body">
                    <div id="subscriptionSendStatus" class="mb-3"></div>
                    <div class="form-group">
                        <label for="subscriptionMessageBody">Message</label>
                        <textarea id="subscriptionMessageBody" class="form-control compact-textarea" rows="7"></textarea>
                    </div>
                    <button id="sendSubscriptionMessage" type="button" class="btn btn-primary" disabled
                            data-command-template="docker compose exec gcp-pubsub env PUBSUB_EMULATOR_HOST=localhost:8681 CLOUDSDK_API_ENDPOINT_OVERRIDES_PUBSUB=http://localhost:8681/ gcloud pubsub topics publish {#subscriptionTopic} --message='{#subscriptionMessageBody}' --project=localstack-ui">Send</button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">
                    <span>Messages</span>
                    <button id="pollPubSubMessages" type="button" class="btn btn-secondary btn-sm"
                            data-command-template="curl -X POST http://localhost:8681/v1/projects/localstack-ui/subscriptions/{#subscriptionName}:pull -H 'Authorization: Bearer owner' -H 'Content-Type: application/json' -d '{&quot;maxMessages&quot;:10,&quot;returnImmediately&quot;:true}'">
                        Poll Messages
                    </button>
                </div>
                <div class="card-body">
                    <div id="pubsubMessageStatus" class="mb-3"></div>
                    <table class="table table-striped table-sm">
                        <thead>
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col">ID</th>
                            <th scope="col">Message</th>
                            <th scope="col">Publish Time</th>
                            <th scope="col">Attributes</th>
                            <th scope="col">Ack ID</th>
                        </tr>
                        </thead>
                        <tbody id="pubsubMessageRows">
                        <tr>
                            <td colspan="6" class="text-muted">No messages polled.</td>
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
<script type="text/javascript" src="/resources/js/gcpPubSubSubscription.js"></script>
</html>
