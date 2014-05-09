/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "dojo/Deferred",
    "dijit/form/ValidationTextBox",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GDictionary",
    "geometria/GFace",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GMeasurement",
    "geometria/GNotepadContainer",
    "geometria/GNotepadRecord",
    "geometria/GPoint",
    "geometria/GUtils",
    "geometria/GVariable",
    "geometria/GWidgets"
], function(lang, Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict,
            GFace, figuresContainer, mainContainer, GMeasurement, notepadContainer, GNotepadRecord,
            GPoint, utils, GVariable, widgets) {

    var helpTopic = "MeasureDistance";
    var measurementType = "distance";

    var validatePoints = function(props, results) {
        results = results || { points: [{}, {}], variable: {} };
        var solid = figuresContainer.getFigure(props.figureName).solid;
        var pointsValid = true;
        $.each(props.points, function(index, label) {
            if (solid.points[label]) {
                results.points[index].valid = true;
            }
            else {
                pointsValid = false;
                results.points[index].error = dict.get("NoSuchPointInSelectedFigure");
            }
        });
        if (!pointsValid) {
            return false;
        }
        if (props.points[0] == props.points[1]) {
            return false;
        }
        return true;
    };

    var validateVariable = function(props, results) {
        results = results || { points: [{}, {}], variable: {} };
        if (!utils.variableRegex.test(props.variableName)) {
            results.variable.error = dict.get("VariableRule");
            return false;
        }
        if (notepadContainer.getRecord(props.variableName)) {
            results.variable.error = dict.get("VariableAlreadyExists", props.variableName);
            return false;
        }
        results.variable.valid = true;
        return true;
    };

    var validate = function(props, results) {
        var pointsValid = validatePoints(props, results)
        var variableValid = validateVariable(props, results);
        return pointsValid && variableValid;
    };
    
    var validateExternal = function(props) {
        if (!Array.isArray(props.points) || props.points.length != 2 || !props.figureName) {
            return false;
        }
        var figure = figuresContainer.getFigure(props.figureName);
        if (!figure) {
            return false;
        }
        return validate(props);
    };
    
    var validateSelection = function() {
        var figure = figuresContainer.getSelectedFigure();
        var solid = figure.solid;
        var selectedElements = [];
        $.each(solid.selection, function(code, element) {
            selectedElements.push(element);
        });
        var props;
        if (selectedElements.length == 1) {
            if (!(selectedElements[0] instanceof GPoint) &&
                    !(selectedElements[0] instanceof GFace)) {
                props = {
                    figureName: figure.name,
                    points: [selectedElements[0].p1.label, selectedElements[0].p2.label]
                }
            }
        }
        else if (selectedElements.length == 2) {
            if (selectedElements[0] instanceof GPoint &&
                    selectedElements[1] instanceof GPoint) {
                props = { 
                    figureName: figure.name,
                    points: [selectedElements[0].label, selectedElements[1].label]
                };
            }
        }
        if (props && validatePoints(props)) {
            return props;
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var label1 = props.points[0];
        var label2 = props.points[1];
        var p1 = solid.points[label1];
        var p2 = solid.points[label2];
        var distance = vec3.dist(p1.crds, p2.crds);
        var variable = new GVariable(props.variableName, distance);
        var expression = new GMeasurement(measurementType, props);
        var record = new GNotepadRecord(variable, expression);
        notepadContainer.add(record);
        return props;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24MeasureDistance",

        label: dict.get("action.MeasureDistance"),
        
        measurementType: measurementType,

        expressionRegex: /^\|([A-Z][0-9]*)([A-Z][0-9]*)\|$/,

        execute: function() {
            var selectionProps = validateSelection();
            var dialogDeferred = new Deferred();
            var dialog;
            var figure = figuresContainer.getSelectedFigure();
            var container = new LayoutContainer();
            var topContainer = new LayoutContainer({
                region: "center"
            });
            var pInputs = [];
            var inputEnters = [];
            $.each(["left", "right"], function(index, region) {
                var pInputDeferred = new Deferred();
                var pInput = widgets.validationTextBox({
                    "class": "geometria_pointinput",
                    onKeyUp: function(event) {
                        if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                            pInputDeferred.resolve();
                        }
                    }
                });
                pInputs.push(pInput);
                inputEnters.push(pInputDeferred.promise);
                var pContainer = new LayoutContainer({
                    "class": "geometria_inputcontainer",
                    region: region
                });
                var pPane = new ContentPane({
                    "class": "geometria_inputpane",
                    region: "bottom",
                    content: pInput
                });
                pContainer.addChild(pPane);
                topContainer.addChild(pContainer);
            });
            var iconPane = new ContentPane({
                "class": "geometriaIconMeasureDistance",
                region: "center"
            });
            topContainer.addChild(iconPane);
            container.addChild(topContainer);
            if (selectionProps) {
                pInputs[0].set("value", selectionProps.points[0]);
                pInputs[1].set("value", selectionProps.points[1]);
            }
            var bottomContainer = new LayoutContainer({
                region: "bottom",
                style: "height:40px;"
            });
            bottomContainer.addChild(new ContentPane({
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
            inputEnters.push(variableInputDeferred.promise);
            bottomContainer.addChild(variablePane);
            container.addChild(bottomContainer);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_measuredistance",
                dict.get("MeasureDistance"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        variableName: variableInput.get("value").trim(),
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()]
                    };
                    var results = { points: [{}, {}], variable: {} };
                    var valid = validate(inputProps, results);
                    if (results.points[inputIndex].error) {
                        this.invalidMessage = results.points[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results.points[inputIndex].valid ||
                        !inputProps.points[inputIndex].length;
                });
            });
            variableInput.set("validator", function() {
                var inputProps = {
                        figureName: figure.name,
                        variableName: variableInput.get("value").trim(),
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()]
                };
                var results = { points: [{}, {}], variable: {} };
                var valid = validate(inputProps, results);
                if (results.variable.error) {
                    this.invalidMessage = results.variable.error;
                }
                dialog.okButton.set("disabled", !valid);
                return results.variable.valid || !inputProps.variableName.length;
            });
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    variableName: variableInput.get("value").trim(),
                    points: [pInputs[0].get("value").trim().toUpperCase(),
                             pInputs[1].get("value").trim().toUpperCase()]
                };
                var outProps = apply(inputProps);
                figure.solid.selection = {};
                figure.draw();
                mainContainer.setDocumentModified(true);
                dialogDeferred.resolve(outProps);
            });
            return dialogDeferred.promise;
        },

        getExpression: function(props) {
            return "|" + props.points[0] + props.points[1] + "|";
        },
        
        validateSelection: validateSelection,
        
        undo: function(props) {
            notepadContainer.removeLastRecord();
        },
        
        playBack: function(props, external) {
            if (external && !validateExternal(props)) {
                return null;
            }
            return apply(props);
        },

        evaluate: function(props) {
            var outProps = {
                figureName: props.figureName,
                variableName: props.variableName
            };
            var match = new RegExp(this.expressionRegex).exec(props.expression);
            outProps.points = [match[1], match[2]];
            if (!validateExternal(outProps)) {
                return null;
            }
            return apply(outProps);
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },
        
        toLog: function(props) {
            return dict.get("MeasureDistanceInFigure", props.variableName, props.points[0],
                props.points[1], props.figureName);
        },

        toJson: function(props) {
            return {
                "action": "measureDistanceAction",
                "props": {
                    "figureName": props.figureName,
                    "variableName": props.variableName,
                    "points": props.points
                }
            };
        }
    };
});