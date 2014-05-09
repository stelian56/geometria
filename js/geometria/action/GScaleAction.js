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

    var helpTopic = "ScaleFigure";

    var validatePoints = function(props, results) {
        results = results || { points: [{}, {}], factor: {} };
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
        if (props.points[1] == props.points[2]) {
            return false;
        }
        return true;
    };

    var validate = function(props, results) {
        var solid = figuresContainer.getFigure(props.figureName).solid;
        results = results || { points: [{}, {}], factor: {} };
        var pointsValid = validatePoints(props, results);
        var scope = notepadContainer.getScope();
        var factor = utils.eval(props.factor, scope);
        if (isNaN(factor)) {
            results.factor.error = dict.get("EnterValidExpression");
        }
        else if (factor <= 0) {
            results.factor.error = dict.get("EnterPositiveExpression");
        }
        else {
            results.factor.valid = true;
            return true;
        }
        return false;
    };

    var validateExternal = function(props) {
        if (!Array.isArray(props.points) || props.points.length != 2 || !props.figureName) {
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
        var p1, p2;
        if (selectedElements.length == 1) {
            if (!(selectedElements[0] instanceof GPoint) &&
                    !(selectedElements[0] instanceof GFace)) {
                p1 = selectedElements[0].p1;
                p2 = selectedElements[0].p2;
            }
        }
        if (selectedElements.length == 2) {
            if (selectedElements[0] instanceof GPoint && selectedElements[1] instanceof GPoint) {
                p1 = selectedElements[0];
                p2 = selectedElements[1];
            }
        }
        if (p1 && p2) {
            var labels = [p1.label, p2.label];
            var props = { 
                figureName: figure.name,
                points: labels
            };
            if (validatePoints(props)) {
                return props;
            }
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var scope = notepadContainer.getScope();
        var factor = utils.eval(props.factor, scope);
        var solidProps = solid.toJson().solid;
        solid.scale(props.points[0], props.points[1], factor);
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

        icon: "geometriaIcon24 geometriaIcon24Scale",

        label: dict.get("action.Scale"),
        
        execute: function() {
            var executeId = this.getExecuteId();
            var selectionProps = validateSelection();
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
            topContainer.addChild(leftPane);
            topContainer.addChild(rightPane);
            var iconPane = new ContentPane({
                "class": "geometriaIconScale",
                region: "center"
            });
            topContainer.addChild(iconPane);
            container.addChild(topContainer);
            if (selectionProps) {
                $.each([0, 1], function(index) {
                    pInputs[this].set("value", selectionProps.points[index]);
                });
            }
            var factorInputDeferred = new Deferred();
            var factorInput = widgets.validationTextBox({
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        factorInputDeferred.resolve();
                    }
                }
            });
            var bottomContainer = new LayoutContainer({
                region: "bottom",
                style: "height:40px;"
            });
            var labelPane = new ContentPane({
                region: "left",
                content: dict.get("ByFactor")
            });
            bottomContainer.addChild(labelPane);
            var factorPane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: factorInput
            });
            inputEnters.push(factorInputDeferred.promise);
            bottomContainer.addChild(factorPane);
            container.addChild(bottomContainer);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_scale",
                dict.get("ScaleFigure"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        factor: factorInput.get("value").trim(),
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()]
                    };
                    var results = { points: [{}, {}], factor: {} };
                    var valid = validate(inputProps, results);
                    if (results.points[inputIndex].error) {
                        this.invalidMessage = results.points[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results.points[inputIndex].valid ||
                        !inputProps.points[inputIndex].length;
                });
            });
            factorInput.set("validator", function() {
                var inputProps = {
                        figureName: figure.name,
                        factor: factorInput.get("value").trim(),
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()]
                };
                var results = { points: [{}, {}], factor: {} };
                var valid = validate(inputProps, results);
                if (results.factor.error) {
                    this.invalidMessage = results.factor.error;
                }
                dialog.okButton.set("disabled", !valid);
                return results.factor.valid || !inputProps.factor.length;
            });
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    factor: factorInput.get("value").trim(),
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
            return dict.get("ScaleFigureByFactor", props.figureName, props.factor, props.points[0],
                props.points[1]);
        },

        toJson: function(props) {
            return {
                "action": "scaleAction",
                "props": {
                    "figureName": props.figureName,
                    "factor": props.factor,
                    "points": props.points
                }
            };
        }
    };
});
