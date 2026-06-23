$(document).ready(function () {
  var message = $('#gcpSecretsMessage');
  var status = $('#gcpSecretsStatus');
  var secretsList = $('#gcpSecretsList');
  var selectedSecretId = $('#selectedGcpSecretId');
  var selectedSecretValue = $('#selectedGcpSecretValue');
  var selectedControls = $('#selectedGcpSecretValue, #loadGcpSecretValue, #updateGcpSecretValue, #deleteGcpSecret');

  refreshSecrets();

  $('#refreshGcpSecrets').click(refreshSecrets);

  $('#createGcpSecret').click(function () {
    var secretId = $.trim($('#gcpSecretId').val());
    if (!secretId) {
      showMessage("Secret ID is required.", "warning");
      $('#gcpSecretId').focus();
      return;
    }
    $.ajax({
      url: "/gcp/secret-manager/secrets?" + $.param({secretId: secretId}),
      method: "POST",
      dataType: "json",
      contentType: "text/plain; charset=utf-8",
      data: $('#gcpSecretValue').val()
    }).done(function (result) {
      showMessage(result.message || result.status, result.status == "success" ? "success" : "danger");
      if (result.status == "success") {
        $('#gcpSecretId, #gcpSecretValue').val("");
        loadSecrets();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create secret failed: " + textStatus, "danger");
    });
  });

  secretsList.on('click', '.select-gcp-secret', function () {
    selectSecret($(this).data('secret-id'));
  });

  $('#loadGcpSecretValue').click(loadSelectedSecretValue);

  $('#updateGcpSecretValue').click(function () {
    if (!selectedSecretId.val()) {
      showMessage("Select a secret first.", "warning");
      return;
    }
    $.ajax({
      url: "/gcp/secret-manager/secrets/value?" + $.param({
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

  $('#deleteGcpSecret').click(function () {
    if (!selectedSecretId.val()) {
      showMessage("Select a secret first.", "warning");
      return;
    }
    $.ajax({
      url: "/gcp/secret-manager/secrets",
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

  function refreshSecrets() {
    $.ajax({
      url: "/gcp/secret-manager/status",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      status.html("<span class='badge badge-success'>Connected</span>"
          + "<span class='resource-meta'>URL: " + escapeHtml(result.uri) + "</span>"
          + "<span class='resource-meta'>Resource: " + escapeHtml(result.resourceName) + "</span>");
      loadSecrets();
    }).fail(function () {
      status.html("<span class='badge badge-danger'>Unavailable</span>");
      secretsList.html("<div class='empty-state'>GCP Secret Manager status check failed.</div>");
    });
  }

  function loadSecrets() {
    $.ajax({
      url: "/gcp/secret-manager/secrets",
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
            + "<span class='resource-meta'>Resource: " + escapeHtml(secret.resourceName) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(secret.createdOn) + "</span>"
            + "<span class='resource-meta'>LastChangedOn: " + escapeHtml(secret.lastChangedOn) + "</span>"
            + "<span class='resource-meta'>LatestVersion: " + escapeHtml(secret.latestVersion)
            + " | VersionCount: " + escapeHtml(secret.versionCount) + "</span>"
            + "<div class='action-row mt-2'>"
            + "<button type='button' class='btn btn-outline-primary btn-sm select-gcp-secret'"
            + " data-secret-id='" + escapeAttribute(secret.name) + "'>Select</button>"
            + "</div>"
            + "</div>";
      });
      secretsList.html(html + "</div>");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Secret list failed: " + textStatus, "danger");
      secretsList.html("<div class='empty-state'>Could not load secrets.</div>");
    });
  }

  function selectSecret(secretId) {
    selectedSecretId.val(secretId);
    $('#selectedGcpSecretName').text(secretId);
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
      url: "/gcp/secret-manager/secrets/value",
      method: "GET",
      dataType: "json",
      data: {
        secretId: selectedSecretId.val()
      }
    }).done(function (result) {
      selectedSecretValue.val(result.value || "");
      showMessage("Loaded secret " + result.name + " version " + result.version + ".", "success");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Load secret failed: " + textStatus, "danger");
    });
  }

  function clearSelectedSecret() {
    selectedSecretId.val("");
    selectedSecretValue.val("");
    $('#selectedGcpSecretName').text("None");
    selectedControls.prop('disabled', true);
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
