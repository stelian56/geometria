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
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GPoint",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict,
            figuresContainer, mainContainer, GPoint, utils, widgets) {

    var helpTopic = "JoinPoints";

    var validate = function(props, results) {
        results = results || [{}, {}];
        var solid = figuresContainer.getFigure(props.figureName).solid;
        var pointsValid = true;
        $.each(props.points, function(index, label) {
            if (solid.points[label]) {
                results[index].valid = true;
            }
            else {
                pointsValid = false;
                results[index].error = dict.get("NoSuchPointInSelectedFigure");
            }
        });
        if (!pointsValid) {
            return false;
        }
        if (props.points[0] == props.points[1]) {
            return false;
        }
        var faces = solid.facesThroughPoints([props.points[0], props.points[1]]);
        if (!utils.anyProp(faces)) {
            return false;
        }
        var lines = solid.linesThroughPoints(props.points[0], props.points[1]);
        if (utils.anyProp(lines)) {
            return false;
        }
        return true;
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
        if (selectedElements.length == 2 &&
                selectedElements[0] instanceof GPoint &&
                selectedElements[1] instanceof GPoint) {
            var props = { 
                figureName: figure.name,
                points: [selectedElements[0].label, selectedElements[1].label]
            };
            if (validate(props)) {
                return props;
            }
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var label1 = props.points[0];
        var label2 = props.points[1];
        var faces = solid.facesThroughPoints([label1, label2]);
        var face = utils.anyProp(faces);
        var removedLines = [];
        var addedLine = face.addLine(label1, label2, removedLines, solid);
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        var outProps = lang.mixin({
            face: face.getFaceCode(),
            addedLine: addedLine.getStickCode(),
            removedLines: removedLines
        }, props);
        return outProps;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24JoinPoints",

        label: dict.get("action.JoinPoints"),
        
        execute: function(contextMenuTriggered) {
            var selectionProps = validateSelection();
            if (contextMenuTriggered && selectionProps) {
                var outProps = apply(selectionProps);
                mainContainer.setDocumentModified(true);
                return outProps;
            }
            var dialogDeferred = new Deferred();
            var dialog;
            var figure = figuresContainer.getSelectedFigure();
            var solid = figure.solid;
            var container = new LayoutContainer();
            var pInputs = [];
            var pInputEnters = [];
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
                pInputEnters.push(pInputDeferred.promise);
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
                container.addChild(pContainer);
            });
            var iconPane = new ContentPane({
                "class": "geometriaIconJoinPoints",
                region: "center"
            });
            container.addChild(iconPane);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_joinpoints",
                dict.get("JoinPoints"), pInputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()]
                    };
                    var results = [{}, {}];
                    var valid = validate(inputProps, results);
                    if (results[inputIndex].error) {
                        this.invalidMessage = results[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results[inputIndex].valid || !inputProps.points[inputIndex].length;
                });
            });
            if (selectionProps) {
                pInputs[0].set("value", selectionProps.points[0]);
                pInputs[1].set("value", selectionProps.points[1]);
            }
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    points: [pInputs[0].get("value").trim().toUpperCase(),
                             pInputs[1].get("value").trim().toUpperCase()]
                };
                var outProps = apply(inputProps);
                mainContainer.setDocumentModified(true);
                dialogDeferred.resolve(outProps);
            });
            return dialogDeferred.promise;
        },

        validateSelection: validateSelection,
        
        undo: function(props) {
            var figure = figuresContainer.getFigure(props.figureName);
            var solid = figure.solid;
            solid.selection = {};
            var face = solid.getFace(props.face);
            face.undoAddLine(props.addedLine, props.removedLines);
            solid.makeConfig();
            figure.draw(true);
            figuresContainer.select(props.figureName);
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
            return dict.get("JoinPointsInFigure", props.points[0], props.points[1],
                props.figureName);
        },

        toJson: function(props) {
            return {
                "action": "joinPointsAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points
                }
            };
        }
    };
});
