<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>GCP Cloud Storage</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="/resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Cloud Storage" src="/resources/images/gcs.svg" />
        <div>
            <h2 class="font-weight-bold">GCP Cloud Storage</h2>
            <span class="badge badge-primary">Storage emulator</span>
        </div>
    </div>

    <div id="storageMessage" class="mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Connection</div>
                <div class="card-body">
                    <div id="storageStatus" class="mb-3"></div>
                    <div class="action-row">
                        <button id="refreshStorage" class="btn btn-secondary">Refresh</button>
                        <button id="startStorage" class="btn btn-primary" style="display: none;">Start Storage</button>
                        <button id="stopStorage" class="btn btn-danger" style="display: none;">Stop Storage</button>
                    </div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Create bucket</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="storageBucketName">Bucket name</label>
                        <input id="storageBucketName" class="form-control" type="text" placeholder="orders-data" disabled>
                    </div>
                    <button id="createStorageBucket" class="btn btn-primary" disabled>Create Bucket</button>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Upload text object</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="storageObjectBucket">Bucket</label>
                        <select id="storageObjectBucket" class="form-control" disabled></select>
                    </div>
                    <div class="form-group">
                        <label for="storageObjectName">Object name</label>
                        <input id="storageObjectName" class="form-control" type="text" placeholder="sample.txt" disabled>
                    </div>
                    <div class="form-group">
                        <label for="storageObjectContent">Object content</label>
                        <textarea id="storageObjectContent" class="form-control" rows="5" disabled></textarea>
                    </div>
                    <button id="uploadStorageObject" class="btn btn-primary" disabled>Upload Object</button>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Upload file</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="storageFileBucket">Bucket</label>
                        <select id="storageFileBucket" class="form-control" disabled></select>
                    </div>
                    <div class="form-group">
                        <label for="storageFileObjectName">Object name</label>
                        <input id="storageFileObjectName" class="form-control" type="text" placeholder="leave blank to use file name" disabled>
                    </div>
                    <div class="form-group">
                        <label for="storageFile">File</label>
                        <input id="storageFile" class="form-control-file" type="file" disabled>
                    </div>
                    <button id="uploadStorageFile" class="btn btn-primary" disabled>Upload File</button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Buckets</div>
                <div class="card-body">
                    <div id="storageBuckets"></div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Objects</div>
                <div class="card-body">
                    <div id="storageObjects"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/gcpStorage.js"></script>
</html>
