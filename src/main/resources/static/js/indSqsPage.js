$(document).ready(function () {
  $('#sendSqsMessage').click(function () {
    var queueName = $('#queueName').val();
    var message = $('#sqsMessageBox').val();

    if (message.length == 0) {
      alert("message cannot be empty");
      return;
    }

    $.ajax({
      url: "/sqs/sendMessage/" + encodeURIComponent(queueName),
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
