$(document).ready(function () {
  var keyList = $('#redisKeyList');
  var status = $('#redisStatus');
  var keyInput = $('#redisKey');
  var valueInput = $('#redisValue');

  refreshRedis();

  $('#refreshRedis').click(function () {
    refreshRedis();
  });

  $('#saveRedis').click(function () {
    var key = keyInput.val();
    if (key.length == 0) {
      alert("key cannot be empty");
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
      alert("Request failed: " + textStatus);
    });
  });

  $('#deleteRedis').click(function () {
    var key = keyInput.val();
    if (key.length == 0) {
      alert("key cannot be empty");
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
      alert("Request failed: " + textStatus);
    });
  });

  $(document).on('click', "button.redis-key", function () {
    var key = $(this).data("key");
    loadValue(key);
  });

  function refreshRedis() {
    $.ajax({
      url: "/redis/health",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      status.html("<span class='badge badge-success'>Connected: " + msg.status + "</span>");
      loadKeys();
    }).fail(function () {
      status.html("<span class='badge badge-danger'>Redis unavailable</span>");
      keyList.html("");
    });
  }

  function loadKeys() {
    $.ajax({
      url: "/redis/keys",
      method: "GET",
      dataType: "json"
    }).done(function (keys) {
      if (keys.length == 0) {
        keyList.html("<div class='empty-state'>No keys found.</div>");
        return;
      }
      var listElement = "<div class='list-group'>";
      $.each(keys, function (index, key) {
        listElement = listElement
            + "<button type='button' class='list-group-item list-group-item-action redis-key' data-key='"
            + escapeHtml(key) + "'>" + escapeHtml(key) + "</button>";
      });
      listElement = listElement + "</div>";
      keyList.html(listElement);
    }).fail(function (jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
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
      alert("Request failed: " + textStatus);
    });
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
