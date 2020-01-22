<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Localstack</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
</head>
<body>
<div class="container">

    <h1 class="font-weight-bold text-center text-lg-left mt-4 mb-0">Localstack Services</h1>
    <hr class="mt-2 mb-5">

    <div class="row text-center">

        <div class="col-lg-4 col-md-4 col-6">
            <a href="/messaging" class="mb-4 h-100">
                <img style="height: 150px; margin: 10px" class="img-fluid img-thumbnail" alt="SNS" src="resources/images/sns.png" /> <br>
            </a>
            <span class="badge badge-primary">SNS</span>
        </div>
        <div class="col-lg-4 col-md-4 col-6">
            <a href="/messaging" class="mb-4 h-100">
                <img  style="height: 150px; margin: 10px" class="img-fluid img-thumbnail" alt="SQS" src="resources/images/sqs.png" /> <br>
            </a>
            <span class="badge badge-primary">SQS</span>
        </div>
        <div class="col-lg-4 col-md-4 col-6">
            <a href="/s3" class="mb-4 h-100">
                <img style="height: 150px; margin: 10px" class="img-fluid img-thumbnail" alt="S3"  src="resources/images/s3.png"> <br>
            </a>
            <span class="badge badge-primary">S3</span>
        </div>
    </div>

</div>
</body>
<script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
</html>