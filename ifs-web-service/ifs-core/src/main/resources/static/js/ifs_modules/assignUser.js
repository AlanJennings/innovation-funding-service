IFS.progressiveSelect = (function(){
    "use strict";
    var s;
    var index =0;
    return {
        settings : {
            selectEl : "select.prog-menu",
            dropdownContainer : ".assign-container .assign-button"
        },
        init : function(){
            s = this.settings;
            IFS.progressiveSelect.initDomChanges();
            IFS.progressiveSelect.initEventHandlers();
        },
        initEventHandlers : function (){
          jQuery(document).on('click',function(){
              IFS.progressiveSelect.hideAll();
          });
          jQuery('body').on('click', '.assign-button [aria-controls]' , function(e){
            e.stopPropagation();
            IFS.progressiveSelect.toggleDropdown(this);
          });
        },
        initDomChanges : function(){
          jQuery(s.dropdownContainer).each(function(){
            IFS.progressiveSelect.initDropDownHTML(this);
          });
          jQuery(s.selectEl).each(function(){
            IFS.progressiveSelect.selectToListHTML(this);
          });
        },
        selectToListHTML : function(el){
          el = jQuery(el);
          el.next('button').remove();
          var children = el.children('option');
          var name = el.attr('name');
          var html = '<ul class="list-bullet">';
          children.each(function(){
          	var inst = jQuery(this);
            var content = inst.text();
            var value = inst.attr('value');

            if(inst.attr('disabled') == 'disabled'){
              html += '<li>'+content+'</li>';
            }
            else {
              html += '<li><button value="'+value+'" name="'+name+'" class="buttonlink">'+content+'</button></li>';
            }
          });
          html+='</ul>';
          el.after(html).remove();
        },
        initDropDownHTML : function(el){
            var inst = jQuery(el);
            var id = 'dropdown-'+index;   // create unique id for a11y relationship
            // wrap the content and make it focusable
            inst.nextUntil('h2,h3').wrapAll('<div id="'+ id +'" aria-hidden="true">');
            // Add the button inside the <h2> so both the heading and button semanics are read
            inst.wrapInner('<button aria-expanded="false" aria-controls="'+ id +'" type="button">');
            index++;
        },
        toggleDropdown : function(el){
            var inst = jQuery(el);
            var dropdown = jQuery('#'+inst.attr('aria-controls'));
            var state = inst.attr('aria-expanded') === 'false' ? true : false;

            IFS.progressiveSelect.hideAll();
            if(state){
              inst.attr('aria-expanded', 'true');
              dropdown.attr('aria-hidden', 'false');
            }
        },
        hideAll : function(){
          jQuery('[aria-controls*="dropdown-"][aria-expanded="true"]').attr('aria-expanded','false');
          jQuery('[id^="dropdown-"][aria-hidden="false"]').attr('aria-hidden','true');
        }

    };
})();