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
    "geometria/GMath",
    "geometria/GNotepadContainer",
    "geometria/GPoint",
    "geometria/GProblem",
    "geometria/GSolid",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict, GFace,
            figuresContainer, mainContainer, math, notepadContainer, GPoint, GProblem, GSolid,
            utils, widgets) {

    var helpTopic = "ShearFigure";

    var validate = function(props, results) {
        results = results || { points: [{}, {}, {}] };
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
        var ps = [];
        $.each(props.points, function(index, label) {
            var p = solid.points[label];
            ps.push(p);
        });
        if (math.areCollinearPoints([ps[0].crds, ps[1].crds, ps[2].crds], solid.getRefLength())) {
            return false;
        }
        return true;
    };

    var validateExternal = function(props) {
        if (!Array.isArray(props.points) || props.points.length != 3 || !props.figureName) {
            return false;
        }
        var figure = figuresContainer.getFigure(props.figureName);
        if (!figure) {
            return false;
        }
        if (mainContainer.currentDocument.problem.containsFigure(props.figureName)) {
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
        var ps, stick;
        if (selectedElements.length == 3) {
            if (selectedElements[0] instanceof GPoint) {
                if (selectedElements[1] instanceof GPoint) {
                    if (!(selectedElements[2] instanceof GPoint) &&
                            !(selectedElements[2] instanceof GFace)) {
                        ps = [selectedElements[0], selectedElements[1]];
                        stick = selectedElements[2];
                    }
                }
                else if (!(selectedElements[1] instanceof GPoint) &&
                            !(selectedElements[1] instanceof GFace) &&
                            selectedElements[2] instanceof GPoint) {
                    ps = [selectedElements[0], selectedElements[2]];
                    stick = selectedElements[1];
                }
            }
            else if (!(selectedElements[0] instanceof GPoint) &&
                    !(selectedElements[0] instanceof GFace) &&
                    selectedElements[1] instanceof GPoint &&
                    selectedElements[2] instanceof GPoint) {
                ps = [selectedElements[1], selectedElements[2]];
                stick = selectedElements[0];
            }
        }
        if (ps) {
            var labels;
            if (stick.p1.label == ps[0].label) {
                labels = [stick.p1.label, stick.p2.label, ps[1].label];
            }
            else if (stick.p1.label == ps[1].label) {
                labels = [stick.p1.label, stick.p2.label, ps[0].label];
            }
            else if (stick.p2.label == ps[0].label) {
                labels = [stick.p2.label, stick.p1.label, ps[1].label];
            }
            else if (stick.p2.label == ps[1].label) {
                labels = [stick.p2.label, stick.p1.label, ps[0].label];
            }
            if (labels) {
                var props = { 
                    figureName: figure.name,
                    points: labels
                };
                if (validate(props)) {
                    return props;
                }
            }
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var solidProps = solid.toJson().solid;
        solid.shear(props.points);
        notepadContainer.figureTransformed(props.figureName, props.executeId);
        solid.selection = {};
        figure.draw();
        if (!figure.isInView()) {
            figure.fitToView();
        }
        var outProps = lang.mixin(props, {
            solid: solidProps
        });
        return outProps;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24Shear",

        label: dict.get("action.Shear"),
        
        execute: function(contextMenuTriggered) {
            var executeId = this.getExecuteId();
            var selectionProps = validateSelection();
            if (contextMenuTriggered && selectionProps) {
				selectionProps.executeId = executeId;
                var outProps = apply(selectionProps);
                notepadContainer.update();
                mainContainer.setDocumentModified(true);
                return outProps;
            }
            var dialogDeferred = new Deferred();
            var dialog;
            var figure = figuresContainer.getSelectedFigure();
            var solid = figure.solid;
            var container = new LayoutContainer();
            var topContainer = new LayoutContainer({
                region: "center"
            });
            var pInputs = [];
            var inputEnters = [];
            
            var createInputPane = function(region) {
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
                    region: region,
                    content: pInput
                });
                pContainer.addChild(pPane);
                return pContainer;
            };
            
            var rightPane = new LayoutContainer({
                "class": "geometria_inputcontainer",
                region: "right"
            });
            var leftPane = new LayoutContainer({
                "class": "geometria_inputcontainer",
                region: "left"
            });
            var pPane1 = createInputPane("bottom");
            leftPane.addChild(pPane1);
            var pPane2 = createInputPane("bottom");
            rightPane.addChild(pPane2);
            var pPane3 = createInputPane("top");
            leftPane.addChild(pPane3);
            topContainer.addChild(leftPane);
            topContainer.addChild(rightPane);
            var iconPane = new ContentPane({
                "class": "geometriaIconShear",
                region: "center"
            });
            topContainer.addChild(iconPane);
            container.addChild(topContainer);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_shear",
                dict.get("ShearFigure"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase(),
                                 pInputs[2].get("value").trim().toUpperCase()]
                    };
                    var results = { points: [{}, {}, {}] };
                    var valid = validate(inputProps, results);
                    if (results.points[inputIndex].error) {
                        this.invalidMessage = results.points[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results.points[inputIndex].valid ||
                        !inputProps.points[inputIndex].length;
                });
            });
            if (selectionProps) {
                $.each([0, 1, 2], function(index) {
                    pInputs[this].set("value", selectionProps.points[index]);
                });
            }
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    points: [pInputs[0].get("value").trim().toUpperCase(),
                             pInputs[1].get("value").trim().toUpperCase(),
                             pInputs[2].get("value").trim().toUpperCase()],
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
            var solid = new GSolid().make(props.solid);
            figure.solid = solid;
            notepadContainer.transformFigureUndone(props.executeId);
            notepadContainer.update();
            figure.draw(true);
            if (!figure.isInView()) {
                figure.fitToView();
            }
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

        updateState: function() {
            var doc = mainContainer.currentDocument;
            if (!(doc instanceof GProblem)) {
                var figure = figuresContainer.getSelectedFigure();
                if (figure) {
                    if (doc.problem.containsFigure(figure.name)) {
                        this.base.enabled = false;
                    }
                }
            }
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },
        
        toLog: function(props) {
            return dict.get("ShearFigureAlong", props.figureName, props.points[0],
                props.points[1], props.points[2]);
        },

        toJson: function(props) {
            return {
                "action": "shearAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points
                }
            };
        }
    };
});