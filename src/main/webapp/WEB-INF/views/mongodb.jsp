<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>MongoDB</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="MongoDB" src="resources/images/mongodb.svg" />
        <div>
            <h2 class="font-weight-bold">MongoDB</h2>
            <span class="badge badge-primary">Local database</span>
        </div>
    </div>

    <div id="mongoMessage" class="mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Connection</div>
                <div class="card-body">
                    <div id="mongoStatus" class="mb-3"></div>
                    <div class="action-row">
                        <button id="refreshMongo" class="btn btn-secondary"
                                data-command="mongosh mongodb://localhost:27017 --eval 'db.adminCommand({ ping: 1 })'">Refresh</button>
                        <button id="startMongo" class="btn btn-primary" style="display: none;"
                                data-command="docker compose up -d mongodb">Start MongoDB</button>
                        <button id="stopMongo" class="btn btn-danger" style="display: none;"
                                data-command="docker compose stop mongodb">Stop MongoDB</button>
                    </div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Create database</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="mongoDatabaseName">Database name</label>
                        <input id="mongoDatabaseName" class="form-control" type="text" placeholder="orders" disabled>
                    </div>
                    <div class="form-group">
                        <label for="mongoCollectionName">First collection</label>
                        <input id="mongoCollectionName" class="form-control" type="text" placeholder="customers" disabled>
                    </div>
                    <button id="createMongoDatabase" class="btn btn-primary" disabled
                            data-command-template="mongosh mongodb://localhost:27017/{#mongoDatabaseName} --eval 'db.createCollection(&quot;{#mongoCollectionName}&quot;)'">Create DB</button>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Create collection</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="mongoCollectionDatabase">Database</label>
                        <select id="mongoCollectionDatabase" class="form-control" disabled>
                            <option value="">No databases found</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="newMongoCollectionName">Collection name</label>
                        <input id="newMongoCollectionName" class="form-control" type="text" placeholder="invoices" disabled>
                    </div>
                    <button id="createMongoCollection" class="btn btn-primary" disabled
                            data-command-template="mongosh mongodb://localhost:27017/{#mongoCollectionDatabase} --eval 'db.createCollection(&quot;{#newMongoCollectionName}&quot;)'">Create Collection</button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Databases</div>
                <div class="card-body">
                    <div id="mongoDatabases"></div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Collections</div>
                <div class="card-body">
                    <div id="mongoCollections" class="empty-state">Select a database.</div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="resources/js/mongodb.js"></script>
</html>
