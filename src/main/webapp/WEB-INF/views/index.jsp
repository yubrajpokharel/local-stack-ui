<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Localstack</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
</head>
<body>
<div class="container">
    <div class="row">
        <div class="col-lg-6">
            <div class="row">
                <div class="col-lg-12">
                    <img style="height: 100px; margin: 10px" class="img-fluid" alt="SNS" src="https://static.mparticle.com/providerimg/forwarderlogo_93_AmazonSNS.svg" />
                </div>
                <div class="col-lg-12" id="snsList"></div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="row">
                <div class="col-lg-12">
                    <img  style="height: 100px; margin: 10px" class="img-fluid" alt="SQS" src="https://panoply.io/images/integration-logos/sqs.svg" />
                </div>
                <div class="col-lg-12" id="sqsList"></div>
            </div>
        </div>
    </div>

    <div class="row">&nbsp;</div>

    <div class="row">
        <div class="col-lg-6">
            <div class="card">
                <div class="card-header">
                    Create Topic
                </div>
                <div class="card-body">
                    <div class="input-group" style="margin-bottom: 5px">
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
            <div class="card">
                <div class="card-header">
                    Create Queue
                </div>
                <div class="card-body">
                    <div class="input-group" style="margin-bottom: 5px">
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

    <div class="row">&nbsp;</div>

    <div class="row">
        <div class="col-lg-12">
            <div class="card">
                <div class="card-header">
                    Subscription
                </div>
                <div class="card-body">
                    <div class="input-group" style="margin-bottom: 5px">
                        <div class="input-group">
                            <span class="input-group-text"> Queue </span>
                            <select class="form-control" id="queueOptions">
                            </select>&nbsp;&nbsp;
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
<script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/onStartUp.js"></script>
</html>