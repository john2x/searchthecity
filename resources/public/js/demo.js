!function($) {
  // suggestions
  $("#suggestions li a").on("click", function(e) {
    var $this = $(this);
    $("input[name='q']").val($this.text());
  });

  var setResult = function(text) {
    $("#search-results").text(text);
  };
  var clearResult = function() {
    setResult("Enter a query to get started.");
  };
  var showResult = function(result) {
    var prettyResult = JSON.stringify(result, null, 2);
    setResult(prettyResult);
  };

  // AJAX on /api
  $("form").on("submit", function(e) {
    var q = $("input[name='q']").val(),
        $button = $("form button"),
        $querystring = $("#query-string"),
        $preloader = $("#preloader");
    $button.text("Searching...");
    $querystring.show();
    $querystring.text("http://searchthecity.me/api?q=" + encodeURIComponent(q));
    $button.attr("disabled", "disabled");
    $preloader.show();
    $.ajax("/api?q=" + q, {
      success: function(data, textStatus) {
        clearResult();
        showResult(data);
      },
      error: function(jqXHR, textStatus, e) {
        setResult("Oops, something went wrong. It would be awesome if you could tweet me (@john2x) the query you tried. Thanks!");
      },
      complete: function() {
        $("form button").text("Search");
        $button.removeAttr("disabled");
        $preloader.hide();
      }
    });
    return false;
  });

  $(".clear").on("click", function(e) {
    $("#query-string").hide();
    clearResult();
  });
}(window.jQuery)
