$(document).ready(function () {
  var bucketList = $('#bucketList');

  apiCall("/s3-buckets", bucketList);

  function apiCall(url, snsList) {
    $.ajax({
      url: url,
      method: "GET",
      dataType: "html"
    }).done(function (msg) {
      var result = $.parseJSON(msg);
      var listElement = "<ul class='list-group'>";
      if (result.length > 0) {
        $.each(result, (function (index, element) {

          listElement = listElement + "<li class='list-group-item'>"
              + functionGenerateLinkBucket(url, element)
              + "<span data-type=\"bucket\" data-name=\"" + element.name
              + "\" class=\"badge badge-danger delete\">Delete</span></li>";
        }));
      } else {
        listElement = listElement + "<li class='list-group-item text-muted'>No buckets</li>";
      }
      listElement = listElement + "</ul>";
      snsList.html(listElement);
    }).fail(function (jqXHR, textStatus) {
      console.log("Request failed: " + textStatus);
    });
  }

  function functionGenerateLinkBucket(prePender, bucket) {
    var localstackUrl = "http://s3.us-east-1.localhost.localstack.cloud:4566/" + bucket.name;
    var createdOn = formatDisplayTime(bucket.creationDate);
    return "<a class='resource-link' href=" + prePender + "/" + bucket.name + ">"
        + bucket.name + "</a>"
        + "<span class='resource-meta'>URL: " + localstackUrl + "</span>"
        + "<span class='resource-meta'>CreatedOn: " + createdOn + "</span>";
  }

  function formatDisplayTime(value) {
    if (!value) {
      return "Unavailable";
    }
    var date = new Date(value);
    if (isNaN(date.getTime())) {
      return String(value).substring(0, 16);
    }
    return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate())
        + "T" + pad(date.getHours()) + ":" + pad(date.getMinutes());
  }

  function pad(value) {
    return String(value).padStart(2, "0");
  }

  $(document).on('click', "span.delete", function () {
    var name = $(this).data("name");
    var url;
      url = "/s3-buckets/delete/" + name;
    $.ajax({
      url: url,
      method: "POST",
      contentType: "application/json; charset=utf-8",
    }).done(function (msg) {
      console.log("successfully deleted!");
      apiCall("/s3-buckets", bucketList);
    }).fail(function (jqXHR, textStatus) {
      console.log("Request failed: " + textStatus);
    });
  });

  $('#createBucket').click(function () {
    var bucketName = $('#bucketName').val();
    if (bucketName.length != 0) {
      var endPointToCreateQueue = "/s3-buckets/create/" + bucketName;
      create(endPointToCreateQueue);
      $('#bucketName').val("");
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
      apiCall("/s3-buckets", bucketList);
    }).fail(function (jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
    });
  }
});
