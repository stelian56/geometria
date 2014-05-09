/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "geometria/GActions",
    "geometria/GDictionary",
    "geometria/GWidgets"
], function(lang, actions, dict, widgets) {

    var textBox;

    var showError = function(error) {
        textBox.textbox.selectionStart = error.bounds.begin;
        textBox.textbox.selectionEnd = error.bounds.end;
        textBox.set("invalidMessage", error.message);
        textBox.set("validator", function() { return false; });
        textBox.validate();
        textBox.set("validator", function() { return true; });
    };

    var evaluate = function() {
        var input = textBox.get("value");
        actions.calculateAction.base.execute(input, showError, textBox);
    };

    return {
        
        startUp: function() {
            var args = arguments[0];
            lang.mixin(args, {
                "class": "geometria_calculator",
                placeHolder: dict.get("Calculator"),
                onKeyUp: function(event) {
                    if (event.keyCode == 13) {
                        evaluate();
                    }
                }
            });
            textBox = widgets.validationTextBox(args);
            return textBox;
        },
        
        clear: function() {
            textBox.set("value", "");
        }
    };
});
