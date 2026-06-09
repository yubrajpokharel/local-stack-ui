$(document).ready(function () {
  var message = $('#storageMessage');
  var status = $('#storageStatus');
  var buckets = $('#storageBuckets');
  var objects = $('#storageObjects');
  var objectBucket = $('#storageObjectBucket');
  var fileBucket = $('#storageFileBucket');
  var controls = [
    $('#storageBucketName'), $('#createStorageBucket'), objectBucket,
    $('#storageObjectName'), $('#storageObjectContent'), $('#uploadStorageObject'),
    fileBucket, $('#storageFileObjectName'), $('#storageFile'), $('#uploadStorageFile')
  ];

  refreshStorage();

  $('#refreshStorage').click(refreshStorage);

  $('#startStorage').click(function () {
    postAction("/gcp/storage/start", "Starting Cloud Storage...", function () {
      setTimeout(refreshStorage, 1800);
    });
  });

  $('#stopStorage').click(function () {
    postAction("/gcp/storage/stop", "Stopping Cloud Storage...", function (msg) {
      if (msg.status == "success") {
        setUnavailable("Cloud Storage is not running.");
      }
      setTimeout(refreshStorage, 1200);
    });
  });

  $('#createStorageBucket').click(function () {
    var bucketName = $.trim($('#storageBucketName').val());
    if (!bucketName) {
      showMessage("Bucket name is required.", "warning");
      return;
    }
    $.ajax({
      url: "/gcp/storage/buckets",
      method: "POST",
      dataType: "json",
      data: {bucketName: bucketName}
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        $('#storageBucketName').val("");
        loadBuckets();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create bucket failed: " + textStatus, "danger");
    });
  });

  buckets.on('click', '.storage-bucket', function () {
    var bucketName = $(this).data('bucket');
    objectBucket.val(bucketName);
    fileBucket.val(bucketName);
    loadObjects(bucketName);
  });

  buckets.on('click', '.delete-bucket', function () {
    var bucketName = $(this).data('bucket');
    $.ajax({
      url: "/gcp/storage/buckets/" + encodeURIComponent(bucketName),
      method: "DELETE",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      loadBuckets();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Delete bucket failed: " + textStatus, "danger");
    });
  });

  objectBucket.change(function () {
    fileBucket.val($(this).val());
    loadObjects($(this).val());
  });

  fileBucket.change(function () {
    objectBucket.val($(this).val());
    loadObjects($(this).val());
  });

  $('#uploadStorageObject').click(function () {
    var bucketName = objectBucket.val();
    var objectName = $.trim($('#storageObjectName').val());
    if (!bucketName || !objectName) {
      showMessage("Bucket and object name are required.", "warning");
      return;
    }
    $.ajax({
      url: "/gcp/storage/buckets/" + encodeURIComponent(bucketName)
          + "/objects?objectName=" + encodeURIComponent(objectName),
      method: "POST",
      data: $('#storageObjectContent').val(),
      contentType: "text/plain; charset=utf-8",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        $('#storageObjectName').val("");
        $('#storageObjectContent').val("");
        loadObjects(bucketName);
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Upload object failed: " + textStatus, "danger");
    });
  });

  $('#uploadStorageFile').click(function () {
    var bucketName = fileBucket.val();
    var fileInput = $('#storageFile')[0];
    if (!bucketName || !fileInput.files || fileInput.files.length == 0) {
      showMessage("Bucket and file are required.", "warning");
      return;
    }
    var formData = new FormData();
    formData.append("file", fileInput.files[0]);
    var objectName = $.trim($('#storageFileObjectName').val());
    if (objectName) {
      formData.append("objectName", objectName);
    }
    $.ajax({
      url: "/gcp/storage/buckets/" + encodeURIComponent(bucketName) + "/objects/file",
      method: "POST",
      data: formData,
      processData: false,
      contentType: false,
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        $('#storageFileObjectName').val("");
        $('#storageFile').val("");
        objectBucket.val(bucketName);
        loadObjects(bucketName);
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Upload file failed: " + textStatus, "danger");
    });
  });

  objects.on('click', '.delete-object', function () {
    var bucketName = objectBucket.val();
    var objectName = $(this).data('object');
    $.ajax({
      url: "/gcp/storage/buckets/" + encodeURIComponent(bucketName)
          + "/objects/" + encodeURIComponent(objectName),
      method: "DELETE",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      loadObjects(bucketName);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Delete object failed: " + textStatus, "danger");
    });
  });

  function refreshStorage() {
    $.ajax({
      url: "/gcp/storage/status",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      if (msg.running) {
        status.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setControls(true);
        loadBuckets();
      } else {
        status.html("<span class='badge badge-danger'>Unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setUnavailable("Cloud Storage is not running.");
      }
    }).fail(function () {
      status.html("<span class='badge badge-danger'>Unavailable</span>");
      setUnavailable("Cloud Storage status check failed.");
    });
  }

  function loadBuckets() {
    $.ajax({
      url: "/gcp/storage/buckets",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      populateBucketSelect(result);
      if (result.length == 0) {
        buckets.html("<div class='empty-state'>No buckets found.</div>");
        objects.html("<div class='empty-state'>Create a bucket to view objects.</div>");
        return;
      }
      var list = "<div class='list-group'>";
      $.each(result, function (index, bucket) {
        list = list + "<div class='list-group-item'>"
            + "<div class='d-flex justify-content-between align-items-center'>"
            + "<button type='button' class='btn btn-link p-0 storage-bucket' data-bucket='"
            + escapeAttribute(bucket.name) + "'><strong>" + escapeHtml(bucket.name) + "</strong></button>"
            + "<span class='badge badge-danger delete-bucket' data-bucket='" + escapeAttribute(bucket.name)
            + "'>Delete</span></div>"
            + "<span class='resource-meta'>URL: " + escapeHtml(bucket.address) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(bucket.createdOn || "Unavailable") + "</span>"
            + "</div>";
      });
      buckets.html(list + "</div>");
      loadObjects(objectBucket.val());
    }).fail(function (jqXHR, textStatus) {
      showMessage("Bucket list failed: " + textStatus, "danger");
    });
  }

  function loadObjects(bucketName) {
    if (!bucketName) {
      objects.html("<div class='empty-state'>Select a bucket.</div>");
      return;
    }
    $.ajax({
      url: "/gcp/storage/buckets/" + encodeURIComponent(bucketName) + "/objects",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.length == 0) {
        objects.html("<div class='empty-state'>No objects found.</div>");
        return;
      }
      var list = "<div class='list-group'>";
      $.each(result, function (index, object) {
        var downloadUrl = "/gcp/storage/buckets/" + encodeURIComponent(bucketName)
            + "/objects/download?objectName=" + encodeURIComponent(object.name);
        list = list + "<div class='list-group-item'>"
            + "<div class='d-flex justify-content-between align-items-center'>"
            + "<strong>" + escapeHtml(object.name) + "</strong>"
            + "<span><a href='" + downloadUrl + "' class='btn btn-secondary btn-sm mr-1'>Download</a>"
            + "<span class='badge badge-danger delete-object' data-object='" + escapeAttribute(object.name)
            + "'>Delete</span></span></div>"
            + "<span class='resource-meta'>URL: " + escapeHtml(object.address) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(object.createdOn || "Unavailable") + "</span>"
            + "</div>";
      });
      objects.html(list + "</div>");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Object list failed: " + textStatus, "danger");
    });
  }

  function populateBucketSelect(bucketList) {
    var selected = objectBucket.val();
    var selectedFileBucket = fileBucket.val();
    objectBucket.empty();
    fileBucket.empty();
    $.each(bucketList, function (index, bucket) {
      objectBucket.append($("<option/>").val(bucket.name).text(bucket.name));
      fileBucket.append($("<option/>").val(bucket.name).text(bucket.name));
    });
    if (selected) {
      objectBucket.val(selected);
    }
    if (selectedFileBucket) {
      fileBucket.val(selectedFileBucket);
    } else {
      fileBucket.val(objectBucket.val());
    }
  }

  function setUnavailable(text) {
    setControls(false);
    buckets.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    objects.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    objectBucket.empty();
    fileBucket.empty();
  }

  function setControls(isRunning) {
    $('#startStorage').toggle(!isRunning);
    $('#stopStorage').toggle(isRunning);
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
