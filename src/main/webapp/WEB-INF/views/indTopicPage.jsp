<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: yubrajpokhrel
  Date: 11/5/19
  Time: 1:00 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${topicArn}</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="/resources/css/app.css">
</head>
<body>
<div class="container app-shell">
    <a class="app-top-link" href="/" title="home">
        <img class="img-fluid" alt="Home" src="/resources/images/awstool.png" />
        Home</a>
    <div class="page-header">
        <img class="img-fluid" alt="SNS" src="/resources/images/sns.png" />
        <div>
            <h2 class="font-weight-bold">SNS</h2>
            <span class="resource-name">${topicArn}</span>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-7 mb-4">
            <div class="card app-panel">
                <div class="card-header">Subscriptions</div>
                <div class="card-body">
                    <table class="table table-striped table-sm">
                        <thead>
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col">ARN</th>
                            <th scope="col">Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:if test="${not empty subscriptions}">
                            <c:forEach items="${subscriptions}" var="subscription" varStatus="loop">
                                <tr>
                                    <th scope="row">${loop.index}</th>
                                    <td class="code-cell"><b>${subscription.key}</b></td>
                                    <td><button id="${subscription.key}" data-id="${subscription.key}" data-name="${subscription.value}" type="button" class="btn btn-danger btn-sm removeSubsription">Remove</button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:if>
                        </tbody>
                    </table>
                    <c:if test="${empty subscriptions}">
                        <div class="empty-state">No subscriptions found.</div>
                    </c:if>
                </div>
            </div>
        </div>
        <div class="col-lg-5 mb-4">
            <div class="card app-panel">
                <div class="card-header">Send Message</div>
                <div class="card-body">
                    <h5 class="h5">Send Message to topic</h5>
                    <div class="form-group">
                        <textarea class="form-control compact-textarea" id="messageBox" rows="8"></textarea>
                        <input type="hidden" id="topicArn" value="${topicArn}">
                    </div>
                    <button type="button" class="btn btn-primary" id="sendMessage">Send</button>
                    <div class="alert alert-success mt-3" id="messageResult" style="display: none">
                        Message ID : <span id="messageId"></span>
                    </div>
                </div>
            </div>
            </div>
    </div>
</div>

<script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript">
  $(document).ready(function () {
    var messageArea = $("#messageId");
    var topicArn = $('#topicArn').val();
    $('#sendMessage').click(function () {
      var message = $('#messageBox').val();
      if (message.length == 0) {
        alert("message is empty please try again!")
      } else {
        sendMessage(topicArn, message, messageArea);
      }
    });

    function sendMessage(topicArn, message, messageArea) {
      $.ajax({
        url: "/sns-topics/sendMessage/" + topicArn,
        method: "POST",
        contentType: "application/json; charset=utf-8",
        data: message,
      }).done(function (msg) {
        messageArea.text(msg);
        $('#messageResult').show();
      }).fail(function (jqXHR, textStatus) {
        alert("Request failed: " + textStatus);
      });
    }

  //  remove the subscription
    $('.removeSubsription').click(function(){
      var name = $(this).data("name");
      var id = $(this).data("id");
      var url = "/unsubscribe/" + name;
      $.ajax({
        url: url,
        method: "DELETE",
        contentType: "application/json; charset=utf-8",
      }).done(function (msg) {
        //$(this).hide();
        alert(msg);
        location.reload();
      }).fail(function (jqXHR, textStatus) {
        alert("Request failed: " + textStatus);
      });
    });
  });
</script>
</body>
</html>
