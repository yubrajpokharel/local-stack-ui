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
        <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/css/bootstrap.min.css">
        <link rel="stylesheet" href="/resources/css/app.css">
    </head>
    <body>
        <div class="container app-shell">
            <a class="app-top-link" href="/" title="home">
                <img class="img-fluid" alt="Home" src="/resources/images/awstool.png" />
                Home</a>

            <div class="page-header">
                <img class="img-fluid" alt="SQS" src="/resources/images/sqs.png" />
                <div>
                    <h2 class="font-weight-bold">SQS</h2>
                    <span class="resource-name">${sqsQueue}</span>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-5 mb-4">
                    <div class="card app-panel">
                        <div class="card-header">Send Message</div>
                        <div class="card-body">
                            <h5>Send Message</h5>
                            <input type="hidden" id="queueName" value="${sqsQueue}">
                            <div class="form-group">
                                <textarea class="form-control compact-textarea" id="sqsMessageBox" rows="7"></textarea>
                            </div>
                            <button type="button" class="btn btn-primary" id="sendSqsMessage">Send</button>
                            <div class="alert alert-success mt-3" id="sqsSendResult" style="display: none">
                                Message ID : <span id="sqsMessageId"></span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-7 mb-4">
                    <div class="card app-panel">
                        <div class="card-header">Messages</div>
                        <div class="card-body">

                            <table class="table table-striped table-sm">
                                <thead>
                                <tr>
                                    <th scope="col">#</th>
                                    <th scope="col">ID</th>
                                    <th scope="col">Message</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <c:if test="${not empty messages}">
                                        <c:forEach items="${messages}" var="message" varStatus="loop">
                                        <tr>
                                            <th scope="row">${loop.index}</th>
                                            <td class="code-cell"><b>${message.messageId}</b></td>
                                            <td class="code-cell">${message.body}</td>
                                        </tr>
                                        </c:forEach>
                                    </c:if>
                                    <c:if test="${empty messages}">
                                        <tr>
                                            <td colspan="3" class="text-muted">No messages found.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
    <script src="/webjars/jquery/3.4.1/jquery.min.js"></script>
    <script type="text/javascript" src="/resources/js/indSqsPage.js"></script>
</html>
