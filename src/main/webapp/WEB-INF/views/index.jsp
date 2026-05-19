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

    <div class="page-header">
        <img class="img-fluid" alt="Localstack" src="resources/images/awstool.png" />
        <div>
            <h1 class="font-weight-bold">Localstack Services</h1>
            <span class="badge badge-primary">Control panel</span>
        </div>
    </div>

    <div class="row service-grid">

        <div class="col-lg-3 col-md-4 col-6">
            <a href="/messaging" class="service-tile">
                <img class="img-fluid" alt="SNS" src="resources/images/sns.png" /> <br>
                <span class="badge badge-primary">SNS</span>
            </a>
        </div>
        <div class="col-lg-3 col-md-4 col-6">
            <a href="/messaging" class="service-tile">
                <img class="img-fluid" alt="SQS" src="resources/images/sqs.png" /> <br>
                <span class="badge badge-primary">SQS</span>
            </a>
        </div>
        <div class="col-lg-3 col-md-4 col-6">
            <a href="/s3" class="service-tile">
                <img class="img-fluid" alt="S3" src="resources/images/s3.png"> <br>
                <span class="badge badge-primary">S3</span>
            </a>
        </div>
        <div class="col-lg-3 col-md-4 col-6">
            <a href="/redis" class="service-tile">
                <img class="img-fluid" alt="Redis" src="resources/images/cache.png" /> <br>
                <span class="badge badge-primary">Redis</span>
            </a>
        </div>
        <div class="col-lg-3 col-md-4 col-6">
            <a href="/mock-http" class="service-tile">
                <img class="img-fluid" alt="Mock HTTP" src="resources/images/http.png" /> <br>
                <span class="badge badge-primary">Mock HTTP</span>
            </a>
        </div>
    </div>

</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
</html>
