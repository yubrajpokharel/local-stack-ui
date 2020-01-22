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
            <a class="mt-5" href="/" title="home">
                <img style="height: 20px; margin: 10px" class="img-fluid" alt="SNS" src="/resources/images/awstool.png" />
                &nbsp;Home</a>
            <h2 class="font-weight-bold text-center text-lg-left mt-4 mb-0">SQS</h2>
            <hr class="mt-2 mb-5">

            <div class="row">
                <div class="col-lg-12">
                    <div class="row">
                        <div class="col-lg-5">
                            <img style="height: 200px; margin: 10px" alt="SQS" class="img-fluid img-thumbnail"
                                 src="/resources/images/sqs.png">
                        </div>
                        <div class="col-lg-7" style="margin-top: 50px">
                            <h4 class="align-middle">${sqsQueue}</h4>
                        </div>
                        <div class="col-lg-12" style="padding-top: 2em">
                            <h5>Messages</h5>

                            <table class="table table-striped" style="font-size: 0.8em">
                                <thead>
                                <tr>
                                    <th scope="col">#</th>
                                    <th scope="col">ID</th>
                                    <th scope="col">Message</th>
                                </tr>
                                </thead>
                                <tbody>
                                <ul class='list-group'>
                                    <c:if test="${not empty messages}">
                                        <c:forEach items="${messages}" var="message" varStatus="loop">
                                        <tr>
                                            <th scope="row">${loop.index}</th>
                                            <td><b>${message.messageId}</b></td>
                                            <td>${message.body}</td>
                                        </tr>
                                        </c:forEach>
                                    </c:if>
                                </ul>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
