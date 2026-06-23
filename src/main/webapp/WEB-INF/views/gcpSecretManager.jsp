<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>GCP Secret Manager</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="/resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="GCP Secret Manager" src="/resources/images/secrets.svg" />
        <div>
            <h2 class="font-weight-bold">GCP Secret Manager</h2>
            <span class="badge badge-primary">Local developer store</span>
        </div>
    </div>

    <div id="gcpSecretsMessage" class="mb-3"></div>

    <div class="card app-panel">
        <div class="card-header">Connection</div>
        <div class="card-body">
            <div id="gcpSecretsStatus" class="mb-3"></div>
            <div class="action-row">
                <button id="refreshGcpSecrets" class="btn btn-secondary"
                        data-command="gcloud secrets list --project=localstack-ui">Refresh</button>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Create Secret</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="gcpSecretId">Secret ID</label>
                        <input id="gcpSecretId" class="form-control" type="text" placeholder="db_password">
                    </div>
                    <div class="form-group">
                        <label for="gcpSecretValue">Secret value</label>
                        <textarea id="gcpSecretValue" class="form-control compact-textarea" rows="7"></textarea>
                    </div>
                    <button id="createGcpSecret" class="btn btn-primary"
                            data-command-template="gcloud secrets create {#gcpSecretId} --replication-policy=automatic --data-file=/path/to/value --project=localstack-ui">Create Secret</button>
                </div>
            </div>

            <div class="card app-panel">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>Selected Secret</span>
                    <span id="selectedGcpSecretName" class="badge badge-light">None</span>
                </div>
                <div class="card-body">
                    <input id="selectedGcpSecretId" type="hidden">
                    <div class="form-group">
                        <label for="selectedGcpSecretValue">Latest value</label>
                        <textarea id="selectedGcpSecretValue" class="form-control compact-textarea" rows="8" disabled></textarea>
                    </div>
                    <div class="action-row">
                        <button id="loadGcpSecretValue" class="btn btn-secondary" disabled
                                data-command-template="gcloud secrets versions access latest --secret={#selectedGcpSecretId} --project=localstack-ui">Load Value</button>
                        <button id="updateGcpSecretValue" class="btn btn-primary" disabled
                                data-command-template="gcloud secrets versions add {#selectedGcpSecretId} --data-file=/path/to/value --project=localstack-ui">Add Version</button>
                        <button id="deleteGcpSecret" class="btn btn-danger" disabled
                                data-command-template="gcloud secrets delete {#selectedGcpSecretId} --quiet --project=localstack-ui">Delete</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Secrets</div>
                <div class="card-body">
                    <div id="gcpSecretsList"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/gcpSecretManager.js"></script>
</html>
