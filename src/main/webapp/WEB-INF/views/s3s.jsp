<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Localstack</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
</head>
<body>
<div class="container">
    <a class="mt-5" href="/" title="home">
        <img style="height: 20px; margin: 10px" class="img-fluid" alt="SNS" src="resources/images/awstool.png" />
        &nbsp;Home</a>

    <h2 class="font-weight-bold text-center text-lg-left mt-4 mb-0">S3 Service</h2>
    <hr class="mt-2 mb-5">

    <div class="row">
        <div class="col-lg-6">
            <div class="row">
                <div class="col-lg-12 text-left">
                    <img style="height: 100px; margin: 10px" class="img-fluid img-thumbnail" alt="SNS" src="resources/images/s3.png" />
                </div>
                <div class="col-lg-12" id="bucketList"></div>
            </div>
        </div>
    </div>

    <div class="row">&nbsp;</div>

    <div class="row">
        <div class="col-lg-12">
            <div class="card">
                <div class="card-header">
                    Create Bucket
                </div>
                <div class="card-body">
                    <div class="input-group" style="margin-bottom: 5px">
                        <div class="input-group-prepend">
                            <span class="input-group-text">New Bucket name</span>
                        </div>
                        <input id="bucketName" type="text" aria-label="bucket name" class="form-control">
                    </div>
                    <button id="createBucket" href="#" class="btn btn-primary mt-2">Create</button>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
<script type="text/javascript" src="resources/js/initiateS3.js"></script>
</html>