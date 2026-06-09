$(document).ready(function () {
  var message = $('#kafkaMessage');
  var status = $('#kafkaStatus');
  var topics = $('#kafkaTopics');
  var startButton = $('#startKafka');
  var stopButton = $('#stopKafka');
  var topicNameInput = $('#kafkaTopicName');
  var partitionsInput = $('#kafkaPartitions');
  var replicationFactorInput = $('#kafkaReplicationFactor');
  var createTopicButton = $('#createKafkaTopic');

  refreshKafka();

  $('#refreshKafka').click(function () {
    refreshKafka();
  });

  startButton.click(function () {
    showMessage("Starting Kafka...", "info");
    $.ajax({
      url: "/kafka/start",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      setTimeout(refreshKafka, 3500);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  stopButton.click(function () {
    showMessage("Stopping Kafka...", "info");
    $.ajax({
      url: "/kafka/stop",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        status.html("<span class='badge badge-danger'>Kafka unavailable</span>"
            + "<span class='resource-meta'>URL: kafka://localhost:9092</span>");
        setKafkaControls(false);
        topics.html("<div class='empty-state'>Kafka is not running.</div>");
      }
      setTimeout(refreshKafka, 1200);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  createTopicButton.click(function () {
    var topicName = $.trim(topicNameInput.val());
    if (!topicName) {
      showMessage("Topic name is required.", "warning");
      topicNameInput.focus();
      return;
    }
    $.ajax({
      url: "/kafka/topics",
      method: "POST",
      dataType: "json",
      data: {
        topicName: topicName,
        partitions: partitionsInput.val() || 1,
        replicationFactor: replicationFactorInput.val() || 1
      }
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        topicNameInput.val("");
        loadTopics();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create topic failed: " + textStatus, "danger");
    });
  });

  function refreshKafka() {
    $.ajax({
      url: "/kafka/status",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      if (msg.running) {
        status.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setKafkaControls(true);
        loadTopics();
      } else {
        status.html("<span class='badge badge-danger'>Kafka unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>");
        setKafkaControls(false);
        topics.html("<div class='empty-state'>Kafka is not running.</div>");
      }
    }).fail(function () {
      status.html("<span class='badge badge-danger'>Kafka unavailable</span>");
      setKafkaControls(false);
      topics.html("<div class='empty-state'>Kafka status check failed.</div>");
    });
  }

  function loadTopics() {
    $.ajax({
      url: "/kafka/topics",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      if (result.length == 0) {
        topics.html("<div class='empty-state'>No topics found.</div>");
        return;
      }
      var list = "<div class='list-group'>";
      $.each(result, function (index, topic) {
        var createdOn = topic.createdOn || "Unavailable";
        var topicUrl = "/kafka/topics/" + encodeURIComponent(topic.name);
        list = list + "<div class='list-group-item'>"
            + "<strong><a href='" + topicUrl + "'>" + escapeHtml(topic.name) + "</a></strong>"
            + "<span class='resource-meta'>URL: " + escapeHtml(topic.address) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(createdOn) + "</span>"
            + "<div class='action-row mt-2'>"
            + "<a class='btn btn-outline-primary btn-sm' href='" + topicUrl + "'>View Topic</a>"
            + "</div>"
            + "</div>";
      });
      list = list + "</div>";
      topics.html(list);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Topic list failed: " + textStatus, "danger");
      topics.html("<div class='empty-state'>Could not load topics.</div>");
    });
  }

  function setKafkaControls(isRunning) {
    startButton.toggle(!isRunning);
    stopButton.toggle(isRunning);
    topicNameInput.prop('disabled', !isRunning);
    partitionsInput.prop('disabled', !isRunning);
    replicationFactorInput.prop('disabled', !isRunning);
    createTopicButton.prop('disabled', !isRunning);
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
