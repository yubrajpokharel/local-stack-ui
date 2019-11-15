<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: yubrajpokhrel
  Date: 11/6/19
  Time: 11:52 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>${sqsQueue}</title>
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
                        <div class="col-lg-5">
                            <img alt="SQS" class="img-fluid"
                                 src="https://panoply.io/images/integration-logos/sqs.svg">
                        </div>
                        <div class="col-lg-7" style="margin-top: 50px">
                            <h4 class="align-middle">${sqsQueue}</h4>
                        </div>
                        <div class="col-lg-12" style="padding-top: 2em">
                            <h5>Messages</h5>
                            <ul class='list-group'>
                                <c:if test="${not empty messages}">
                                    <c:forEach items="${messages}" var="message">
                                        <li class='list-group-item'><span
                                                class="badge badge-success">${message.messageId}</span> -> ${message.body}
                                        </li>
                                    </c:forEach>
                                </c:if>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
