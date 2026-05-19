<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Localstack</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Messaging" src="resources/images/sns.png" />
        <div>
            <h2 class="font-weight-bold">Messaging Services</h2>
            <span class="badge badge-primary">SNS</span>
            <span class="badge badge-secondary">SQS</span>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-6">
            <div class="card app-panel">
                <div class="card-header">
                    <div class="service-card-header">
                        <img class="img-fluid" alt="SNS" src="resources/images/sns.png" />
                        <span>Topics</span>
                    </div>
                </div>
                <div class="card-body resource-list" id="snsList"></div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="card app-panel">
                <div class="card-header">
                    <div class="service-card-header">
                        <img class="img-fluid" alt="SQS" src="resources/images/sqs.png" />
                        <span>Queues</span>
                    </div>
                </div>
                <div class="card-body resource-list" id="sqsList"></div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-6">
            <div class="card app-panel">
                <div class="card-header">
                    Create Topic
                </div>
                <div class="card-body">
                    <div class="input-group mb-3">
                        <div class="input-group-prepend">
                            <span class="input-group-text">New Topic name</span>
                        </div>
                        <input id="topicName" type="text" aria-label="topic name" class="form-control">
                    </div>
                    <button id="createTopic" href="#" class="btn btn-primary">Create</button>
                </div>
            </div>
        </div>


        <div class="col-lg-6">
            <div class="card app-panel">
                <div class="card-header">
                    Create Queue
                </div>
                <div class="card-body">
                    <div class="input-group mb-3">
                        <div class="input-group-prepend">
                            <span class="input-group-text">New Queue name</span>
                        </div>
                        <input id="queueName" type="text" aria-label="queue name" class="form-control">
                    </div>
                    <button id="createQueue" href="#" class="btn btn-primary">Create</button>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-12">
            <div class="card app-panel">
                <div class="card-header">
                    Subscription
                </div>
                <div class="card-body">
                    <div class="input-group mb-3">
                        <div class="input-group flex-wrap">
                            <span class="input-group-text"> Queue </span>
                            <select class="form-control" id="queueOptions">
                            </select>
                            <span class="input-group-text"> to Topic </span>
                            <select class="form-control" id="topicOptions">
                            </select>
                        </div>
                    </div>
                    <button id="enroll" href="#" class="btn btn-primary">Enroll</button>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="resources/js/onStartUp.js"></script>
</html>
