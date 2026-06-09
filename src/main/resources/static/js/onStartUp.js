$(document).ready(function () {
  var message = $('#awsMessage');
  var status = $('#awsStatus');
  var startButton = $('#startAws');
  var stopButton = $('#stopAws');
  var topicName = $('#topicName');
  var queueName = $('#queueName');
  var queueType = $('#queueType');
  var createTopicButton = $('#createTopic');
  var createQueueButton = $('#createQueue');
  var enrollButton = $('#enroll');
  var snsList = $('#snsList');
  var sqsList = $('#sqsList');
  var snsOptions = $('#topicOptions');
  var sqsOptions = $('#queueOptions');

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
        loadAwsResources();
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

  function loadAwsResources() {
    apiCall("/sns-topics", snsList, "topic");
    apiCall("/sqs/details", sqsList, "queue");
    populateSelectOptions("/sns-topics", snsOptions);
    populateSelectOptions("/sqs", sqsOptions);
  }

  function setAwsUnavailable(text) {
    setAwsControls(false);
    snsList.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    sqsList.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    snsOptions.empty();
    sqsOptions.empty();
  }

  function setAwsControls(isRunning) {
    startButton.toggle(!isRunning);
    stopButton.toggle(isRunning);
    topicName.prop('disabled', !isRunning);
    queueName.prop('disabled', !isRunning);
    queueType.prop('disabled', !isRunning);
    createTopicButton.prop('disabled', !isRunning);
    createQueueButton.prop('disabled', !isRunning);
    snsOptions.prop('disabled', !isRunning);
    sqsOptions.prop('disabled', !isRunning);
    enrollButton.prop('disabled', !isRunning);
  }

  function apiCall(url, snsList, queueOrTopic) {
    $.ajax({
      url: url,
      method: "GET",
      dataType: "html"
    }).done(function (msg) {
      var result = $.parseJSON(msg);
      var listElement = "<ul class='list-group'>";
      if (result.length == 0) {
        listElement = listElement + "<li class='list-group-item text-muted'>None found</li>";
      }
      $.each(result, (function (index, element) {
        if (queueOrTopic == "queue") {
          listElement = listElement + "<li class='list-group-item'>"
              + functionGenerateLinkForQueue(element)
              + "<span data-type=\"queue\" data-name=\"" + element.name
              + "\" class=\"badge badge-danger delete\">Delete</span></li>";
        } else {
          listElement = listElement + "<li class='list-group-item'>"
              + functionGenerateLink(url, element)
              + "<span data-type=\"topic\" data-name=\"" + element
              + "\" class=\"badge badge-danger delete\">Delete</span></li>";
        }
      }));
      listElement = listElement + "</ul>";
      snsList.html(listElement);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function populateSelectOptions(url, selectId) {
    $.ajax({
      url: url,
      method: "GET",
      dataType: "html"
    }).done(function (msg) {
      var result = $.parseJSON(msg);
      selectId.empty();
      $.each(result, (function (index, element) {
        selectId.append($("<option/>").val(element).text(element));
      }));
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function functionGenerateLinkForQueue(queue) {
    var sqsName = queue.name;
    var type = queue.type || "Standard";
    return "<a class='resource-link' href=" + "sqs-message" + "/" + sqsName + " title=" + sqsName + ">"
        + sqsName + "</a>"
        + "<span class='badge badge-light ml-2'>" + type + "</span>";
  }

  function functionGenerateLink(prePender, path) {
    var sns = path.split(":");
    var snsName = sns[sns.length - 1];
    return "<a class='resource-link' href=" + prePender + "/" + path + " title=" + path + ">"
        + snsName + "</a>";
  }

  $('#createTopic').click(function () {
    var topicNameValue = topicName.val();
    if (topicNameValue.length != 0) {
      var endPointToCreateTopic = "/sns/createTopic/" + topicNameValue;
      create(endPointToCreateTopic);
      topicName.val("");
    } else {
      showMessage("Topic name cannot be empty.", "warning");
    }
  });

  $('#createQueue').click(function () {
    var queueNameValue = queueName.val();
    var queueTypeValue = queueType.val();
    if (queueNameValue.length != 0) {
      var endPointToCreateQueue = "/sqs/createQueue/" + queueNameValue + "?type=" + queueTypeValue;
      create(endPointToCreateQueue);
      queueName.val("");
    } else {
      showMessage("Queue name cannot be empty.", "warning");
    }
  });

  function create(url) {
    $.ajax({
      url: url,
      method: "POST",
      contentType: "application/json; charset=utf-8",
    }).done(function (msg) {
      console.log(msg);
      loadAwsResources();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  }

  function subscribe(url) {
    $.ajax({
      url: url,
      method: "POST",
      contentType: "application/json; charset=utf-8",
    }).done(function (msg) {
      showMessage("Successfully subscribed to topic.", "success");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  };

  $(document).on('click', "span.delete", function () {
    var name = $(this).data("name");
    var type = $(this).data("type");
    var url;
    if (type === "queue") {
      url = "/deleteQueue/" + name.split('/').pop();
    } else {
      url = "/deleteTopic/" + name;
    }
    $.ajax({
      url: url,
      method: "POST",
      contentType: "application/json; charset=utf-8",
    }).done(function (msg) {
      console.log("successfully deleted!");
      loadAwsResources();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  $('#enroll').click(function () {
    var queue = $('#queueOptions').val().split('/').pop();
    var topic = $('#topicOptions').val();
    if (queue.length == 0 || topic.length == 0) {
      showMessage("Queue and topic names cannot be empty.", "warning");
    } else {
      subscribe("/subscribe/" + queue + "/" + topic);
    }
  });

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
