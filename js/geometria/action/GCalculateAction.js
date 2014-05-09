/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GCalculation",
    "geometria/GDictionary",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GNotepadRecord",
    "geometria/GUtils",
    "geometria/GVariable"
], function(GCalculation, dict, mainContainer, notepadContainer, GNotepadRecord, utils,
            GVariable) {

    var validate = function(props, results) {
        results = results || {};
        if (!props.variableName) {
            throw "Missing variable in calculation";
        }
        if (!utils.variableRegex.test(props.variableName)) {
            results.type = "variable";
            results.error = dict.get("VariableRule");
            return false;
        }
        if (notepadContainer.getRecord(props.variableName)) {
            results.type = "variable";
            results.error = dict.get("VariableAlreadyExists", props.variableName);
            return false;
        }
        var scope = notepadContainer.getScope();
        if (!props.expression) {
            return false;
        }
        var value = utils.eval(props.expression, scope);
        if (isNaN(value)) {
            results.type = "expression";
            results.error = dict.get("BadExpression");
            return false;
        }
        return true;
    };

    var validateExternal = function(props) {
        return validate(props);
    };
    
    var apply = function(props) {
        var scope = notepadContainer.getScope();
        var value = utils.eval(props.expression, scope);
        var calculation = new GCalculation(props.expression);
        var variable = new GVariable(props.variableName, value);
        var record = new GNotepadRecord(variable, calculation);
        notepadContainer.add(record);
        return props;
    };

    return {

        loggable: true,

        execute: function(input, showError, textBox) {
            var expressionOffset = 0;
            var variableName;
            var expression;
            var tokens = input.split("=");
            var pos;
            if (tokens.length == 1) {
                var scope = notepadContainer.getScope();
                var value = utils.eval(input, scope);
                if (!isNaN(value)) {
                    textBox.set("value", value);
                }
                else {
                    showError({
                        message: dict.get("BadExpression"),
                        bounds: { begin: 0, end: input.length }
                    });
                }
            }
            else if (this.enabled) {
                if (tokens.length > 2) {
                    pos = input.lastIndexOf("=");
                    showError({
                        message: dict.get("MisplaceEqualitySign"),
                        bounds: {
                            begin: pos,
                            end: pos + 1
                        }
                    });
                    return null;
                }
                else  {
                    expression = tokens[1];
                    expressionOffset = input.indexOf("=") + 1;
                    variableName = tokens[0].trim();
                    var props = {
                        variableName: variableName,
                        expression: expression
                    };
                    var results = {};
                    if (validate(props, results)) {
                        var outProps = apply(props);
                        textBox.set("value", "");
                        mainContainer.setDocumentModified(true);
                        return outProps;
                    }
                    else {
                        pos = input.indexOf("=");
                        var bounds = results.type == "variable" ? { begin: 0, end: pos } :
                            { begin: pos + 1, end: input.length };
                        showError({
                            message: results.error,
                            bounds: bounds
                        });
                    }
                }
            }
            else {
                showError({
                    message: dict.get("SolutionPlaybackInProgress"),
                    bounds: {
                        begin: 0,
                        end: input.length
                    }
                });
            }
            return null;
        },
        
        undo: function(props) {
            notepadContainer.removeLastRecord();
        },
        
        playBack: function(props, external) {
            if (external && !validateExternal(props)) {
                return null;
            }
            return apply(props);
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },
        
        toLog: function(props) {
            return dict.get("Calculate", props.variableName, props.expression);
        },
        
        toJson: function(props) {
            return {
                "action": "calculateAction",
                "props": {
                    "variableName": props.variableName,
                    "expression": props.expression
                }
            };
        }
    };
});
