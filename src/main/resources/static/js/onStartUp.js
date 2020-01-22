$(document).ready(function () {
  var snsList = $('#snsList');
  var sqsList = $('#sqsList');
  var snsOptions = $('#topicOptions');
  var sqsOptions = $('#queueOptions');

  apiCall("/sns-topics", snsList, "topic");
  apiCall("/sqs", sqsList, "queue");
  populateSelectOptions("/sns-topics", snsOptions);
  populateSelectOptions("/sqs", sqsOptions);

  function apiCall(url, snsList, queueOrTopic) {
    $.ajax({
      url: url,
      method: "GET",
      dataType: "html"
    }).done(function (msg) {
      var result = $.parseJSON(msg);
      var listElement = "<ul class='list-group'>";
      $.each(result, (function (index, element) {
        if (queueOrTopic == "queue") {
          listElement = listElement + "<li class='list-group-item'>"
              + functionGenerateLinkForQueue(url, element) +
              "&nbsp;&nbsp;&nbsp;<span data-type = \"queue\"data-name ="
              + element
              + " class=\"badge badge-danger delete\">Delete</span></li>";
        } else {
          listElement = listElement + "<li class='list-group-item'>"
              + functionGenerateLink(url, element) +
              "&nbsp;&nbsp;&nbsp;<span data-type = \"topic\"data-name ="
              + element
              + " class=\"badge badge-danger delete\">Delete</span></li>";
        }
      }));
      listElement = listElement + "</ul>";
      snsList.html(listElement);
    }).fail(function (jqXHR, textStatus) {
      console.log("Request failed: " + textStatus);
    });
  }

  function populateSelectOptions(url, selectId) {
    $.ajax({
      url: url,
      method: "GET",
      dataType: "html"
    }).done(function (msg) {
      var result = $.parseJSON(msg);
      var listElement = "<ul class='list-group'>";
      selectId.empty();
      $.each(result, (function (index, element) {
        selectId.append($("<option/>").val(element).text(element));
      }));
    }).fail(function (jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
    });
  }

  function functionGenerateLinkForQueue(prePender, path) {
    var sqsName = path.split("/").pop();
    return "<a href=" + "sqs-message" + "/" + sqsName + " title=" + sqsName + ">"
        + sqsName + "</a>";
  }

  function functionGenerateLink(prePender, path) {
    var sns = path.split(":");
    var snsName = sns[sns.length - 1];
    return "<a href=" + prePender + "/" + path + " title=" + path + ">"
        + snsName + "</a>";
  }

  $('#createTopic').click(function () {
    var topicName = $('#topicName').val();
    if (topicName.length != 0) {
      var endPointToCreateTopic = "/sns/createTopic/" + topicName;
      create(endPointToCreateTopic);
      $('#topicName').val("");
    } else {
      alert("topic name cannot be empty");
    }
  });

  $('#createQueue').click(function () {
    var queueName = $('#queueName').val();
    if (topicName.length != 0) {
      var endPointToCreateQueue = "/sqs/createQueue/" + queueName;
      create(endPointToCreateQueue);
      $('#queueName').val("");
    } else {
      alert("queue name cannot be empty");
    }
  });

  function create(url) {
    $.ajax({
      url: url,
      method: "POST",
      contentType: "application/json; charset=utf-8",
    }).done(function (msg) {
      console.log(msg);
      apiCall("/sns-topics", snsList, "topic");
      apiCall("/sqs", sqsList, "queue");
      populateSelectOptions("/sns-topics", snsOptions);
      populateSelectOptions("/sqs", sqsOptions);
    }).fail(function (jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
    });
  }

  function subscribe(url) {
    $.ajax({
      url: url,
      method: "POST",
      contentType: "application/json; charset=utf-8",
    }).done(function (msg) {
      alert("successfully subscribed to topic");
    }).fail(function (jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
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
      apiCall("/sns-topics", snsList, "topic");
      apiCall("/sqs", sqsList, "queue");
      populateSelectOptions("/sns-topics", snsOptions);
      populateSelectOptions("/sqs", sqsOptions);
    }).fail(function (jqXHR, textStatus) {
      console.log("Request failed: " + textStatus);
    });
  });

  $('#enroll').click(function () {
    var queue = $('#queueOptions').val().split('/').pop();
    var topic = $('#topicOptions').val();
    if (queue.length == 0 || topic.length == 0) {
      alert("Queue and topic names cannot be empty");
    } else {
      subscribe("/subscribe/" + queue + "/" + topic);
    }
  });
});


