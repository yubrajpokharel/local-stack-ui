$(document).ready(function () {
  var topicName = $('#topicName').val();
  var subscriptionRows = $('#topicSubscriptionRows');
  var sendStatus = $('#topicSendStatus');

  loadSubscriptions();

  $('#refreshTopicSubscriptions').click(loadSubscriptions);

  $('#sendTopicMessage').click(function () {
    var message = $('#topicMessageBody').val();
    if (!message) {
      showSendStatus("Message is required.", "warning");
      return;
    }
    $('#sendTopicMessage').prop('disabled', true);
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
        $('#topicMessageBody').val("");
      }
    }).fail(function (jqXHR, textStatus) {
      showSendStatus("Publish failed: " + textStatus, "danger");
    }).always(function () {
      $('#sendTopicMessage').prop('disabled', false);
    });
  });

  function loadSubscriptions() {
    $('#refreshTopicSubscriptions').prop('disabled', true);
    $.ajax({
      url: "/gcp/pubsub/topics/" + encodeURIComponent(topicName) + "/subscriptions",
      method: "GET",
      dataType: "json"
    }).done(renderSubscriptions)
        .fail(function (jqXHR, textStatus) {
          subscriptionRows.html("<tr><td colspan='4' class='text-muted'>Subscription load failed: "
              + escapeHtml(textStatus) + "</td></tr>");
        }).always(function () {
          $('#refreshTopicSubscriptions').prop('disabled', false);
        });
  }

  function renderSubscriptions(result) {
    if (result.length == 0) {
      subscriptionRows.html("<tr><td colspan='4' class='text-muted'>No subscriptions found.</td></tr>");
      return;
    }
    var rows = "";
    $.each(result, function (index, subscription) {
      var subscriptionUrl = "/gcp/pubsub/subscriptions/" + encodeURIComponent(subscription.name);
      rows = rows + "<tr>"
          + "<th scope='row'>" + escapeHtml(index + 1) + "</th>"
          + "<td class='code-cell'><a href='" + subscriptionUrl + "'><b>"
          + escapeHtml(subscription.name) + "</b></a></td>"
          + "<td class='code-cell'>" + escapeHtml(subscription.address || "Unavailable") + "</td>"
          + "<td class='code-cell'>" + escapeHtml(subscription.createdOn || "Unavailable") + "</td>"
          + "</tr>";
    });
    subscriptionRows.html(rows);
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
