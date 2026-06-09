$(document).ready(function () {
  var subscriptionName = $('#subscriptionName').val();
  var messageRows = $('#pubsubMessageRows');
  var status = $('#pubsubMessageStatus');
  var sendStatus = $('#subscriptionSendStatus');

  loadSubscriptionDetails();

  $('#pollPubSubMessages').click(loadMessages);

  $('#sendSubscriptionMessage').click(function () {
    var topicName = $('#subscriptionTopic').val();
    var message = $('#subscriptionMessageBody').val();
    if (!topicName) {
      showSendStatus("Topic is unavailable.", "warning");
      return;
    }
    if (!message) {
      showSendStatus("Message is required.", "warning");
      return;
    }
    $('#sendSubscriptionMessage').prop('disabled', true);
    $.ajax({
      url: "/gcp/pubsub/topics/" + encodeURIComponent(topicName) + "/publish",
      method: "POST",
      data: message,
      contentType: "text/plain; charset=utf-8",
      dataType: "json"
    }).done(function (result) {
      showSendStatus(result.message || result.status,
          result.status == "success" ? "success" : "danger");
      if (result.status == "success") {
        $('#subscriptionMessageBody').val("");
      }
    }).fail(function (jqXHR, textStatus) {
      showSendStatus("Publish failed: " + textStatus, "danger");
    }).always(function () {
      $('#sendSubscriptionMessage').prop('disabled', !$('#subscriptionTopic').val());
    });
  });

  function loadSubscriptionDetails() {
    $.ajax({
      url: "/gcp/pubsub/subscriptions/" + encodeURIComponent(subscriptionName) + "/details",
      method: "GET",
      dataType: "json"
    }).done(function (details) {
      var topicName = details.topic || "";
      $('#subscriptionTopic').val(topicName);
      if (topicName) {
        var topicUrl = "/gcp/pubsub/topics/" + encodeURIComponent(topicName);
        $('#subscriptionTopicDisplay').html("<a href='" + topicUrl + "'>" + escapeHtml(topicName) + "</a>");
        $('#sendSubscriptionMessage').prop('disabled', false);
      } else {
        $('#subscriptionTopicDisplay').text("Unavailable");
        $('#sendSubscriptionMessage').prop('disabled', true);
      }
    }).fail(function () {
      $('#subscriptionTopicDisplay').text("Unavailable");
      $('#sendSubscriptionMessage').prop('disabled', true);
    });
  }

  function loadMessages() {
    $('#pollPubSubMessages').prop('disabled', true);
    status.html("<div class='alert alert-info'>Polling messages...</div>");
    $.ajax({
      url: "/gcp/pubsub/subscriptions/" + encodeURIComponent(subscriptionName) + "/messages",
      method: "GET",
      dataType: "json",
      data: {limit: 10}
    }).done(function (result) {
      renderMessages(result);
      status.empty();
    }).fail(function (jqXHR, textStatus) {
      messageRows.html("<tr><td colspan='6' class='text-muted'>No messages loaded.</td></tr>");
      status.html("<div class='alert alert-danger'>Message load failed: " + escapeHtml(textStatus) + "</div>");
    }).always(function () {
      $('#pollPubSubMessages').prop('disabled', false);
    });
  }

  function renderMessages(result) {
    if (result.length == 0) {
      messageRows.html("<tr><td colspan='6' class='text-muted'>No messages found.</td></tr>");
      return;
    }
    var rows = "";
    $.each(result, function (index, item) {
      rows = rows + "<tr>"
          + "<th scope='row'>" + escapeHtml(item.index || index + 1) + "</th>"
          + "<td class='code-cell'><b>" + escapeHtml(item.messageId || "Unavailable") + "</b></td>"
          + "<td class='code-cell'><pre class='pubsub-table-message'>" + escapeHtml(item.body || "") + "</pre></td>"
          + "<td class='code-cell'>" + escapeHtml(item.publishTime || "Unavailable") + "</td>"
          + "<td class='code-cell'>" + escapeHtml(item.attributes || "{}") + "</td>"
          + "<td class='code-cell'>" + escapeHtml(item.ackId || "Unavailable") + "</td>"
          + "</tr>";
    });
    messageRows.html(rows);
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
