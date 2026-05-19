<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Mock HTTP</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Mock HTTP" src="resources/images/http.png" />
        <div>
            <h2 class="font-weight-bold">Mock HTTP Services</h2>
            <span class="badge badge-dark">localhost</span>
        </div>
    </div>

    <div id="mockMessage" class="mt-3 mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Configure Mock</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="mockName">Name</label>
                        <input id="mockName" type="text" class="form-control" value="mock-service" aria-label="mock name">
                    </div>
                    <div class="form-group">
                        <label for="mockPort">Port</label>
                        <input id="mockPort" type="number" class="form-control" value="8080" min="1" max="65535" aria-label="mock port">
                    </div>
                    <div class="form-group">
                        <label for="mockResponse">Response text</label>
                        <textarea id="mockResponse" class="form-control" rows="8" aria-label="mock response">ok</textarea>
                    </div>
                    <div class="action-row">
                        <button id="startMock" class="btn btn-primary">Start</button>
                        <button id="restartMock" class="btn btn-secondary">Restart</button>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>Running Mocks</span>
                    <span id="mockCount" class="badge badge-light">0</span>
                </div>
                <div class="card-body">
                    <div class="action-row mb-3">
                        <button id="refreshMocks" class="btn btn-secondary btn-sm">Refresh</button>
                        <button id="stopAllMocks" class="btn btn-danger btn-sm">Stop all</button>
                    </div>
                    <div id="mockServerList"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="resources/js/mockHttp.js"></script>
</html>
