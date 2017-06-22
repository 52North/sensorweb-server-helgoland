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
      },
      toggleContent: function(button) {
          var toggle_block = $(button).next('div');
          if (button.classList.contains('active')) {
              this.showContent(toggle_block, button);
          } else {
              this.hideContent(toggle_block, button);
          }
      },
  };
  
  $(function() {
    $('.n52-example-toggler.btn').each(function(i, element) {
        var button = element;
        var toggle_block = $(button).next('div');
        if (button.classList.contains('active')) {
            N52ContentToggler.showContent(toggle_block, button);
        } else {
            N52ContentToggler.hideContent(toggle_block, button);
        }
    });
    $('.n52-example-toggler.btn').bind('click', function (event) {
        var button = event.currentTarget;
        var toggle_block = $(button).next('div')
        if (toggle_block.hasClass('hide')) {
            N52ContentToggler.showContent(toggle_block, button);
        }
        else {
            N52ContentToggler.hideContent(toggle_block, button);
        }
      });
  });
})(jQuery);