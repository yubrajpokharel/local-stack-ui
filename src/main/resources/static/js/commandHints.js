$(document).ready(function () {
  var storageKey = "localstackui.showCommands";

  $.ajaxSetup({timeout: 20000});
  applyPreference(localStorage.getItem(storageKey) == "true");
  decorateCommands();
  ensureToggle();

  $(document).ajaxComplete(function () {
    decorateCommands();
    ensureToggle();
  });

  $(document).on('change input', 'input, textarea, select', function () {
    refreshCommandText();
  });

  $(document).on('change', '#showCommandHints', function () {
    var enabled = $(this).is(':checked');
    localStorage.setItem(storageKey, String(enabled));
    applyPreference(enabled);
    refreshCommandText();
  });

  function applyPreference(enabled) {
    $('body').toggleClass('command-hints-visible', enabled);
  }

  function ensureToggle() {
    if ($('#showCommandHints').length || $('[data-command], [data-command-template]').length == 0) {
      return;
    }
    var checked = localStorage.getItem(storageKey) == "true" ? " checked" : "";
    var toggle = "<label class='command-toggle'>"
        + "<input id='showCommandHints' type='checkbox'" + checked + "> Show commands"
        + "</label>";
    $('.page-header:first').append(toggle);
  }

  function decorateCommands() {
    $('[data-command], [data-command-template]').each(function () {
      var element = $(this);
      if (element.data('commandDecorated')) {
        return;
      }
      element.data('commandDecorated', true);
      var hint = $("<pre class='command-hint'></pre>");
      hint.text(resolveCommand(element));
      if (element.closest('.action-row').length) {
        element.after(hint);
      } else {
        element.after(hint);
      }
    });
    refreshCommandText();
  }

  function refreshCommandText() {
    $('[data-command], [data-command-template]').each(function () {
      var element = $(this);
      var nextHint = element.next('.command-hint');
      if (nextHint.length) {
        var command = resolveCommand(element);
        if (nextHint.text() != command) {
          nextHint.text(command);
        }
      }
    });
  }

  function resolveCommand(element) {
    var command = element.attr('data-command-template') || element.attr('data-command') || "";
    return command.replace(/\{([^}]+)\}/g, function (match, selector) {
      if (!selector.match(/^[#.\[]/)) {
        return match;
      }
      var target;
      try {
        target = $(selector);
      } catch (e) {
        return match;
      }
      if (!target.length) {
        return match;
      }
      return target.val() || "";
    });
  }
});
