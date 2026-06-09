$(document).ready(function () {
  var message = $('#mongoMessage');
  var status = $('#mongoStatus');
  var databases = $('#mongoDatabases');
  var collections = $('#mongoCollections');
  var startButton = $('#startMongo');
  var stopButton = $('#stopMongo');
  var createButton = $('#createMongoDatabase');
  var databaseNameInput = $('#mongoDatabaseName');
  var collectionNameInput = $('#mongoCollectionName');
  var collectionDatabaseSelect = $('#mongoCollectionDatabase');
  var newCollectionNameInput = $('#newMongoCollectionName');
  var createCollectionButton = $('#createMongoCollection');
  var selectedDatabase = null;

  refreshMongo();

  $('#refreshMongo').click(function () {
    refreshMongo();
  });

  startButton.click(function () {
    showMessage("Starting MongoDB...", "info");
    $.ajax({
      url: "/mongodb/start",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      setTimeout(refreshMongo, 1200);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  stopButton.click(function () {
    showMessage("Stopping MongoDB...", "info");
    $.ajax({
      url: "/mongodb/stop",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      selectedDatabase = null;
      setTimeout(refreshMongo, 1200);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  createButton.click(function () {
    var databaseName = $.trim(databaseNameInput.val());
    var collectionName = $.trim(collectionNameInput.val()) || "default_collection";
    if (!databaseName) {
      showMessage("Database name is required.", "warning");
      databaseNameInput.focus();
      return;
    }
    $.ajax({
      url: "/mongodb/databases",
      method: "POST",
      dataType: "json",
      data: {
        databaseName: databaseName,
        collectionName: collectionName
      }
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        selectedDatabase = databaseName;
        databaseNameInput.val("");
        collectionNameInput.val("");
        loadDatabases();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create database failed: " + textStatus, "danger");
    });
  });

  createCollectionButton.click(function () {
    var databaseName = collectionDatabaseSelect.val();
    var collectionName = $.trim(newCollectionNameInput.val());
    if (!databaseName) {
      showMessage("Select a database first.", "warning");
      collectionDatabaseSelect.focus();
      return;
    }
    if (!collectionName) {
      showMessage("Collection name is required.", "warning");
      newCollectionNameInput.focus();
      return;
    }
    $.ajax({
      url: "/mongodb/databases/" + encodeURIComponent(databaseName) + "/collections",
      method: "POST",
      dataType: "json",
      data: {
        collectionName: collectionName
      }
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        selectedDatabase = databaseName;
        newCollectionNameInput.val("");
        loadDatabases();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create collection failed: " + textStatus, "danger");
    });
  });

  databases.on('click', '.mongo-database', function () {
    selectedDatabase = $(this).data('database');
    collectionDatabaseSelect.val(selectedDatabase);
    $('.mongo-database').removeClass('active');
    $(this).addClass('active');
    loadCollections(selectedDatabase);
  });

  function refreshMongo() {
    $.ajax({
      url: "/mongodb/status",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      if (msg.running) {
        status.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setMongoControls(true);
        loadDatabases();
      } else {
        status.html("<span class='badge badge-danger'>Unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setMongoControls(false);
        populateDatabaseSelect([]);
        databases.html("<div class='empty-state'>MongoDB is not running.</div>");
        collections.html("<div class='empty-state'>Start MongoDB to view collections.</div>");
      }
    }).fail(function (jqXHR, textStatus) {
      status.html("<span class='badge badge-danger'>Unavailable</span>");
      setMongoControls(false);
      populateDatabaseSelect([]);
      databases.html("<div class='empty-state'>MongoDB status check failed.</div>");
      collections.html("<div class='empty-state'>MongoDB status check failed.</div>");
    });
  }

  function loadDatabases() {
    $.ajax({
      url: "/mongodb/databases",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.length == 0) {
        databases.html("<div class='empty-state'>No databases found.</div>");
        populateDatabaseSelect([]);
        collections.html("<div class='empty-state'>Create a database to view collections.</div>");
        return;
      }
      if (!selectedDatabase || $.inArray(selectedDatabase, result) == -1) {
        selectedDatabase = result[0];
      }
      populateDatabaseSelect(result);
      var list = "<div class='list-group'>";
      $.each(result, function (index, database) {
        var activeClass = database == selectedDatabase ? " active" : "";
        list = list + "<button type='button' class='list-group-item list-group-item-action mongo-database"
            + activeClass + "' data-database='" + escapeAttribute(database) + "'>"
            + "<strong>" + escapeHtml(database) + "</strong>"
            + "<span class='resource-meta'>Click to view collections</span>"
            + "</button>";
      });
      list = list + "</div>";
      databases.html(list);
      loadCollections(selectedDatabase);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Database list failed: " + textStatus, "danger");
    });
  }

  function loadCollections(databaseName) {
    if (!databaseName) {
      collections.html("<div class='empty-state'>Select a database.</div>");
      return;
    }
    collections.html("<div class='empty-state'>Loading collections...</div>");
    $.ajax({
      url: "/mongodb/databases/" + encodeURIComponent(databaseName) + "/collections",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.length == 0) {
        collections.html("<div class='empty-state'>No collections in " + escapeHtml(databaseName) + ".</div>");
        return;
      }
      var list = "<div class='mb-2'><strong>" + escapeHtml(databaseName) + "</strong></div>"
          + "<ul class='list-group'>";
      $.each(result, function (index, collection) {
        list = list + "<li class='list-group-item'><strong>"
            + escapeHtml(collection) + "</strong></li>";
      });
      list = list + "</ul>";
      collections.html(list);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Collection list failed: " + textStatus, "danger");
      collections.html("<div class='empty-state'>Could not load collections.</div>");
    });
  }

  function setMongoControls(isRunning) {
    startButton.toggle(!isRunning);
    stopButton.toggle(isRunning);
    createButton.prop('disabled', !isRunning);
    databaseNameInput.prop('disabled', !isRunning);
    collectionNameInput.prop('disabled', !isRunning);
    newCollectionNameInput.prop('disabled', !isRunning);
    setCollectionControls(isRunning && collectionDatabaseSelect.find('option[value!=""]').length > 0);
  }

  function populateDatabaseSelect(databaseList) {
    var options = "";
    if (databaseList.length == 0) {
      options = "<option value=''>No databases found</option>";
    } else {
      $.each(databaseList, function (index, database) {
        var selected = database == selectedDatabase ? " selected" : "";
        options = options + "<option value='" + escapeAttribute(database) + "'" + selected + ">"
            + escapeHtml(database) + "</option>";
      });
    }
    collectionDatabaseSelect.html(options);
    setCollectionControls(databaseList.length > 0 && stopButton.is(':visible'));
  }

  function setCollectionControls(enabled) {
    collectionDatabaseSelect.prop('disabled', !enabled);
    newCollectionNameInput.prop('disabled', !enabled);
    createCollectionButton.prop('disabled', !enabled);
  }

  function showMessage(text, type) {
    message.html("<div class='alert alert-" + type + "'>" + escapeHtml(text) + "</div>");
  }

  function escapeAttribute(value) {
    return escapeHtml(value);
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
