<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Secrets Manager</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Secrets Manager" src="resources/images/secrets.svg" />
        <div>
            <h2 class="font-weight-bold">Secrets Manager</h2>
            <span class="badge badge-primary">AWS LocalStack</span>
        </div>
    </div>

    <div id="secretsMessage" class="mb-3"></div>

    <div class="card app-panel">
        <div class="card-header">AWS LocalStack</div>
        <div class="card-body">
            <div id="secretsAwsStatus" class="mb-3"></div>
            <div class="action-row">
                <button id="refreshSecretsAws" class="btn btn-secondary"
                        data-command="curl http://localhost:8085/localstack/status">Refresh</button>
                <button id="startSecretsAws" class="btn btn-primary" style="display: none;"
                        data-command="docker compose up -d localstack">Start AWS Services</button>
                <button id="stopSecretsAws" class="btn btn-danger" style="display: none;"
                        data-command="docker compose stop localstack">Stop AWS Services</button>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Create Secret</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="secretName">Name</label>
                        <input id="secretName" class="form-control" type="text" placeholder="dev/db/password" disabled>
                    </div>
                    <div class="form-group">
                        <label for="secretDescription">Description</label>
                        <input id="secretDescription" class="form-control" type="text" placeholder="Local dev database password" disabled>
                    </div>
                    <div class="form-group">
                        <label for="secretValue">Secret value</label>
                        <textarea id="secretValue" class="form-control compact-textarea" rows="7" disabled></textarea>
                    </div>
                    <button id="createSecret" class="btn btn-primary" disabled
                            data-command-template="awslocal secretsmanager create-secret --name {#secretName} --description '{#secretDescription}' --secret-string '{#secretValue}'">Create Secret</button>
                </div>
            </div>

            <div class="card app-panel">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>Selected Secret</span>
                    <span id="selectedSecretName" class="badge badge-light">None</span>
                </div>
                <div class="card-body">
                    <input id="selectedSecretId" type="hidden">
                    <div class="form-group">
                        <label for="selectedSecretValue">Current value</label>
                        <textarea id="selectedSecretValue" class="form-control compact-textarea" rows="8" disabled></textarea>
                    </div>
                    <div class="action-row">
                        <button id="loadSecretValue" class="btn btn-secondary" disabled
                                data-command-template="awslocal secretsmanager get-secret-value --secret-id {#selectedSecretId}">Load Value</button>
                        <button id="updateSecretValue" class="btn btn-primary" disabled
                                data-command-template="awslocal secretsmanager put-secret-value --secret-id {#selectedSecretId} --secret-string '{#selectedSecretValue}'">Update Value</button>
                        <button id="deleteSecret" class="btn btn-danger" disabled
                                data-command-template="awslocal secretsmanager delete-secret --secret-id {#selectedSecretId} --force-delete-without-recovery">Delete</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Secrets</div>
                <div class="card-body">
                    <div id="secretsList"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/secretsManager.js"></script>
</html>
