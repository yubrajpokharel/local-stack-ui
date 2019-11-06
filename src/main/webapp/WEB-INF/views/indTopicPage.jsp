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
    <link rel="stylesheet"
          href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
          integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
          crossorigin="anonymous">
</head>
<body>
<div class="container">
    <div class="row">
        <div class="col-lg-12">
            <div class="row">
                <div class="col-lg-3">
                    <img alt="SNS"
                         src="https://www.dreamfactory.com/assets/images/logos/integration/amazon-sns.png">
                </div>
                <div class="col-lg-9" style="margin-top: 50px">
                    <h4 class="align-middle">${topicArn}</h4>
                </div>
                <div class="col-lg-12">
                    <h5>Subscriptions</h5>
                    <ul class='list-group'>
                        <c:if test="${not empty subscriptions}">
                            <c:forEach items="${subscriptions}" var="subscription">
                                <li class='list-group-item'>${subscription}</li>
                            </c:forEach>
                        </c:if>
                    </ul>
                </div>
                <hr style="padding-top: 10em"/>
                <div class="col-lg-12">
                    <h5 class="h5">Send Message to topic</h5>
                    <div class="form-group">
                        <textarea class="form-control" id="messageBox" rows="3"></textarea>
                        <input type="hidden" id="topicArn" value="${topicArn}">
                    </div>
                </div>
                <div class="col-lg-12" style="padding-top: 2em">
                    <button type="button" class="btn btn-dark" id="sendMessage">Send</button>
                </div>
                <div class="col-lg-12" id="messageResult" style="padding-top: 2em; display: none">
                    Message ID : <span id="messageId"></span>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://code.jquery.com/jquery-3.4.1.min.js"
        integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="
        crossorigin="anonymous"></script>
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
  });
</script>
</body>
</html>
