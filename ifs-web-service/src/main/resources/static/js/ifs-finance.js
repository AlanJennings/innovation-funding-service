//Innovation Funding Services javascript by Worth
var worthIFSFinance = {
    domReady : function(){

        if(jQuery('body.app-form').size()==1){
            worthIFSFinance.initFinanceCalculations();
        }
    },
    initFinanceCalculations : function(){
        function getFieldValue(selector, currentInputElement){
            // check if the parent tr element has a element that matches the selector, if not, search the whole document.

            if(currentInputElement != null && currentInputElement.parents("tr").find(selector).length == 1){
                console.log(currentInputElement.parents("tr").find(selector).length);
                return currentInputElement.parents("tr").find(selector).val();
            }else{
                console.log("use else:", selector, $(selector).length);
                if($(selector).length == 1){
                    return $(selector).val();
                }else{
                    values = [];
                    $(selector).each(function(index){
                        values[index] = parseInt($(this).val());
                    });
                    return values;
                }

            }
        }
        // calculations made easy: MathOperation['*'](4, 5)
        var MathOperation = {
            '+': function (x, y) { return x + y },
            '+': function (x) { if(_.isArray(x)) return _.reduce(x, function(memo, num){ return memo + num; }, 0); },
            '-': function (x, y) { return x - y },
            '*': function (x, y) { return x * y },
            '/': function (x, y) { return x / y },
        }


        $("[data-calculation-input]").on('change', function(e){
            changedInputElement = $(this);
            console.log('changedInputElement ', changedInputElement);

            // search the element that uses this value for there calculation.
            fieldIdentifier= changedInputElement.data('calculation-input')
            resultElement = changedInputElement.parents("tr").next().find(".calculation-result-js[data-calculation-field*="+fieldIdentifier+"]");
            // if in the current scope there is no calculation result found, use the full document to search.
            if(resultElement.length == 0){
                findByData = $("[data-calculation-field*="+fieldIdentifier+"]");
                console.log("--", findByData);
                resultElement = findByData;
            }

            fields = resultElement.data("calculation-field").split(',');
            operations = resultElement.data("calculation-operations").split(',');
            console.log("fields", fields);
            console.log("operations", operations);
            result = 0.0;

            //TODO: make this fully dynamic so we can use it with unlimited number of fields.
            if(fields.length == 1 && operations.length == 1){
                // ignore current scope, search whole document.
                value1 = getFieldValue(fields[0], null);
                result = MathOperation[operations[0]](value1);
            }else if(fields.length == 2){
                value1 = getFieldValue(fields[0], changedInputElement);
                value2 = getFieldValue(fields[1], changedInputElement);
                result = MathOperation[operations[0]](value1, value2);
            }else if(fields.length == 3){
                value1 = getFieldValue(fields[0], changedInputElement);
                value2 = getFieldValue(fields[1], changedInputElement);
                value3 = getFieldValue(fields[2], changedInputElement);
                console.log(value1, value2, value3);

                result1 = MathOperation[operations[0]](value1, value2);
                result = MathOperation[operations[1]](result1, value3);
            }else{
                console.error("unsupported calculation..");
            }
            console.log("result: ", result);

            // this will ignore all numbers after the comma. (not rounding down)
            resultElement.val(parseInt(result));
            resultElement.trigger('change');
        });
    },
} 

jQuery(document).ready(function(){
  worthIFSFinance.domReady();
});



