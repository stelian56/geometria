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
    "geometria/GNotepadContainer",
    "geometria/GPoint",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict,
            GFace, figuresContainer, mainContainer, notepadContainer, GPoint, utils, widgets) {

    var helpTopic = "EraseSegment";

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
        var lines = solid.linesThroughPoints(props.points[0], props.points[1]);
        if (!utils.anyProp(lines)) {
            return false;
        }
        var faces = solid.facesThroughPoints([props.points[0], props.points[1]]);
        if (utils.propCount(faces) > 1) {
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
        var props;
        if (selectedElements.length == 1) {
            if (!(selectedElements[0] instanceof GPoint || selectedElements[0] instanceof GFace)) {
                props = { 
                    figureName: figure.name,
                    points: [selectedElements[0].p1.label, selectedElements[0].p2.label]
                };
            }
        }
        else if (selectedElements.length == 2) {
            if (selectedElements[0] instanceof GPoint && selectedElements[1] instanceof GPoint) {
                props = { 
                    figureName: figure.name,
                    points: [selectedElements[0].label, selectedElements[1].label]
                };
            }
        }
        if (props && validate(props)) {
            return props;
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
        var removedPoints = [];
        var addedLines = [];
        var removedLabels = [];
        var removedLine = face.removeLineThroughPoints(label1, label2, addedLines, removedLabels);
        $.each(removedLabels, function(index, label) {
            var pJson = solid.points[this].toJson();
            removedPoints.push(pJson);
            delete solid.points[label];
        });
        $.each(removedLabels, function(index, label) {
            notepadContainer.pointRemoved(label, props.figureName, props.executeId);
        });
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        var outProps = lang.mixin({
            face: face.getFaceCode(),
            removedLine: removedLine,
            removedPoints: removedPoints,
            addedLines: addedLines
        }, props);
        return outProps;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        label: dict.get("action.EraseSegment"),
        
        execute: function(contextMenuTriggered) {
            var dialogDeferred = new Deferred();
            var dialog;
            var executeId = this.getExecuteId();
            var figure = figuresContainer.getSelectedFigure();
            var solid = figure.solid;
            var selectionProps = validateSelection();
            if (contextMenuTriggered && selectionProps) {
                selectionProps.executeId = executeId;
                var outProps = apply(selectionProps);
                mainContainer.setDocumentModified(true);
                return outProps;
            }
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
                "class": "geometriaIconEraseSegment",
                region: "center"
            });
            container.addChild(iconPane);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_erasesegment",
                dict.get("EraseSegment"), pInputEnters);
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
                             pInputs[1].get("value").trim().toUpperCase()],
                    executeId: executeId
                };
                var outProps = apply(inputProps);
                notepadContainer.update();
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
            face.undoRemoveLine(props.removedLine, props.addedLines);
            $.each(props.removedPoints, function() {
                var p = new GPoint();
                p.make(this);
                solid.points[p.label] = p;
            });
            $.each(props.removedPoints, function(label) {
                notepadContainer.removePointUndone(props.executeId);
            });
            notepadContainer.update();
            solid.makeConfig();
            figure.draw(true);
            figuresContainer.select(props.figureName);
        },
        
        playBack: function(props, external) {
            var outProps = lang.mixin({}, props);
            if (external) {
                outProps.executeId = this.base.getExecuteId();
                if (!validateExternal(outProps)) {
                    return null;
                }
            }
            outProps = apply(outProps);
            if (!external) {
                notepadContainer.update();
            }
            return outProps;
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },
        
        toLog: function(props) {
            return dict.get("EraseSegmentInFigure", props.points[0], props.points[1],
                props.figureName);
        },

        toJson: function(props) {
            return {
                "action": "eraseSegmentAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points
                }
            };
        }
    };
});
