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
              + functionGenerateLinkBucket(url, element.name) +
              "&nbsp;&nbsp;&nbsp;<span data-type = \"bucket\"data-name ="
              + element.name
              + " class=\"badge badge-danger delete\">Delete</span></li>";
        }));
      } else {
        listElement = listElement + "<li class='list-group-item'>No Buckets</li>";
      }
      listElement = listElement + "</ul>";
      snsList.html(listElement);
    }).fail(function (jqXHR, textStatus) {
      console.log("Request failed: " + textStatus);
    });
  }

  function functionGenerateLinkBucket(prePender, path) {
    return "<a href=" + prePender + "/" + path + ">"
        + path + "</a>";
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


