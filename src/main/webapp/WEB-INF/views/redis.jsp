<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Redis</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Redis" src="resources/images/cache.png" />
        <div>
            <h2 class="font-weight-bold">Redis</h2>
            <span class="badge badge-primary">Key value store</span>
        </div>
    </div>

    <div id="redisMessage" class="mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Keys</div>
                <div class="card-body">
                    <div class="action-row mb-3">
                        <button id="refreshRedis" class="btn btn-secondary"
                                data-command="redis-cli -h localhost -p 6379 ping">Refresh</button>
                        <button id="startRedis" class="btn btn-primary" style="display: none;"
                                data-command="docker compose up -d redis">Start Redis</button>
                        <button id="stopRedis" class="btn btn-danger" style="display: none;"
                                data-command="docker compose stop redis">Stop Redis</button>
                    </div>
                    <div id="redisStatus" class="mb-3"></div>
                    <div id="redisKeyList"></div>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Value</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="redisKey">Key</label>
                        <input id="redisKey" type="text" class="form-control" aria-label="redis key" disabled>
                    </div>
                    <div class="form-group">
                        <label for="redisValue">Value</label>
                        <textarea id="redisValue" class="form-control" rows="8" aria-label="redis value" disabled></textarea>
                    </div>
                    <div class="action-row">
                        <button id="saveRedis" class="btn btn-primary" disabled
                                data-command-template="redis-cli -h localhost -p 6379 SET {#redisKey} {#redisValue}">Save</button>
                        <button id="deleteRedis" class="btn btn-danger" disabled
                                data-command-template="redis-cli -h localhost -p 6379 DEL {#redisKey}">Delete</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="resources/js/redis.js"></script>
</html>
