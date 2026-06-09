$(document).ready(function () {
  var topicName = $('#kafkaTopicName').val();
  var sendStatus = $('#kafkaTopicSendStatus');
  var partitionVisual = $('#kafkaPartitionVisual');
  var partitionSummary = $('#kafkaPartitionSummary');

  loadTopic();
  loadPartitions();

  $('#refreshKafkaPartitions').click(function () {
    loadTopic();
    loadPartitions();
  });

  $('#sendKafkaTopicMessage').click(function () {
    var message = $('#kafkaTopicMessageBody').val();
    if (!message) {
      showSendStatus("Message is required.", "warning");
      return;
    }
    $('#sendKafkaTopicMessage').prop('disabled', true);
    $.ajax({
      url: "/kafka/topics/" + encodeURIComponent(topicName) + "/messages",
      method: "POST",
      data: message,
      contentType: "text/plain; charset=utf-8",
      dataType: "json"
    }).done(function (result) {
      showSendStatus(result.message || result.status,
          result.status == "success" ? "success" : "danger");
      if (result.status == "success") {
        $('#kafkaTopicMessageBody').val("");
        setTimeout(function () {
          loadTopic();
          loadPartitions();
        }, 500);
      }
    }).fail(function (jqXHR, textStatus) {
      showSendStatus("Send failed: " + textStatus, "danger");
    }).always(function () {
      $('#sendKafkaTopicMessage').prop('disabled', false);
    });
  });

  function loadTopic() {
    $.ajax({
      url: "/kafka/topics/" + encodeURIComponent(topicName) + "/details",
      method: "GET",
      dataType: "json"
    }).done(function (topic) {
      $('#kafkaTopicAddress').text(topic.address || "Unavailable");
      $('#kafkaTopicPartitionCount').text(topic.partitionCount || "Unavailable");
      $('#kafkaTopicMessageCount').text(topic.messageCount || "0");
    });
  }

  function loadPartitions() {
    $('#refreshKafkaPartitions').prop('disabled', true);
    partitionSummary.html("<span class='resource-meta'>Loading partition metrics...</span>");
    $.ajax({
      url: "/kafka/topics/" + encodeURIComponent(topicName) + "/partitions",
      method: "GET",
      dataType: "json"
    }).done(renderPartitions)
        .fail(function (jqXHR, textStatus) {
          partitionSummary.empty();
          partitionVisual.html("<div class='empty-state'>Partition load failed: "
              + escapeHtml(textStatus) + "</div>");
        }).always(function () {
          $('#refreshKafkaPartitions').prop('disabled', false);
        });
  }

  function renderPartitions(partitions) {
    if (partitions.length == 0) {
      partitionSummary.empty();
      partitionVisual.html("<div class='empty-state'>No partitions found.</div>");
      return;
    }
    var total = 0;
    var maxCount = 0;
    $.each(partitions, function (index, partition) {
      var count = parseInt(partition.messageCount || "0", 10);
      total = total + count;
      maxCount = Math.max(maxCount, count);
    });
    partitionSummary.html("<span class='badge badge-primary'>" + escapeHtml(partitions.length)
        + " partitions</span><span class='resource-meta'>Total messages: "
        + escapeHtml(total) + "</span>");
    var html = "";
    $.each(partitions, function (index, partition) {
      var count = parseInt(partition.messageCount || "0", 10);
      var width = maxCount == 0 ? 0 : Math.max(8, Math.round((count / maxCount) * 100));
      html = html + "<div class='partition-metric'>"
          + "<div class='partition-metric-header'>"
          + "<strong>Partition " + escapeHtml(partition.partition) + "</strong>"
          + "<span>" + escapeHtml(count) + " messages</span>"
          + "</div>"
          + "<div class='partition-track'><div class='partition-fill' style='width: "
          + width + "%'></div></div>"
          + "<div class='partition-meta'>"
          + "<span>Beginning: " + escapeHtml(partition.beginningOffset || "0") + "</span>"
          + "<span>End: " + escapeHtml(partition.endOffset || "0") + "</span>"
          + "<span>Leader: " + escapeHtml(partition.leader || "Unavailable") + "</span>"
          + "<span>Replicas: " + escapeHtml(partition.replicas || "Unavailable") + "</span>"
          + "<span>ISR: " + escapeHtml(partition.isr || "Unavailable") + "</span>"
          + "</div>"
          + "</div>";
    });
    partitionVisual.html(html);
  }

  function showSendStatus(text, type) {
    sendStatus.html("<div class='alert alert-" + type + "'>" + escapeHtml(text) + "</div>");
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
