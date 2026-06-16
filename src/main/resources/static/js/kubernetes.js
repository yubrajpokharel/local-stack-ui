$(document).ready(function () {
  var provider = $('#kubernetesProvider').val();
  var status = $('#kubernetesStatus');
  var message = $('#kubernetesMessage');
  var namespaceSelect = $('#kubernetesNamespace');
  var pods = $('#kubernetesPods');
  var deployments = $('#kubernetesDeployments');
  var services = $('#kubernetesServices');
  var output = $('#kubernetesOutput');
  var serviceControls = $('#kubernetesServiceDeployment, #kubernetesServiceName, '
      + '#kubernetesServiceType, #kubernetesServicePort, #kubernetesServiceTargetPort, '
      + '#createKubernetesService');

  setIdle();

  $('#refreshKubernetes').click(refreshKubernetes);
  $('#loadKubernetesOverview').click(loadOverview);
  $('#loadKubernetesEvents').click(loadEvents);

  $('#kubernetesServiceDeployment').change(function () {
    if (!$('#kubernetesServiceName').val()) {
      $('#kubernetesServiceName').val($(this).val());
    }
  });

  $('#createKubernetesService').click(function () {
    var namespace = namespaceSelect.val();
    var deploymentName = $('#kubernetesServiceDeployment').val();
    var serviceName = $('#kubernetesServiceName').val();
    if (!namespace || !deploymentName || !serviceName) {
      setIdleMessage("Namespace, deployment, and service name are required.");
      return;
    }
    $('#createKubernetesService').prop('disabled', true);
    $.ajax({
      url: "/kubernetes/" + encodeURIComponent(provider) + "/services",
      method: "POST",
      dataType: "json",
      data: {
        namespace: namespace,
        deploymentName: deploymentName,
        serviceName: serviceName,
        serviceType: $('#kubernetesServiceType').val(),
        port: $('#kubernetesServicePort').val(),
        targetPort: $('#kubernetesServiceTargetPort').val()
      }
    }).done(function (result) {
      message.html("<div class='alert alert-" + (result.status == "success" ? "success" : "danger")
          + "'>" + escapeHtml(result.message || result.status) + "</div>");
      if (result.status == "success") {
        loadOverview();
      }
    }).fail(function (jqXHR) {
      message.html("<div class='alert alert-danger'>" + escapeHtml(jqXHR.responseText
          || "Create service failed.") + "</div>");
    }).always(function () {
      $('#createKubernetesService').prop('disabled', !$('#kubernetesServiceDeployment').val());
    });
  });

  pods.on('click', '.load-pod-logs', function () {
    var pod = $(this).data('pod');
    var container = $(this).data('container') || "";
    var namespace = namespaceSelect.val() || "default";
    output.text("Loading logs...");
    $.ajax({
      url: "/kubernetes/" + encodeURIComponent(provider) + "/logs",
      method: "GET",
      dataType: "text",
      data: {
        namespace: namespace,
        pod: pod,
        container: container
      }
    }).done(function (result) {
      output.text(result || "No logs returned.");
    }).fail(function (jqXHR) {
      output.text(jqXHR.responseText || "Log request failed.");
    });
  });

  function refreshKubernetes() {
    message.empty();
    status.html("<span class='badge badge-secondary'>Checking...</span>");
    $.ajax({
      url: "/kubernetes/" + encodeURIComponent(provider) + "/status",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.running) {
        status.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>Context: " + escapeHtml(result.context) + "</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(result.uri) + "</span>");
        loadNamespaces();
      } else {
        status.html("<span class='badge badge-danger'>Unavailable</span>"
            + "<span class='resource-meta'>Context: " + escapeHtml(result.context) + "</span>");
        setUnavailable(result.message || "Kubernetes is not reachable.");
      }
    }).fail(function (jqXHR) {
      status.html("<span class='badge badge-danger'>Unavailable</span>");
      setUnavailable(jqXHR.responseText || "Kubernetes status check failed.");
    });
  }

  function loadNamespaces() {
    $.ajax({
      url: "/kubernetes/" + encodeURIComponent(provider) + "/namespaces",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      namespaceSelect.empty();
      if (result.length == 0) {
        setUnavailable("No namespaces found.");
        return;
      }
      $.each(result, function (index, namespace) {
        namespaceSelect.append($("<option/>").val(namespace.name).text(namespace.name));
      });
      if (namespaceSelect.find("option[value='default']").length) {
        namespaceSelect.val("default");
      }
      $('#loadKubernetesOverview, #loadKubernetesEvents').prop('disabled', false);
      pods.html("<div class='empty-state'>Select a namespace and click Load Namespace.</div>");
      deployments.html("<div class='empty-state'>Select a namespace and click Load Namespace.</div>");
      services.html("<div class='empty-state'>Select a namespace and click Load Namespace.</div>");
    }).fail(function (jqXHR) {
      setUnavailable(jqXHR.responseText || "Namespace load failed.");
    });
  }

  function loadOverview() {
    var namespace = namespaceSelect.val() || "default";
    if (!namespaceSelect.val()) {
      setIdleMessage("Click Refresh to load Kubernetes namespaces first.");
      return;
    }
    $.ajax({
      url: "/kubernetes/" + encodeURIComponent(provider) + "/overview",
      method: "GET",
      dataType: "json",
      data: {namespace: namespace}
    }).done(function (result) {
      renderPods(result.pods || []);
      renderSimpleList(deployments, result.deployments || [], ["name", "ready", "available", "createdOn"]);
      renderSimpleList(services, result.services || [], ["name", "type", "clusterIp", "ports", "createdOn"]);
      populateDeploymentOptions(result.deployments || []);
    }).fail(function (jqXHR) {
      setUnavailable(jqXHR.responseText || "Namespace overview failed.");
    });
  }

  function loadEvents() {
    var namespace = namespaceSelect.val() || "default";
    if (!namespaceSelect.val()) {
      setIdleMessage("Click Refresh to load Kubernetes namespaces first.");
      return;
    }
    output.text("Loading events...");
    $.ajax({
      url: "/kubernetes/" + encodeURIComponent(provider) + "/events",
      method: "GET",
      dataType: "text",
      data: {namespace: namespace}
    }).done(function (result) {
      output.text(result || "No events returned.");
    }).fail(function (jqXHR) {
      output.text(jqXHR.responseText || "Event request failed.");
    });
  }

  function renderPods(result) {
    if (result.length == 0) {
      pods.html("<div class='empty-state'>No pods found.</div>");
      return;
    }
    var html = "<div class='list-group'>";
    $.each(result, function (index, pod) {
      var container = String(pod.containers || "").split(",")[0] || "";
      var command = "kubectl logs -n " + (namespaceSelect.val() || "default") + " " + pod.name
          + " --tail=200";
      html = html + "<div class='list-group-item'>"
          + "<strong>" + escapeHtml(pod.name) + "</strong>"
          + "<span class='resource-meta'>Status: " + escapeHtml(pod.status)
          + " | Ready: " + escapeHtml(pod.ready)
          + " | Restarts: " + escapeHtml(pod.restarts) + "</span>"
          + "<span class='resource-meta'>Node: " + escapeHtml(pod.node)
          + " | CreatedOn: " + escapeHtml(pod.createdOn) + "</span>"
          + "<div class='action-row mt-2'>"
          + "<button type='button' class='btn btn-outline-primary btn-sm load-pod-logs'"
          + " data-pod='" + escapeAttribute(pod.name) + "'"
          + " data-container='" + escapeAttribute(container) + "'"
          + " data-command='" + escapeAttribute(command) + "'>Logs</button>"
          + "</div>"
          + "</div>";
    });
    pods.html(html + "</div>");
  }

  function renderSimpleList(target, result, fields) {
    if (result.length == 0) {
      target.html("<div class='empty-state'>None found.</div>");
      return;
    }
    var html = "<div class='list-group'>";
    $.each(result, function (index, item) {
      html = html + "<div class='list-group-item'><strong>" + escapeHtml(item.name) + "</strong>";
      $.each(fields, function (fieldIndex, field) {
        if (field != "name") {
          html = html + "<span class='resource-meta'>" + escapeHtml(field)
              + ": " + escapeHtml(item[field] || "Unavailable") + "</span>";
        }
      });
      html = html + "</div>";
    });
    target.html(html + "</div>");
  }

  function populateDeploymentOptions(result) {
    var deploymentSelect = $('#kubernetesServiceDeployment');
    deploymentSelect.empty();
    if (result.length == 0) {
      deploymentSelect.append($("<option/>").val("").text("No deployments found"));
      serviceControls.prop('disabled', true);
      return;
    }
    $.each(result, function (index, deployment) {
      deploymentSelect.append($("<option/>").val(deployment.name).text(deployment.name));
    });
    if (!$('#kubernetesServiceName').val()) {
      $('#kubernetesServiceName').val(result[0].name);
    }
    serviceControls.prop('disabled', false);
  }

  function setUnavailable(text) {
    message.html("<div class='alert alert-warning'>" + escapeHtml(text) + "</div>");
    $('#loadKubernetesOverview, #loadKubernetesEvents').prop('disabled', true);
    serviceControls.prop('disabled', true);
    $('#kubernetesServiceDeployment').empty();
    pods.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    deployments.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    services.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
  }

  function setIdle() {
    status.html("<span class='badge badge-secondary'>Not checked</span>"
        + "<span class='resource-meta'>Kubernetes commands run only after you click Refresh.</span>");
    namespaceSelect.empty();
    $('#loadKubernetesOverview, #loadKubernetesEvents').prop('disabled', true);
    serviceControls.prop('disabled', true);
    $('#kubernetesServiceDeployment').empty();
    pods.html("<div class='empty-state'>Click Refresh to check Kubernetes.</div>");
    deployments.html("<div class='empty-state'>Click Refresh to check Kubernetes.</div>");
    services.html("<div class='empty-state'>Click Refresh to check Kubernetes.</div>");
  }

  function setIdleMessage(text) {
    message.html("<div class='alert alert-info'>" + escapeHtml(text) + "</div>");
  }

  function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
  }

  function escapeAttribute(value) {
    return escapeHtml(value).replace(/`/g, "&#096;");
  }
});
