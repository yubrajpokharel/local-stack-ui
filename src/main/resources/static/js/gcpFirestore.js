$(document).ready(function () {
  var message = $('#firestoreMessage');
  var status = $('#firestoreStatus');
  var collections = $('#firestoreCollections');
  var documents = $('#firestoreDocuments');
  var controls = [
    $('#firestoreCollectionName'), $('#firestoreDocumentName'),
    $('#firestoreDocumentContent'), $('#createFirestoreDocument')
  ];
  var selectedCollection = null;

  refreshFirestore();

  $('#refreshFirestore').click(refreshFirestore);

  $('#startFirestore').click(function () {
    postAction("/gcp/firestore/start", "Starting Firestore...", function () {
      setTimeout(refreshFirestore, 3000);
    });
  });

  $('#stopFirestore').click(function () {
    postAction("/gcp/firestore/stop", "Stopping Firestore...", function (msg) {
      if (msg.status == "success") {
        setUnavailable("Firestore is not running.");
      }
      setTimeout(refreshFirestore, 1200);
    });
  });

  $('#createFirestoreDocument').click(function () {
    var collectionName = $.trim($('#firestoreCollectionName').val());
    var documentName = $.trim($('#firestoreDocumentName').val());
    var content = $('#firestoreDocumentContent').val();
    if (!collectionName || !documentName) {
      showMessage("Collection and document ID are required.", "warning");
      return;
    }
    try {
      JSON.parse(content || "{}");
    } catch (e) {
      showMessage("Document fields must be valid JSON.", "warning");
      return;
    }
    $.ajax({
      url: "/gcp/firestore/collections/" + encodeURIComponent(collectionName)
          + "/documents?documentName=" + encodeURIComponent(documentName),
      method: "POST",
      data: content || "{}",
      contentType: "text/plain; charset=utf-8",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        selectedCollection = collectionName;
        $('#firestoreDocumentName').val("");
        loadCollections();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create document failed: " + textStatus, "danger");
    });
  });

  collections.on('click', '.firestore-collection', function () {
    selectedCollection = $(this).data('collection');
    $('#firestoreCollectionName').val(selectedCollection);
    $('.firestore-collection').removeClass('active');
    $(this).addClass('active');
    loadDocuments(selectedCollection);
  });

  documents.on('click', '.delete-document', function () {
    var documentName = $(this).data('document');
    $.ajax({
      url: "/gcp/firestore/collections/" + encodeURIComponent(selectedCollection)
          + "/documents/" + encodeURIComponent(documentName),
      method: "DELETE",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      loadDocuments(selectedCollection);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Delete document failed: " + textStatus, "danger");
    });
  });

  function refreshFirestore() {
    $.ajax({
      url: "/gcp/firestore/status",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      if (msg.running) {
        status.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setControls(true);
        loadCollections();
      } else {
        status.html("<span class='badge badge-danger'>Unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setUnavailable("Firestore is not running.");
      }
    }).fail(function () {
      status.html("<span class='badge badge-danger'>Unavailable</span>");
      setUnavailable("Firestore status check failed.");
    });
  }

  function loadCollections() {
    $.ajax({
      url: "/gcp/firestore/collections",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.length == 0) {
        collections.html("<div class='empty-state'>No collections found.</div>");
        documents.html("<div class='empty-state'>Create a document to create a collection.</div>");
        return;
      }
      if (!selectedCollection || !containsCollection(result, selectedCollection)) {
        selectedCollection = result[0].name;
      }
      $('#firestoreCollectionName').val(selectedCollection);
      var list = "<div class='list-group'>";
      $.each(result, function (index, collection) {
        var active = collection.name == selectedCollection ? " active" : "";
        list = list + "<button type='button' class='list-group-item list-group-item-action firestore-collection"
            + active + "' data-collection='" + escapeAttribute(collection.name) + "'>"
            + "<strong>" + escapeHtml(collection.name) + "</strong>"
            + "<span class='resource-meta'>URL: " + escapeHtml(collection.address) + "</span>"
            + "</button>";
      });
      collections.html(list + "</div>");
      loadDocuments(selectedCollection);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Collection list failed: " + textStatus, "danger");
    });
  }

  function loadDocuments(collectionName) {
    if (!collectionName) {
      documents.html("<div class='empty-state'>Select a collection.</div>");
      return;
    }
    $.ajax({
      url: "/gcp/firestore/collections/" + encodeURIComponent(collectionName) + "/documents",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.length == 0) {
        documents.html("<div class='empty-state'>No documents found.</div>");
        return;
      }
      var list = "<div class='list-group'>";
      $.each(result, function (index, document) {
        list = list + "<div class='list-group-item'>"
            + "<div class='d-flex justify-content-between align-items-center'>"
            + "<strong>" + escapeHtml(document.name) + "</strong>"
            + "<span class='badge badge-danger delete-document' data-document='" + escapeAttribute(document.name)
            + "'>Delete</span></div>"
            + "<span class='resource-meta'>URL: " + escapeHtml(document.address) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(document.createdOn || "Unavailable") + "</span>"
            + "<pre class='code-cell mt-2 mb-0'>" + escapeHtml(document.fields || "{}") + "</pre>"
            + "</div>";
      });
      documents.html(list + "</div>");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Document list failed: " + textStatus, "danger");
    });
  }

  function containsCollection(collectionList, collectionName) {
    for (var i = 0; i < collectionList.length; i++) {
      if (collectionList[i].name == collectionName) {
        return true;
      }
    }
    return false;
  }

  function setUnavailable(text) {
    setControls(false);
    collections.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    documents.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
  }

  function setControls(isRunning) {
    $('#startFirestore').toggle(!isRunning);
    $('#stopFirestore').toggle(isRunning);
    $.each(controls, function (index, control) {
      control.prop('disabled', !isRunning);
    });
  }

  function postAction(url, text, callback) {
    showMessage(text, "info");
    $.ajax({
      url: url,
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      callback(msg);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
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
