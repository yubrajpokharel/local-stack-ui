<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <title>Command Runner</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Command Runner" src="resources/images/terminal.svg" />
        <div>
            <h2 class="font-weight-bold">Command Runner</h2>
            <span class="badge badge-dark">Maven, Gradle, Docker, kubectl</span>
        </div>
    </div>

    <div id="commandRunnerMessage" class="mb-3"></div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Run Command</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="commandRunnerCwd">Working directory</label>
                        <input id="commandRunnerCwd" class="form-control" list="commandRunnerDirectories"
                               placeholder="/Users/s2151621/Desktop/GitTerm/local-stack-ui">
                        <datalist id="commandRunnerDirectories"></datalist>
                        <small class="form-text text-muted">Must be under an allowed directory.</small>
                    </div>
                    <div class="form-group">
                        <label for="commandTemplate">Command template</label>
                        <select id="commandTemplate" class="form-control">
                            <option value="">Custom command</option>
                            <option value="pwd">Print current directory</option>
                            <option value="ls -la">List files</option>
                            <option value="./mvnw clean package">Maven wrapper build</option>
                            <option value="mvn clean package">Maven build</option>
                            <option value="./gradlew clean build">Gradle wrapper build</option>
                            <option value="gradle clean build">Gradle build</option>
                            <option value="docker build -t spring6:local .">Docker build local image</option>
                            <option value="kubectl get pods -A">Kubernetes pods</option>
                            <option value="kubectl get services -A">Kubernetes services</option>
                            <option value="git status --short">Git status</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="commandInput">Command</label>
                        <textarea id="commandInput" class="form-control compact-textarea" rows="4"
                                  placeholder="./gradlew clean build"></textarea>
                    </div>
                    <div class="action-row">
                        <button id="runCommand" type="button" class="btn btn-primary"
                                data-command-template="{#commandInput}">Run</button>
                        <button id="clearCommandOutput" type="button" class="btn btn-secondary">Clear Output</button>
                    </div>
                </div>
            </div>

            <div class="card app-panel">
                <div class="card-header">Allowed Commands</div>
                <div class="card-body">
                    <div class="command-chip-list">
                        <span class="badge badge-light">pwd</span>
                        <span class="badge badge-light">ls</span>
                        <span class="badge badge-light">mvn</span>
                        <span class="badge badge-light">./mvnw</span>
                        <span class="badge badge-light">gradle</span>
                        <span class="badge badge-light">./gradlew</span>
                        <span class="badge badge-light">docker</span>
                        <span class="badge badge-light">kubectl</span>
                        <span class="badge badge-light">curl</span>
                        <span class="badge badge-light">java</span>
                        <span class="badge badge-light">git status/log/branch/diff</span>
                    </div>
                    <p class="resource-meta mb-0 mt-3">
                        Configure directories with command.runner.allowed.directories or COMMAND_RUNNER_ALLOWED_DIRECTORIES.
                    </p>
                </div>
            </div>
        </div>

        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>Output</span>
                    <span id="commandRunnerExitCode" class="badge badge-light">Not run</span>
                </div>
                <div class="card-body">
                    <pre id="commandOutput" class="command-output">Choose a directory and run a command.</pre>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/commandRunner.js"></script>
</html>
