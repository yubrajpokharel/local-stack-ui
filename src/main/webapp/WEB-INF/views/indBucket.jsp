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
        <img style="height: 20px; margin: 10px" class="img-fluid" alt="SNS"
             src="/resources/images/awstool.png"/>
        &nbsp;Home</a>
    <h2 class="font-weight-bold text-center text-lg-left mt-4 mb-0">S3 - Bucket</h2>
    <hr class="mt-2 mb-5">
    <input id="bucketName" value="${bucketName}" name="bucketName" type="hidden">
    <div class="row">
        <div class="col-lg-12">
            <div class="row">
                <div class="col-lg-5">
                    <img style="height: 200px; margin: 10px" alt="SQS"
                         class="img-fluid img-thumbnail"
                         src="/resources/images/s3.png">
                </div>
                <div class="col-lg-7" style="margin-top: 50px">
                    <h4 class="align-middle">${bucketName}</h4>
                </div>
                <div class="col-lg-12 mt-2">
                    <!-- Button trigger modal -->
                    <button type="button" class="btn btn-primary" data-toggle="modal"
                            data-target="#addFileModal">
                        Upload file
                    </button>
                </div>
                <div class="col-lg-12" style="padding-top: 2em">
                    <h5>Contents</h5>

                    <table class="table table-striped" style="font-size: 0.8em">
                        <thead>
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col">Name</th>
                            <th scope="col">Action</th>
                        </tr>
                        </thead>
                        <tbody id="fileContents"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>


<!-- Modal -->
<div class="modal fade" id="addFileModal" tabindex="-1" role="dialog"
     aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="exampleModalLabel">Upload file to ${bucketName}</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="uploadForm" class="form-inline" method="POST"
                      enctype="multipart/form-data"
                      action="/s3-buckets/uploadFile/${bucketName}">
                    <div class="form-group mx-sm-3 mb-2">
                        <label for="file" class="sr-only">Select File: </label>
                        <input type="file" id="file" name="file" placeholder="Password">
                    </div>
                    <button type="submit" class="btn btn-primary mb-2">Upload</button>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
            </div>
        </div>
    </div>
</div>


<script src="https://code.jquery.com/jquery-3.4.1.min.js"
        integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="
        crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"
        integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"
        crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"
        integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl"
        crossorigin="anonymous"></script>

<script type="text/javascript">
  $(document).ready(function () {

    getInternalContent();

    $('#myModal').on('shown.bs.modal', function () {
      $('#myInput').trigger('focus')
    });

    $("#uploadForm").submit(function (evt) {
      evt.preventDefault();
      var formData = new FormData($(this)[0]);
      $.ajax({
        url: $("#uploadForm").attr('action'),
        type: 'POST',
        data: formData,
        contentType: false,
        enctype: 'multipart/form-data',
        processData: false,
        success: function (response) {
          $('#addFileModal').modal('toggle');
          getInternalContent();
        }
      });
      return false;
    });

    function getInternalContent() {
      var url = "/s3-buckets/get/" + $("#bucketName").val();
      var listParent = $('#fileContents');
      var innerContent = "";
      $.ajax({
        url: url,
        type: 'GET',
        contentType: "application/json; charset=utf-8",
        success: function (result) {
          if (result.length > 0) {
            $.each(result, (function (index, element) {
              var fileName = element.split("/").pop();
              innerContent = innerContent + '<tr id="' + fileName + '">' +
                  '<th scope="row">' + index + '</th>' +
                  '<td><b>' + fileName + '</b></td>' +
                  '<td data-name="' + element + '"  class="deleteFile">Delete</td>' +
                  '</tr>'
            }));
          } else {
            var innerContent = innerContent + '<tr>' +
                '<th scope="row">0</th>' +
                '<td><b>Bucket is empty.</b></td>' +
                '<td></td>' +
                '</tr>'
          }
          listParent.html(innerContent);
        }
      });
    }

    $(document).on('click', "td.deleteFile", function () {
      var key = $(this).data("name");
      $.ajax({
        url: "/s3-buckets/deleteFile/" + $("#bucketName").val() + "/" + key,
        type: 'DELETE',
        contentType: "application/json; charset=utf-8",
        success: function (result) {
          getInternalContent();
        }
      });

    });

  });
</script>
</body>
</html>
