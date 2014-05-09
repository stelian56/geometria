/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "geometria/GActions"
], function(declare, actions) {

    return declare(null, {

        constructor: function(expression) {
            this.expression = expression;
        },
        
        make: function(props) {
            this.expression = props.calculation.expression;
            var outProps = {
                variableName: props.variableName,
                expression: this.expression
            };
            actions.calculateAction.playBack(outProps, true);
        },

        variableRenamed: function(oldName, newName) {
            var expression = " " + this.expression + " ";
            var regex = new RegExp("(\\W)" + oldName + "(\\W)");
            while (regex.test(expression)) {
                expression = expression.replace(regex, "$1" + newName + "$2");
            }
            this.expression = expression.substring(1, expression.length -1);
        },
        
        toJson: function() {
            var json = {
                "calculation": {
                    "expression": this.expression
                }
            };
            return json;
        }
    });
});