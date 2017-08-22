(function($) {
  'use strict';

  $(function() {
    $('.swagger-method-title').bind('click', function (e) {
        var source = e.currentTarget;
        var details = $('.swagger-method-details', source.parentElement)
        if (details.hasClass('open')) {
          details.removeClass('open');
          e.preventDefault();
        }
        else {
          details.addClass('open');
          e.preventDefault();
        }
      });
  });
})(jQuery);