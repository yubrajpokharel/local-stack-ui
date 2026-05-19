$(document).ready(function () {
  var message = $('#mockMessage');
  var serverList = $('#mockServerList');
  var mockCount = $('#mockCount');
  var nameInput = $('#mockName');
  var portInput = $('#mockPort');
  var responseInput = $('#mockResponse');

  loadServers();

  $('#refreshMocks').click(function () {
    loadServers();
  });

  $('#startMock').click(function () {
    startMock(false);
  });

  $('#restartMock').click(function () {
    startMock(true);
  });

  $('#stopAllMocks').click(function () {
    $.ajax({
      url: "/mock-http/stop-all",
      method: "DELETE",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message, "success");
      loadServers();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  $(document).on('click', "button.stop-mock", function () {
    stopMock($(this).data("port"));
  });

  $(document).on('click', "button.load-mock", function () {
    nameInput.val($(this).data("name"));
    portInput.val($(this).data("port"));
    responseInput.val($(this).data("response"));
  });

  function startMock(restartFirst) {
    var name = nameInput.val();
    var port = parseInt(portInput.val(), 10);
    var response = responseInput.val();

    if (name.length == 0) {
      showMessage("name cannot be empty", "danger");
      return;
    }

    if (!port || port < 1 || port > 65535) {
      showMessage("port must be between 1 and 65535", "danger");
      return;
    }

    if (restartFirst) {
      $.ajax({
        url: "/mock-http/stop/" + port,
        method: "DELETE",
        dataType: "json"
      }).always(function () {
        createMock(name, port, response);
      });
      return;
    }

    createMock(name, port, response);
  }

  function createMock(name, port, response) {
    $.ajax({
      url: "/mock-http/start",
      method: "POST",
      dataType: "json",
      data: {
        name: name,
        port: port,
        response: response
      }
    }).done(function (msg) {
      showMessage(msg.message, msg.status == "success" ? "success" : "danger");
      loadServers();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function stopMock(port) {
    $.ajax({
      url: "/mock-http/stop/" + port,
      method: "DELETE",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message, "success");
      loadServers();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function loadServers() {
    $.ajax({
      url: "/mock-http/servers",
      method: "GET",
      dataType: "json"
    }).done(function (servers) {
      mockCount.text(servers.length);
      if (servers.length == 0) {
        serverList.html("<div class='empty-state'>No mock servers running.</div>");
        return;
      }

      var table = "<div class='table-responsive'><table class='table table-hover table-sm mb-0'>"
          + "<thead><tr><th>Name</th><th>Address</th><th>CreatedOn</th><th>Response</th><th>Action</th></tr></thead><tbody>";

      $.each(servers, function (index, server) {
        var address = server.address || ("http://localhost:" + server.port);
        var createdOn = formatDisplayTime(server.createdOn);
        table = table + "<tr>"
            + "<td><strong>" + escapeHtml(server.name || "") + "</strong><br>"
            + "<span class='badge badge-success'>running</span></td>"
            + "<td><a class='badge badge-light' href='" + escapeHtml(address)
            + "' target='_blank'>" + escapeHtml(address) + "</a></td>"
            + "<td><span class='resource-meta'>" + escapeHtml(createdOn) + "</span></td>"
            + "<td><code class='mock-response d-inline-block'>"
            + escapeHtml(server.response || "") + "</code></td>"
            + "<td>"
            + "<button class='btn btn-secondary btn-sm load-mock mr-1' data-name='"
            + escapeHtml(server.name || "") + "' data-port='" + server.port + "' data-response='"
            + escapeHtml(server.response || "") + "'>Load</button>"
            + "<button class='btn btn-danger btn-sm stop-mock' data-port='" + server.port
            + "'>Stop</button>"
            + "</td>"
            + "</tr>";
      });

      table = table + "</tbody></table></div>";
      serverList.html(table);
    }).fail(function (jqXHR, textStatus) {
      mockCount.text("0");
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function showMessage(text, type) {
    message.html("<div class='alert alert-" + type + "'>" + escapeHtml(text) + "</div>");
  }

  function formatDisplayTime(value) {
    if (!value) {
      return "Unavailable";
    }
    var date = new Date(value);
    if (isNaN(date.getTime())) {
      return String(value).substring(0, 16);
    }
    return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate())
        + "T" + pad(date.getHours()) + ":" + pad(date.getMinutes());
  }

  function pad(value) {
    return String(value).padStart(2, "0");
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
