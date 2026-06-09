$(document).ready(function () {
  var message = $('#pubsubMessage');
  var status = $('#pubsubStatus');
  var topics = $('#pubsubTopics');
  var subscriptions = $('#pubsubSubscriptions');
  var topicOptions = $('#pubsubTopicOptions');
  var publishTopic = $('#pubsubPublishTopic');
  var controls = [
    $('#pubsubTopicName'), $('#createPubSubTopic'), $('#pubsubSubscriptionName'),
    topicOptions, $('#createPubSubSubscription'), publishTopic,
    $('#pubsubMessageBody'), $('#publishPubSubMessage')
  ];

  refreshPubSub();

  $('#refreshPubSub').click(refreshPubSub);

  $('#startPubSub').click(function () {
    postAction("/gcp/pubsub/start", "Starting Pub/Sub...", function () {
      setTimeout(refreshPubSub, 3000);
    });
  });

  $('#stopPubSub').click(function () {
    postAction("/gcp/pubsub/stop", "Stopping Pub/Sub...", function (msg) {
      if (msg.status == "success") {
        setUnavailable("Pub/Sub is not running.");
      }
      setTimeout(refreshPubSub, 1200);
    });
  });

  $('#createPubSubTopic').click(function () {
    var topicName = $.trim($('#pubsubTopicName').val());
    if (!topicName) {
      showMessage("Topic name is required.", "warning");
      return;
    }
    $.ajax({
      url: "/gcp/pubsub/topics",
      method: "POST",
      dataType: "json",
      data: {topicName: topicName}
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        $('#pubsubTopicName').val("");
        loadResources();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create topic failed: " + textStatus, "danger");
    });
  });

  $('#createPubSubSubscription').click(function () {
    var subscriptionName = $.trim($('#pubsubSubscriptionName').val());
    var topicName = topicOptions.val();
    if (!subscriptionName || !topicName) {
      showMessage("Subscription name and topic are required.", "warning");
      return;
    }
    $.ajax({
      url: "/gcp/pubsub/subscriptions",
      method: "POST",
      dataType: "json",
      data: {
        subscriptionName: subscriptionName,
        topicName: topicName
      }
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        $('#pubsubSubscriptionName').val("");
        loadSubscriptions();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create subscription failed: " + textStatus, "danger");
    });
  });

  $('#publishPubSubMessage').click(function () {
    var topicName = publishTopic.val();
    var text = $('#pubsubMessageBody').val();
    if (!topicName || !text) {
      showMessage("Topic and message are required.", "warning");
      return;
    }
    $.ajax({
      url: "/gcp/pubsub/topics/" + encodeURIComponent(topicName) + "/publish",
      method: "POST",
      data: text,
      contentType: "text/plain; charset=utf-8",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        $('#pubsubMessageBody').val("");
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Publish failed: " + textStatus, "danger");
    });
  });

  function refreshPubSub() {
    $.ajax({
      url: "/gcp/pubsub/status",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      if (msg.running) {
        status.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setControls(true);
        loadResources();
      } else {
        status.html("<span class='badge badge-danger'>Unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setUnavailable("Pub/Sub is not running.");
      }
    }).fail(function () {
      status.html("<span class='badge badge-danger'>Unavailable</span>");
      setUnavailable("Pub/Sub status check failed.");
    });
  }

  function loadResources() {
    loadTopics();
    loadSubscriptions();
  }

  function loadTopics() {
    $.ajax({
      url: "/gcp/pubsub/topics",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      populateTopicSelects(result);
      if (result.length == 0) {
        topics.html("<div class='empty-state'>No topics found.</div>");
        return;
      }
      var list = "<div class='list-group'>";
      $.each(result, function (index, topic) {
        var topicUrl = "/gcp/pubsub/topics/" + encodeURIComponent(topic.name);
        list = list + "<div class='list-group-item'><strong><a href='" + topicUrl + "'>"
            + escapeHtml(topic.name) + "</a></strong>"
            + "<span class='resource-meta'>URL: " + escapeHtml(topic.address) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(topic.createdOn || "Unavailable") + "</span>"
            + "<div class='action-row mt-2'>"
            + "<a class='btn btn-outline-primary btn-sm' href='" + topicUrl + "'>View Topic</a>"
            + "</div>"
            + "</div>";
      });
      topics.html(list + "</div>");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Topic list failed: " + textStatus, "danger");
    });
  }

  function loadSubscriptions() {
    $.ajax({
      url: "/gcp/pubsub/subscriptions",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.length == 0) {
        subscriptions.html("<div class='empty-state'>No subscriptions found.</div>");
        return;
      }
      var list = "<div class='list-group'>";
      $.each(result, function (index, subscription) {
        var subscriptionUrl = "/gcp/pubsub/subscriptions/" + encodeURIComponent(subscription.name);
        list = list + "<div class='list-group-item'><strong><a href='" + subscriptionUrl + "'>"
            + escapeHtml(subscription.name) + "</a></strong>"
            + "<span class='resource-meta'>Topic: " + escapeHtml(subscription.topic || "Unavailable") + "</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(subscription.address) + "</span>"
            + "<div class='action-row mt-2'>"
            + "<a class='btn btn-outline-primary btn-sm' href='" + subscriptionUrl + "'>View Messages</a>"
            + "</div>"
            + "</div>";
      });
      subscriptions.html(list + "</div>");
    }).fail(function (jqXHR, textStatus) {
      showMessage("Subscription list failed: " + textStatus, "danger");
    });
  }

  function populateTopicSelects(topicList) {
    topicOptions.empty();
    publishTopic.empty();
    $.each(topicList, function (index, topic) {
      topicOptions.append($("<option/>").val(topic.name).text(topic.name));
      publishTopic.append($("<option/>").val(topic.name).text(topic.name));
    });
  }

  function setUnavailable(text) {
    setControls(false);
    topics.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    subscriptions.html("<div class='empty-state'>" + escapeHtml(text) + "</div>");
    topicOptions.empty();
    publishTopic.empty();
  }

  function setControls(isRunning) {
    $('#startPubSub').toggle(!isRunning);
    $('#stopPubSub').toggle(isRunning);
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

  function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
  }

});
