/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GMeasurement",
    "geometria/GNotepadContainer",
    "geometria/GNotepadRecord",
    "geometria/GUtils",
    "geometria/GVariable",
    "geometria/GWidgets"
], function(Deferred, ContentPane, LayoutContainer, dict, figuresContainer, mainContainer,
            GMeasurement, notepadContainer, GNotepadRecord, utils, GVariable, widgets) {

    var helpTopic = "ComputeTotalArea";
    var measurementType = "totalArea";
    
    var validate = function(props, results) {
        results = results || {};
        if (!utils.variableRegex.test(props.variableName)) {
            results.error = dict.get("VariableRule");
            return false;
        }
        if (notepadContainer.getRecord(props.variableName)) {
            results.error = dict.get("VariableAlreadyExists", props.variableName);
            return false;
        }
        results.valid = true;
        return true;
    };
    
    var validateExternal = function(props) {
        if (!props.figureName) {
            return false;
        }
        if (!props.variableName) {
            return false;
        }
        if (!figuresContainer.getFigure(props.figureName)) {
            return false;
        }
        return validate(props);
    };
    
    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var area = figure.solid.computeTotalArea();
        var variable = new GVariable(props.variableName, area);
        var expression = new GMeasurement(measurementType, props);
        var record = new GNotepadRecord(variable, expression);
        notepadContainer.add(record);
        return props;
    };

    return {

        loggable: true,
        
        figureSpecific: true,
    
        label: dict.get("action.TotalArea"),

        measurementType: measurementType,

        expressionRegex: /^totalArea$/,
        
        execute: function() {
            var dialogDeferred = new Deferred();
            var dialog;
            var container = new LayoutContainer();
            container.addChild(new ContentPane({
                region: "left",
                content: dict.get("AssignToVariable")
            }));
            var variableInputDeferred = new Deferred();
            var variableInput = widgets.validationTextBox({
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        variableInputDeferred.resolve();
                    }
                }
            });
            var variablePane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: variableInput
            });
            container.addChild(variablePane);
            var figure = figuresContainer.getSelectedFigure();
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_totalarea",
                dict.get("TotalAreaOf", figure.name), [variableInputDeferred.promise]);
            dialog.okButton.set("disabled", true);
            variableInput.set("validator", function() {
                var inputProps = {
                    variableName: variableInput.get("value").trim()
                };
                var results = {};
                var valid = validate(inputProps, results);
                if (results.error) {
                    this.invalidMessage = results.error;
                }
                dialog.okButton.set("disabled", !valid);
                return results.valid || !inputProps.variableName.length;
            });
            dialog.ok.then(function() {
                var inputProps = {
                    figureName: figuresContainer.getSelectedFigure().name,
                    variableName: variableInput.get("value").trim()
                };
                var outProps = apply(inputProps);
                figure.solid.selection = {};
                figure.draw();
                mainContainer.setDocumentModified(true);
                dialogDeferred.resolve(outProps);
            });
            return dialogDeferred.promise;
        },
        
        getExpression: function() {
            return "totalArea";
        },
        
        undo: function() {
            notepadContainer.removeLastRecord();
        },
        
        playBack: function(props, external) {
            if (external && !validateExternal(props)) {
                return null;
            }
            return apply(props);
        },

        evaluate: function(props) {
            if (!validateExternal(props)) {
                return null;
            }
            return apply(props);
        },
        
        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            return dict.get("ComputeTotalAreaOfFigure", props.variableName, props.figureName);
        },
        
        toJson: function(props) {
            return {
                "action": "totalAreaAction",
                "props": {
                    "figureName": props.figureName,
                    "variableName": props.variableName
                }
            };
        }
    };
});
