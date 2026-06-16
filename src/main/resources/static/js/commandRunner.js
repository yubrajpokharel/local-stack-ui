$(document).ready(function () {
  var cwdInput = $('#commandRunnerCwd');
  var output = $('#commandOutput');
  var message = $('#commandRunnerMessage');
  var exitCode = $('#commandRunnerExitCode');
  var runButton = $('#runCommand');

  loadDirectories();

  $('#commandTemplate').change(function () {
    var command = $(this).val();
    if (command) {
      $('#commandInput').val(command);
    }
  });

  $('#clearCommandOutput').click(function () {
    output.text("Output cleared.");
    exitCode.removeClass('badge-success badge-danger badge-secondary').addClass('badge-light')
        .text("Not run");
    message.empty();
  });

  runButton.click(function () {
    var cwd = cwdInput.val();
    var command = $('#commandInput').val();
    if (!cwd || !command) {
      setMessage("Working directory and command are required.", "warning");
      return;
    }
    runButton.prop('disabled', true).text("Running...");
    output.text("Running " + command + "\n\nWorking directory: " + cwd);
    exitCode.removeClass('badge-success badge-danger badge-light').addClass('badge-secondary')
        .text("Running");
    $.ajax({
      url: "/command-runner/run",
      method: "POST",
      dataType: "json",
      data: {
        cwd: cwd,
        command: command
      }
    }).done(function (result) {
      if (result.status == "success") {
        var code = Number(result.exitCode);
        exitCode.removeClass('badge-light badge-secondary badge-success badge-danger')
            .addClass(code == 0 ? 'badge-success' : 'badge-danger')
            .text("Exit " + result.exitCode);
        output.text("$ cd " + result.cwd + "\n$ " + result.command + "\n\n"
            + (result.output || "Command finished with no output."));
        setMessage(code == 0 ? "Command completed." : "Command finished with a non-zero exit code.",
            code == 0 ? "success" : "warning");
      } else {
        exitCode.removeClass('badge-light badge-secondary badge-success').addClass('badge-danger')
            .text("Failed");
        output.text(result.message || "Command failed.");
        setMessage(result.message || "Command failed.", "danger");
      }
    }).fail(function (jqXHR) {
      var text = jqXHR.responseText || "Command request failed.";
      exitCode.removeClass('badge-light badge-secondary badge-success').addClass('badge-danger')
          .text("Failed");
      output.text(text);
      setMessage(text, "danger");
    }).always(function () {
      runButton.prop('disabled', false).text("Run");
    });
  });

  function loadDirectories() {
    $.ajax({
      url: "/command-runner/directories",
      method: "GET",
      dataType: "json"
    }).done(function (result) {
      $('#commandRunnerDirectories').empty();
      $.each(result, function (index, directory) {
        $('#commandRunnerDirectories').append($("<option/>").val(directory));
      });
      if (result.length > 0) {
        var springProject = result.find(function (directory) {
          return directory.indexOf("/spring6") >= 0;
        });
        cwdInput.val(springProject || result[0]);
      }
    }).fail(function (jqXHR) {
      setMessage(jqXHR.responseText || "Unable to load allowed directories.", "danger");
    });
  }

  function setMessage(text, type) {
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
});
