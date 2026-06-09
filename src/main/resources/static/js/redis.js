$(document).ready(function () {
  var message = $('#redisMessage');
  var keyList = $('#redisKeyList');
  var status = $('#redisStatus');
  var keyInput = $('#redisKey');
  var valueInput = $('#redisValue');
  var startButton = $('#startRedis');
  var stopButton = $('#stopRedis');
  var saveButton = $('#saveRedis');
  var deleteButton = $('#deleteRedis');

  refreshRedis();

  $('#refreshRedis').click(function () {
    refreshRedis();
  });

  startButton.click(function () {
    showMessage("Starting Redis...", "info");
    $.ajax({
      url: "/redis/start",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      setTimeout(refreshRedis, 1200);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  stopButton.click(function () {
    showMessage("Stopping Redis...", "info");
    $.ajax({
      url: "/redis/stop",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      keyInput.val("");
      valueInput.val("");
      if (msg.status == "success") {
        status.html("<span class='badge badge-danger'>Redis unavailable</span>"
            + "<span class='resource-meta'>URL: redis://localhost:6379</span>");
        setRedisControls(false);
        keyList.html("<div class='empty-state'>Redis is not running.</div>");
      }
      setTimeout(refreshRedis, 1200);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  saveButton.click(function () {
    var key = keyInput.val();
    if (key.length == 0) {
      showMessage("Key cannot be empty.", "warning");
      return;
    }
    $.ajax({
      url: "/redis/value/" + encodeURIComponent(key),
      method: "POST",
      data: valueInput.val(),
      contentType: "text/plain; charset=utf-8",
    }).done(function () {
      refreshRedis();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  deleteButton.click(function () {
    var key = keyInput.val();
    if (key.length == 0) {
      showMessage("Key cannot be empty.", "warning");
      return;
    }
    $.ajax({
      url: "/redis/value/" + encodeURIComponent(key),
      method: "DELETE",
    }).done(function () {
      keyInput.val("");
      valueInput.val("");
      refreshRedis();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  $(document).on('click', "button.redis-key", function () {
    var key = $(this).data("key");
    loadValue(key);
  });

  function refreshRedis() {
    $.ajax({
      url: "/redis/status",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      if (msg.running) {
        status.html("<span class='badge badge-success'>Connected: " + escapeHtml(msg.response) + "</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setRedisControls(true);
        loadKeys();
      } else {
        status.html("<span class='badge badge-danger'>Redis unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setRedisControls(false);
        keyList.html("<div class='empty-state'>Redis is not running.</div>");
      }
    }).fail(function () {
      status.html("<span class='badge badge-danger'>Redis unavailable</span>");
      setRedisControls(false);
      keyList.html("<div class='empty-state'>Redis is not running.</div>");
    });
  }

  function loadKeys() {
    $.ajax({
      url: "/redis/key-details",
      method: "GET",
      dataType: "json"
    }).done(function (keys) {
      if (keys.length == 0) {
        keyList.html("<div class='empty-state'>No keys found.</div>");
        return;
      }
      var listElement = "<div class='list-group'>";
      $.each(keys, function (index, keyDetails) {
        var key = keyDetails.name;
        var createdOn = keyDetails.createdOn || "Unavailable";
        listElement = listElement
            + "<button type='button' class='list-group-item list-group-item-action redis-key' data-key='"
            + escapeHtml(key) + "'>"
            + "<strong>" + escapeHtml(key) + "</strong>"
            + "<span class='resource-meta'>URL: " + escapeHtml(keyDetails.address) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(createdOn) + "</span>"
            + "</button>";
      });
      listElement = listElement + "</div>";
      keyList.html(listElement);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function loadValue(key) {
    $.ajax({
      url: "/redis/value/" + encodeURIComponent(key),
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      keyInput.val(msg.key);
      valueInput.val(msg.value);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function setRedisControls(isRunning) {
    startButton.toggle(!isRunning);
    stopButton.toggle(isRunning);
    keyInput.prop('disabled', !isRunning);
    valueInput.prop('disabled', !isRunning);
    saveButton.prop('disabled', !isRunning);
    deleteButton.prop('disabled', !isRunning);
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
});
