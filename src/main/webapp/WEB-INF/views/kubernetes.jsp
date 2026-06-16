<!DOCTYPE html>
<html lang="en">
<head>
    <title>${providerName} Kubernetes</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="/resources/images/awstool.png" />
        Home</a>

    <div class="page-header">
        <img class="img-fluid" alt="Kubernetes" src="/resources/images/kubernetes.svg" />
        <div>
            <h2 class="font-weight-bold">${providerName} Kubernetes</h2>
            <span class="badge badge-primary">kubectl helper</span>
        </div>
    </div>

    <input type="hidden" id="kubernetesProvider" value="${provider}">

    <div id="kubernetesMessage" class="mb-3"></div>

    <div class="card app-panel">
        <div class="card-header">
            <button class="btn btn-link p-0 text-left font-weight-bold" type="button"
                    data-toggle="collapse" data-target="#dockerDesktopKubernetesGuide"
                    aria-expanded="false" aria-controls="dockerDesktopKubernetesGuide">
                Docker Desktop Kubernetes setup
            </button>
        </div>
        <div id="dockerDesktopKubernetesGuide" class="collapse">
            <div class="card-body">
                <ol class="mb-3">
                    <li>Open Docker Desktop.</li>
                    <li>Go to Settings.</li>
                    <li>Open Kubernetes.</li>
                    <li>Enable Kubernetes.</li>
                    <li>Click Apply &amp; Restart.</li>
                    <li>Wait until Docker Desktop shows Kubernetes as running.</li>
                    <li>Run the commands below, then click Refresh on this page.</li>
                </ol>
                <pre class="command-reference">kubectl config get-contexts
kubectl config use-context docker-desktop
kubectl get nodes</pre>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Connection</div>
                <div class="card-body">
                    <div id="kubernetesStatus" class="mb-3"></div>
                    <div class="action-row">
                        <button id="refreshKubernetes" class="btn btn-secondary"
                                data-command="kubectl config current-context&#10;kubectl version --client -o json">
                            Refresh
                        </button>
                        <button id="loadKubernetesEvents" class="btn btn-secondary"
                                data-command-template="kubectl get events -n {#kubernetesNamespace} --sort-by=.lastTimestamp">
                            Events
                        </button>
                    </div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Provider Commands</div>
                <div class="card-body">
                    <div class="resource-meta mb-2">Connect cluster</div>
                    <pre class="code-cell">${connectCommand}</pre>
                    <div class="resource-meta mb-2">List clusters</div>
                    <pre class="code-cell">${listClustersCommand}</pre>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Namespace</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="kubernetesNamespace">Namespace</label>
                        <select id="kubernetesNamespace" class="form-control"></select>
                    </div>
                    <button id="loadKubernetesOverview" class="btn btn-primary"
                            data-command-template="kubectl get pods,deployments,services -n {#kubernetesNamespace}">
                        Load Namespace
                    </button>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Create Service</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="kubernetesServiceDeployment">Deployment</label>
                        <select id="kubernetesServiceDeployment" class="form-control" disabled></select>
                    </div>
                    <div class="form-group">
                        <label for="kubernetesServiceName">Service name</label>
                        <input id="kubernetesServiceName" class="form-control" type="text" placeholder="spring6" disabled>
                    </div>
                    <div class="form-group">
                        <label for="kubernetesServiceType">Service type</label>
                        <select id="kubernetesServiceType" class="form-control" disabled>
                            <option value="ClusterIP">ClusterIP</option>
                            <option value="NodePort">NodePort</option>
                            <option value="LoadBalancer">LoadBalancer</option>
                        </select>
                    </div>
                    <div class="form-row">
                        <div class="form-group col-6">
                            <label for="kubernetesServicePort">Port</label>
                            <input id="kubernetesServicePort" class="form-control" type="number" min="1" max="65535" value="8080" disabled>
                        </div>
                        <div class="form-group col-6">
                            <label for="kubernetesServiceTargetPort">Target port</label>
                            <input id="kubernetesServiceTargetPort" class="form-control" type="number" min="1" max="65535" value="8080" disabled>
                        </div>
                    </div>
                    <button id="createKubernetesService" class="btn btn-primary" disabled
                            data-command-template="kubectl expose deployment {#kubernetesServiceDeployment} -n {#kubernetesNamespace} --name {#kubernetesServiceName} --type {#kubernetesServiceType} --port {#kubernetesServicePort} --target-port {#kubernetesServiceTargetPort}">
                        Create Service
                    </button>
                </div>
            </div>
        </div>
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Pods</div>
                <div class="card-body">
                    <div id="kubernetesPods"></div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Deployments</div>
                <div class="card-body">
                    <div id="kubernetesDeployments"></div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Services</div>
                <div class="card-body">
                    <div id="kubernetesServices"></div>
                </div>
            </div>
            <div class="card app-panel">
                <div class="card-header">Output</div>
                <div class="card-body">
                    <pre id="kubernetesOutput" class="code-cell mb-0">Select a namespace or pod action.</pre>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script src="/webjars/popper.js/1.14.7/umd/popper.min.js"></script>
<script src="/webjars/bootstrap/4.3.1/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/resources/js/commandHints.js"></script>
<script type="text/javascript" src="/resources/js/kubernetes.js"></script>
</html>
