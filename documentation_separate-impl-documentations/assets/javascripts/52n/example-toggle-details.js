(function($) {
  'use strict';
  
  var N52ContentToggler = {
      showContent: function(content, button) {
          content.removeClass('hide');
          button.setAttribute('title', 'Hide content');
      },
      hideContent: function(content, button) {
          content.addClass('hide');
          button.setAttribute('title', 'Show content');
      }
  };
  
  $(function() {
    $('.n52-example-toggler.btn').each(function(i, element) {
        var button = element;
        var toggle_block = $(button).nextAll('div');
        if (button.classList.contains('active')) {
            N52ContentToggler.showContent(toggle_block, button);
        } else {
            N52ContentToggler.hideContent(toggle_block, button);
        }
    });
    $('.n52-example-toggler.btn').on('click', function (event) {
        var button = event.currentTarget;
        var toggle_block = $(button).nextAll('div')
        if (toggle_block.hasClass('hide')) {
            N52ContentToggler.showContent(toggle_block, button);
        } else {
            N52ContentToggler.hideContent(toggle_block, button);
        }
      });
  });
})(jQuery);