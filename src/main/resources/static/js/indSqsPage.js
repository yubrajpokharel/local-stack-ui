$(document).ready(function () {
  if ($('#queueType').val() == "FIFO") {
    $('#fifoFields').show();
  }

  $('#sendSqsMessage').click(function () {
    var queueName = $('#queueName').val();
    var queueType = $('#queueType').val();
    var message = $('#sqsMessageBox').val();
    var url = "/sqs/sendMessage/" + encodeURIComponent(queueName);

    if (message.length == 0) {
      alert("message cannot be empty");
      return;
    }

    if (queueType == "FIFO") {
      var groupId = $('#messageGroupId').val();
      if (groupId.length == 0) {
        alert("message group ID cannot be empty for FIFO queues");
        return;
      }
      url = url + "?messageGroupId=" + encodeURIComponent(groupId);
      var deduplicationId = $('#messageDeduplicationId').val();
      if (deduplicationId.length != 0) {
        url = url + "&messageDeduplicationId=" + encodeURIComponent(deduplicationId);
      }
    }

    $.ajax({
      url: url,
      method: "POST",
      contentType: "text/plain; charset=utf-8",
      data: message,
    }).done(function (messageId) {
      $('#sqsMessageId').text(messageId);
      $('#sqsSendResult').show();
      $('#sqsMessageBox').val("");
      setTimeout(function () {
        location.reload();
      }, 750);
    }).fail(function (jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
    });
  });
});
