<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>GCP Firestore</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="/resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Firestore" src="/resources/images/firestore.svg" />
        <div>
            <h2 class="font-weight-bold">GCP Firestore</h2>
            <span class="badge badge-primary">Document database emulator</span>
        </div>
    </div>

    <div id="firestoreMessage" class="mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Connection</div>
                <div class="card-body">
                    <div id="firestoreStatus" class="mb-3"></div>
                    <div class="action-row">
                        <button id="refreshFirestore" class="btn btn-secondary"
                                data-command="curl -X POST http://localhost:8787/v1/projects/localstack-ui/databases/(default)/documents:listCollectionIds -H 'Authorization: Bearer owner' -H 'Content-Type: application/json' -d '{&quot;pageSize&quot;:100}'">Refresh</button>
                        <button id="startFirestore" class="btn btn-primary" style="display: none;"
                                data-command="docker compose up -d gcp-firestore">Start Firestore</button>
                        <button id="stopFirestore" class="btn btn-danger" style="display: none;"
                                data-command="docker compose stop gcp-firestore">Stop Firestore</button>
                    </div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Create document</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="firestoreCollectionName">Collection</label>
                        <input id="firestoreCollectionName" class="form-control" type="text" placeholder="orders" disabled>
                    </div>
                    <div class="form-group">
                        <label for="firestoreDocumentName">Document ID</label>
                        <input id="firestoreDocumentName" class="form-control" type="text" placeholder="order-1" disabled>
                    </div>
                    <div class="form-group">
                        <label for="firestoreDocumentContent">JSON fields</label>
                        <textarea id="firestoreDocumentContent" class="form-control compact-textarea" rows="8" disabled>{"status":"created"}</textarea>
                    </div>
                    <button id="createFirestoreDocument" class="btn btn-primary" disabled
                            data-command-template="curl -X POST 'http://localhost:8787/v1/projects/localstack-ui/databases/(default)/documents/{#firestoreCollectionName}?documentId={#firestoreDocumentName}' -H 'Authorization: Bearer owner' -H 'Content-Type: application/json' -d '{&quot;fields&quot;:{}}'">Create Document</button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Collections</div>
                <div class="card-body">
                    <div id="firestoreCollections"></div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Documents</div>
                <div class="card-body">
                    <div id="firestoreDocuments"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/gcpFirestore.js"></script>
</html>
