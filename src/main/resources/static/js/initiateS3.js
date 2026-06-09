$(document).ready(function () {
  var message = $('#awsMessage');
  var status = $('#awsStatus');
  var startButton = $('#startAws');
  var stopButton = $('#stopAws');
  var bucketNameInput = $('#bucketName');
  var createBucketButton = $('#createBucket');
  var bucketList = $('#bucketList');

  refreshAws();

  $('#refreshAws').click(function () {
    refreshAws();
  });

  startButton.click(function () {
    showMessage("Starting AWS services...", "info");
    $.ajax({
      url: "/localstack/start",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      setTimeout(refreshAws, 3000);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  stopButton.click(function () {
    showMessage("Stopping AWS services...", "info");
    $.ajax({
      url: "/localstack/stop",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        setAwsUnavailable("AWS services are not running.");
      }
      setTimeout(refreshAws, 1200);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  function refreshAws() {
    $.ajax({
      url: "/localstack/status",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      if (msg.running) {
        status.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setAwsControls(true);
        apiCall("/s3-buckets", bucketList);
      } else {
        status.html("<span class='badge badge-danger'>Unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setAwsUnavailable("AWS services are not running.");
      }
    }).fail(function () {
      status.html("<span class='badge badge-danger'>Unavailable</span>");
      setAwsUnavailable("AWS status check failed.");
    });
  }

  function setAwsUnavailable(text) {
    setAwsControls(false);
    bucketList.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
  }

  function setAwsControls(isRunning) {
    startButton.toggle(!isRunning);
    stopButton.toggle(isRunning);
    bucketNameInput.prop('disabled', !isRunning);
    createBucketButton.prop('disabled', !isRunning);
  }

  function apiCall(url, snsList) {
    $.ajax({
      url: url,
      method: "GET",
      dataType: "html"
    }).done(function (msg) {
      var result = $.parseJSON(msg);
      var listElement = "<ul class='list-group'>";
      if (result.length > 0) {
        $.each(result, (function (index, element) {

          listElement = listElement + "<li class='list-group-item'>"
              + functionGenerateLinkBucket(url, element)
              + "<span data-type=\"bucket\" data-name=\"" + element.name
              + "\" class=\"badge badge-danger delete\">Delete</span></li>";
        }));
      } else {
        listElement = listElement + "<li class='list-group-item text-muted'>No buckets</li>";
      }
      listElement = listElement + "</ul>";
      snsList.html(listElement);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function functionGenerateLinkBucket(prePender, bucket) {
    var localstackUrl = "http://s3.us-east-1.localhost.localstack.cloud:4566/" + bucket.name;
    var createdOn = formatDisplayTime(bucket.creationDate);
    return "<a class='resource-link' href=" + prePender + "/" + bucket.name + ">"
        + bucket.name + "</a>"
        + "<span class='resource-meta'>URL: " + localstackUrl + "</span>"
        + "<span class='resource-meta'>CreatedOn: " + createdOn + "</span>";
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

  $(document).on('click', "span.delete", function () {
    var name = $(this).data("name");
    var url;
      url = "/s3-buckets/delete/" + name;
    $.ajax({
      url: url,
      method: "POST",
      contentType: "application/json; charset=utf-8",
    }).done(function (msg) {
      console.log("successfully deleted!");
      apiCall("/s3-buckets", bucketList);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  $('#createBucket').click(function () {
    var bucketName = bucketNameInput.val();
    if (bucketName.length != 0) {
      var endPointToCreateQueue = "/s3-buckets/create/" + bucketName;
      create(endPointToCreateQueue);
      bucketNameInput.val("");
    } else {
      showMessage("Bucket name cannot be empty.", "warning");
    }
  });

  function create(url) {
    $.ajax({
      url: url,
      method: "POST",
      contentType: "application/json; charset=utf-8",
    }).done(function (msg) {
      console.log(msg);
      apiCall("/s3-buckets", bucketList);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
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
