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
        <img class="img-fluid" alt="S3" src="resources/images/s3.png" />
        <div>
            <h2 class="font-weight-bold">S3 Service</h2>
            <span class="badge badge-primary">Buckets</span>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-6">
            <div class="card app-panel">
                <div class="card-header">
                    <div class="service-card-header">
                        <img class="img-fluid" alt="S3" src="resources/images/s3.png" />
                        <span>Buckets</span>
                    </div>
                </div>
                <div class="card-body resource-list" id="bucketList"></div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="card app-panel">
                <div class="card-header">
                    Create Bucket
                </div>
                <div class="card-body">
                    <div class="input-group mb-3">
                        <div class="input-group-prepend">
                            <span class="input-group-text">New Bucket name</span>
                        </div>
                        <input id="bucketName" type="text" aria-label="bucket name" class="form-control">
                    </div>
                    <button id="createBucket" href="#" class="btn btn-primary">Create</button>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="resources/js/initiateS3.js"></script>
</html>
