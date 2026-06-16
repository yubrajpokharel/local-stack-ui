$(document).ready(function () {
  var message = $('#rabbitMessage');
  var status = $('#rabbitStatus');
  var queues = $('#rabbitQueues');
  var startButton = $('#startRabbit');
  var stopButton = $('#stopRabbit');
  var queueNameInput = $('#rabbitQueueName');
  var queueTypeInput = $('#rabbitQueueType');
  var durableInput = $('#rabbitDurable');
  var autoDeleteInput = $('#rabbitAutoDelete');
  var createQueueButton = $('#createRabbitQueue');
  var publishQueueInput = $('#rabbitPublishQueue');
  var payloadInput = $('#rabbitPayload');
  var publishButton = $('#publishRabbitMessage');
  var selectedQueueBadge = $('#rabbitSelectedQueue');
  var peekCountInput = $('#rabbitPeekCount');
  var peekButton = $('#peekRabbitMessages');
  var messages = $('#rabbitMessages');
  var selectedQueue = "";

  refreshRabbit();

  $('#refreshRabbit').click(refreshRabbit);

  startButton.click(function () {
    showMessage("Starting RabbitMQ...", "info");
    $.ajax({
      url: "/rabbitmq/start",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      setTimeout(refreshRabbit, 3500);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  stopButton.click(function () {
    showMessage("Stopping RabbitMQ...", "info");
    $.ajax({
      url: "/rabbitmq/stop",
      method: "POST",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        status.html("<span class='badge badge-danger'>RabbitMQ unavailable</span>"
            + "<span class='resource-meta'>URL: amqp://localhost:5672/%2F</span>");
        setRabbitControls(false);
        queues.html("<div class='empty-state'>RabbitMQ is not running.</div>");
        messages.html("<div class='empty-state'>RabbitMQ is not running.</div>");
      }
      setTimeout(refreshRabbit, 1200);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Request failed: " + textStatus, "danger");
    });
  });

  createQueueButton.click(function () {
    var queueName = $.trim(queueNameInput.val());
    if (!queueName) {
      showMessage("Queue name is required.", "warning");
      queueNameInput.focus();
      return;
    }
    $.ajax({
      url: "/rabbitmq/queues",
      method: "POST",
      dataType: "json",
      data: {
        queueName: queueName,
        durable: durableInput.is(':checked'),
        autoDelete: autoDeleteInput.is(':checked'),
        queueType: queueTypeInput.val()
      }
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        queueNameInput.val("");
        loadQueues();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Create queue failed: " + textStatus, "danger");
    });
  });

  publishButton.click(function () {
    var queueName = publishQueueInput.val();
    if (!queueName) {
      showMessage("Queue is required.", "warning");
      return;
    }
    $.ajax({
      url: "/rabbitmq/queues/" + encodeURIComponent(queueName) + "/messages",
      method: "POST",
      data: payloadInput.val(),
      contentType: "text/plain; charset=utf-8",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (msg.status == "success") {
        payloadInput.val("");
        loadQueues();
      }
    }).fail(function (jqXHR, textStatus) {
      showMessage("Publish failed: " + textStatus, "danger");
    });
  });

  queues.on('click', '.rabbit-select-queue', function () {
    selectedQueue = $(this).data('queue');
    selectedQueueBadge.text(selectedQueue);
    peekCountInput.prop('disabled', false);
    peekButton.prop('disabled', false);
    peekMessages();
  });

  queues.on('click', '.rabbit-delete-queue', function () {
    var queueName = $(this).data('queue');
    $.ajax({
      url: "/rabbitmq/queues/" + encodeURIComponent(queueName),
      method: "DELETE",
      dataType: "json"
    }).done(function (msg) {
      showMessage(msg.message || msg.status, msg.status == "success" ? "success" : "danger");
      if (selectedQueue == queueName) {
        selectedQueue = "";
        selectedQueueBadge.text("No queue selected");
        messages.html("<div class='empty-state'>Select a queue to inspect messages.</div>");
        peekCountInput.prop('disabled', true);
        peekButton.prop('disabled', true);
      }
      loadQueues();
    }).fail(function (jqXHR, textStatus) {
      showMessage("Delete queue failed: " + textStatus, "danger");
    });
  });

  peekButton.click(peekMessages);

  function refreshRabbit() {
    $.ajax({
      url: "/rabbitmq/status",
      method: "GET",
      dataType: "json"
    }).done(function (msg) {
      if (msg.running) {
        status.html("<span class='badge badge-success'>Connected</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>"
            + "<span class='resource-meta'>Management: " + escapeHtml(msg.managementUri) + "</span>"
            + "<span class='resource-meta'>Version: " + escapeHtml(msg.version) + "</span>");
        setRabbitControls(true);
        loadQueues();
      } else {
        status.html("<span class='badge badge-danger'>RabbitMQ unavailable</span>"
            + "<span class='resource-meta'>URL: " + escapeHtml(msg.uri) + "</span>"
            + "<span class='resource-meta'>Management: " + escapeHtml(msg.managementUri) + "</span>");
        setRabbitControls(false);
        queues.html("<div class='empty-state'>RabbitMQ is not running.</div>");
        messages.html("<div class='empty-state'>RabbitMQ is not running.</div>");
      }
    }).fail(function () {
      status.html("<span class='badge badge-danger'>RabbitMQ unavailable</span>");
      setRabbitControls(false);
      queues.html("<div class='empty-state'>RabbitMQ status check failed.</div>");
    });
  }

  function loadQueues() {
    $.ajax({
      url: "/rabbitmq/queues",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      publishQueueInput.empty();
      if (result.length == 0) {
        queues.html("<div class='empty-state'>No queues found.</div>");
        setPublishControls(false);
        return;
      }
      var list = "<div class='list-group'>";
      $.each(result, function (index, queue) {
        publishQueueInput.append($("<option/>").val(queue.name).text(queue.name));
        list = list + "<div class='list-group-item'>"
            + "<strong>" + escapeHtml(queue.name) + "</strong>"
            + "<span class='resource-meta'>URL: " + escapeHtml(queue.address) + "</span>"
            + "<span class='resource-meta'>CreatedOn: " + escapeHtml(queue.createdOn || "Unavailable") + "</span>"
            + "<span class='resource-meta'>Type: " + escapeHtml(queue.type)
            + " | Messages: " + escapeHtml(queue.messages)
            + " | Ready: " + escapeHtml(queue.ready)
            + " | Unacked: " + escapeHtml(queue.unacked)
            + " | Consumers: " + escapeHtml(queue.consumers) + "</span>"
            + "<div class='action-row mt-2'>"
            + "<button type='button' class='btn btn-outline-primary btn-sm rabbit-select-queue' data-queue='"
            + escapeAttribute(queue.name) + "'>View Messages</button>"
            + "<button type='button' class='btn btn-outline-danger btn-sm rabbit-delete-queue' data-queue='"
            + escapeAttribute(queue.name) + "' data-command='curl -u guest:guest -X DELETE http://localhost:15672/api/queues/%2F/"
            + encodeURIComponent(queue.name) + "'>Delete</button>"
            + "</div>"
            + "</div>";
      });
      queues.html(list + "</div>");
      setPublishControls(true);
    }).fail(function (jqXHR, textStatus) {
      showMessage("Queue list failed: " + textStatus, "danger");
      queues.html("<div class='empty-state'>Could not load queues.</div>");
      setPublishControls(false);
    });
  }

  function peekMessages() {
    if (!selectedQueue) {
      showMessage("Select a queue first.", "warning");
      return;
    }
    messages.html("<div class='empty-state'>Loading messages...</div>");
    $.ajax({
      url: "/rabbitmq/queues/" + encodeURIComponent(selectedQueue) + "/messages",
      method: "GET",
      dataType: "json",
      data: {count: peekCountInput.val() || 10}
    }).done(function (result) {
      if (result.length == 0) {
        messages.html("<div class='empty-state'>No messages found.</div>");
        return;
      }
      var html = "";
      $.each(result, function (index, item) {
        html = html + "<div class='pubsub-message-item'>"
            + "<div class='pubsub-message-title'>Message " + (index + 1)
            + " | bytes: " + escapeHtml(item.payloadBytes)
            + " | redelivered: " + escapeHtml(item.redelivered) + "</div>"
            + "<pre class='pubsub-message-body'>" + escapeHtml(item.payload) + "</pre>"
            + "<span class='resource-meta'>Exchange: " + escapeHtml(item.exchange || "(default)")
            + " | Routing key: " + escapeHtml(item.routingKey) + "</span>"
            + "</div>";
      });
      messages.html(html);
    }).fail(function (jqXHR, textStatus) {
      messages.html("<div class='empty-state'>Message load failed.</div>");
      showMessage("Message load failed: " + textStatus, "danger");
    });
  }

  function setRabbitControls(isRunning) {
    startButton.toggle(!isRunning);
    stopButton.toggle(isRunning);
    queueNameInput.prop('disabled', !isRunning);
    queueTypeInput.prop('disabled', !isRunning);
    durableInput.prop('disabled', !isRunning);
    autoDeleteInput.prop('disabled', !isRunning);
    createQueueButton.prop('disabled', !isRunning);
    setPublishControls(isRunning && publishQueueInput.find('option').length > 0);
    if (!isRunning) {
      selectedQueue = "";
      selectedQueueBadge.text("No queue selected");
      peekCountInput.prop('disabled', true);
      peekButton.prop('disabled', true);
    }
  }

  function setPublishControls(isEnabled) {
    publishQueueInput.prop('disabled', !isEnabled);
    payloadInput.prop('disabled', !isEnabled);
    publishButton.prop('disabled', !isEnabled);
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

  function escapeAttribute(value) {
    return escapeHtml(value).replace(/`/g, "&#096;");
  }
});
