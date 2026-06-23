$(document).ready(function () {
  var message = $('#secretsMessage');
  var awsStatus = $('#secretsAwsStatus');
  var secretsList = $('#secretsList');
  var startButton = $('#startSecretsAws');
  var stopButton = $('#stopSecretsAws');
  var createControls = $('#secretName, #secretDescription, #secretValue, #createSecret');
  var selectedSecretId = $('#selectedSecretId');
  var selectedSecretValue = $('#selectedSecretValue');
  var selectedControls = $('#selectedSecretValue, #loadSecretValue, #updateSecretValue, #deleteSecret');

  refreshAws();

  $('#refreshSecretsAws').click(refreshAws);

  startButton.click(function () {
    showMessage("Starting AWS LocalStack...", "info");
    $.ajax({
      url: "/localstack/start",
      method: "POST",
      dataType: "json"
    }).done(function (result) {
      showMessage(result.message || result.status, result.status == "success" ? "success" : "danger");
      setTimeout(refreshAws, 1800);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  stopButton.click(function () {
    showMessage("Stopping AWS LocalStack...", "info");
    $.ajax({
      url: "/localstack/stop",
      method: "POST",
      dataType: "json"
    }).done(function (result) {
      showMessage(result.message || result.status, result.status == "success" ? "success" : "danger");
      setAwsControls(false);
      secretsList.html("<div class='empty-state'>AWS LocalStack is not running.</div>");
      setTimeout(refreshAws, 1200);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  $('#createSecret').click(function () {
    var name = $.trim($('#secretName').val());
    if (!name) {
      showMessage("Secret name is required.", "warning");
      $('#secretName').focus();
      return;
    }
    $.ajax({
      url: "/secrets-manager/secrets?" + $.param({
        secretName: name,
        description: $('#secretDescription').val()
      }),
      method: "POST",
      dataType: "json",
      contentType: "text/plain; charset=utf-8",
      data: $('#secretValue').val()
    }).done(function (result) {
      showMessage(result.message || result.status, result.status == "success" ? "success" : "danger");
      if (result.status == "success") {
        $('#secretName, #secretDescription, #secretValue').val("");
        loadSecrets();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create secret failed: " + textStatus, "danger");
    });
  });

  secretsList.on('click', '.select-secret', function () {
    selectSecret($(this).data('secret-id'), $(this).data('secret-name'));
  });

  $('#loadSecretValue').click(loadSelectedSecretValue);

  $('#updateSecretValue').click(function () {
    if (!selectedSecretId.val()) {
      showMessage("Select a secret first.", "warning");
      return;
    }
    $.ajax({
      url: "/secrets-manager/secrets/value?" + $.param({
        secretId: selectedSecretId.val()
      }),
      method: "PUT",
      dataType: "json",
      contentType: "text/plain; charset=utf-8",
      data: selectedSecretValue.val()
    }).done(function (result) {
      showMessage(result.message || result.status, result.status == "success" ? "success" : "danger");
      if (result.status == "success") {
        loadSecrets();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Update secret failed: " + textStatus, "danger");
    });
  });

  $('#deleteSecret').click(function () {
    if (!selectedSecretId.val()) {
      showMessage("Select a secret first.", "warning");
      return;
    }
    $.ajax({
      url: "/secrets-manager/secrets",
      method: "DELETE",
      dataType: "json",
      data: {
        secretId: selectedSecretId.val()
      }
    }).done(function (result) {
      showMessage(result.message || result.status, result.status == "success" ? "success" : "danger");
      if (result.status == "success") {
        clearSelectedSecret();
        loadSecrets();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Delete secret failed: " + textStatus, "danger");
    });
  });

  function refreshAws() {
    $.ajax({
      url: "/localstack/status",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.running) {
        awsStatus.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(result.uri) + "</span>");
        setAwsControls(true);
        loadSecrets();
      } else {
        awsStatus.html("<span class='badge badge-danger'>AWS unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(result.uri) + "</span>");
        setAwsControls(false);
        secretsList.html("<div class='empty-state'>AWS LocalStack is not running.</div>");
      }
    }).fail(function () {
      awsStatus.html("<span class='badge badge-danger'>AWS unavailable</span>");
      setAwsControls(false);
      secretsList.html("<div class='empty-state'>AWS LocalStack status check failed.</div>");
    });
  }

  function loadSecrets() {
    $.ajax({
      url: "/secrets-manager/secrets",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.length == 0) {
        secretsList.html("<div class='empty-state'>No secrets found.</div>");
        return;
      }
      var html = "<div class='list-group'>";
      $.each(result, function (index, secret) {
        html = html + "<div class='list-group-item'>"
            + "<strong>" + escapeHtml(secret.name) + "</strong>"
            + "<span class='resource-meta'>ARN: " + escapeHtml(secret.arn) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(secret.createdOn) + "</span>"
            + "<span class='resource-meta'>LastChangedOn: " + escapeHtml(secret.lastChangedOn) + "</span>"
            + "<span class='resource-meta'>Description: " + escapeHtml(secret.description || "Unavailable") + "</span>"
            + "<div class='action-row mt-2'>"
            + "<button type='button' class='btn btn-outline-primary btn-sm select-secret'"
            + " data-secret-id='" + escapeAttribute(secret.arn) + "'"
            + " data-secret-name='" + escapeAttribute(secret.name) + "'>Select</button>"
            + "</div>"
            + "</div>";
      });
      secretsList.html(html + "</div>");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Secret list failed: " + textStatus, "danger");
      secretsList.html("<div class='empty-state'>Could not load secrets.</div>");
    });
  }

  function selectSecret(secretId, secretName) {
    selectedSecretId.val(secretId);
    $('#selectedSecretName').text(secretName);
    selectedSecretValue.val("");
    selectedControls.prop('disabled', false);
    loadSelectedSecretValue();
  }

  function loadSelectedSecretValue() {
    if (!selectedSecretId.val()) {
      showMessage("Select a secret first.", "warning");
      return;
    }
    $.ajax({
      url: "/secrets-manager/secrets/value",
      method: "GET",
      dataType: "json",
      data: {
        secretId: selectedSecretId.val()
      }
    }).done(function (result) {
      selectedSecretValue.val(result.value || "");
      showMessage("Loaded secret " + result.name + ".", "success");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Load secret failed: " + textStatus, "danger");
    });
  }

  function clearSelectedSecret() {
    selectedSecretId.val("");
    selectedSecretValue.val("");
    $('#selectedSecretName').text("None");
    selectedControls.prop('disabled', true);
  }

  function setAwsControls(isRunning) {
    startButton.toggle(!isRunning);
    stopButton.toggle(isRunning);
    createControls.prop('disabled', !isRunning);
    if (!isRunning) {
      clearSelectedSecret();
    }
  }

  function showMessage(text, type) {
    message.html("<div class='alert alert-" + type + "'>" + escapeHtml(text) + "</div>");
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
