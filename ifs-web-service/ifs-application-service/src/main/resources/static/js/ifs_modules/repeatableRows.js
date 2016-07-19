// Set up the handlers for adding and removing Cost Category costs rows
IFS.application.repeatableRows = (function() {
    "use strict";

    return {
        init: function(){
            IFS.application.repeatableRows.backForwardCacheReload();
            jQuery('body').on('click', '[data-repeatable-rowcontainer]', function(e){
              e.preventDefault();
              IFS.application.repeatableRows.backForwardCacheInvalidate();
              IFS.application.repeatableRows.addRow(this,e);
            });
            jQuery('body').on('click', '.js-remove-row',function(e){
              IFS.application.repeatableRows.backForwardCacheInvalidate();
              IFS.application.repeatableRows.removeRow(this,e);
            });
        },
        getAjaxUrl : function(el){
            var url = '';
            if(typeof(jQuery(el).val()) !== 'undefined' && typeof(jQuery(el).attr('name')) !== 'undefined' && jQuery("#application_id").length == 1){
                var applicationId =  jQuery("#application_id").val();
                url = window.location.protocol + '//'+window.location.host + '/application/' + applicationId + '/form/'+ jQuery(el).attr('name') + '/' + jQuery(el).val();
            }
            return url;
        },
        addRow : function(el,event){
            var url = IFS.application.repeatableRows.getAjaxUrl(el);
            if(url.length){
              event.preventDefault();
              jQuery.ajaxProtected({
                  url : url,
                  beforeSend : function(){
                      jQuery(el).before('<span class="form-hint">Adding a new row</span>');
                  },
                  cache: false
              }).done(function(data){
                  var target = jQuery(el).attr("data-repeatable-rowcontainer");
                  jQuery(el).prev().remove();
                  jQuery(target).append(data);
                  jQuery('body').trigger('updateSerializedFormState');
                  var appendRow = jQuery(data).find('[type="number"][name],[type="text"][name],[type="email"][name]').first().attr('name');
                  if(typeof(appendRow) !== 'undefined'){
                      jQuery('[name='+appendRow+']').focus();
                  }
              });
            }
        },
        removeRow : function(el,event){
            var url = IFS.application.repeatableRows.getAjaxUrl(el);
            if(url.length){
              event.preventDefault();
              jQuery.ajaxProtected({
                  url : url
              }).done(function(data){
                  data = jQuery.parseJSON(data);
                  if(data.status == 'OK'){
                    jQuery('[data-repeatable-row='+jQuery(el).val()+']').remove();
                    jQuery('body').trigger('recalculateAllFinances').trigger('updateSerializedFormState');
                  }
              });
            }
        },
        backForwardCacheReload : function(){
          //INFUND-2965 ajax results don't show when using the back button on the page after
          var input = jQuery('#cacheTest');
          if (input.val() !== "") {
            // the page has been loaded from the cache as the #cachetest has a value
            // equivalent of persisted == true
            jQuery('#cacheTest').val("");
            window.location.reload();
          }
        },
        backForwardCacheInvalidate : function(){
          // change the input value so that we can detect
          // if the page is reloaded from cache later
          jQuery('#cacheTest').val("cached");
        }
    };
})();