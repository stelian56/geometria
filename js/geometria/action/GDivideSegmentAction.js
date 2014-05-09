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
], function(lang, Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict, GFace,
            figuresContainer, mainContainer, notepadContainer, GPoint, utils, widgets) {

    var helpTopic = "DivideSegment";

    var validatePoints = function(props, results) {
        results = results || { points: [{}, {}], ratio: [{}, {}] };
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
        var lines = solid.linesThroughPoints(props.points[0], props.points[1]);
        if (!utils.anyProp(lines)) {
            return false;
        }
        return true;
    };

    var getDivisionPoint = function(props) {
        var scope = notepadContainer.getScope();
        var ratio = [];
        $.each([0, 1], function(index) {
            ratio[index] = utils.eval(props.ratio[index], scope);
        });
        var solid = figuresContainer.getFigure(props.figureName).solid;
        var p1 = solid.points[props.points[0]];
        var p2 = solid.points[props.points[1]];
        var crds = vec3.create();
        var scaledCrds1 = vec3.create();
        vec3.scale(scaledCrds1, p1.crds, ratio[1]);
        var scaledCrds2 = vec3.create();
        vec3.scale(scaledCrds2, p2.crds, ratio[0]);
        vec3.add(crds, scaledCrds1, scaledCrds2);
        vec3.scale(crds, crds, 1/(ratio[0] + ratio[1]));
        return crds;
    };
    
    var validate = function(props, results) {
        results = results || { points: [{}, {}], ratio: [{}, {}] };
        var pointsValid = validatePoints(props, results);
        var scope = notepadContainer.getScope();
        var ratio = [];
        var ratioTermsValid = true;
        $.each([0, 1], function(index) {
            ratio[index] = utils.eval(props.ratio[index], scope);
            if (isNaN(ratio[index])) {
                ratioTermsValid = false;
                results.ratio[index].error = dict.get("EnterValidExpression");
            }
            else if (ratio[index] <= 0) {
                ratioTermsValid = false;
                results.ratio[index].error = dict.get("EnterPositiveExpression");
            }
            else {
                results.ratio[index].valid = true;
            }
        });
        if (pointsValid && ratioTermsValid) {
            var crds = getDivisionPoint(props);
            var solid = figuresContainer.getFigure(props.figureName).solid;
            if (!solid.pointAt(crds)) {
                return true;
            }
        }
        return false;
    };
    
    var validateExternal = function(props) {
        if (!Array.isArray(props.points) || props.points.length != 2 || !props.figureName) {
            return false;
        }
        if (!Array.isArray(props.ratio) || props.ratio.length != 2) {
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
        if (props && validatePoints(props)) {
            return props;
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var crds = getDivisionPoint(props);
        var p = solid.addPoint(crds);
        var faces = solid.facesThroughPoints([props.points[0], props.points[1]]);
        $.each(faces, function() {
            this.addPoint(p, solid);
        });
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        var outProps = lang.mixin({
            addedPoint: p.label
        }, props);
        return outProps;
    };
    
    return {

        loggable: true,
        
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24DivideSegment",
        
        label: dict.get("action.DivideSegment"),
        
        execute: function(contextMenuTriggered, midpoint) {
            var selectionProps = validateSelection();
            if (contextMenuTriggered && selectionProps && midpoint) {
                selectionProps.ratio = ["1", "1"];
                var outProps = apply(selectionProps);
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
                "class": "geometriaIconDivideSegment",
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
                style: "height: 40px"
            });
            bottomContainer.addChild(new ContentPane({
                region: "left",
                style: "width:50px;",
                content: dict.get("InRatio")
            }));
            var ratioInputs = [];
            var ratioContainer = new LayoutContainer({
                region: "center"
            });
            ratioContainer.addChild(new ContentPane({
                "class": "geometria_ratiodivider",
                region: "center",
                content: ":"
            }));
            $.each(["left", "right"], function(index, region) {
                var ratioInputDeferred = new Deferred();
                var ratioInput = widgets.validationTextBox({
                    "class": "geometria_pointinput",
                    onKeyUp: function(event) {
                        if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                            ratioInputDeferred.resolve();
                        }
                    }
                });
                ratioInputs.push(ratioInput);
                inputEnters.push(ratioInputDeferred.promise);
                var rContainer = new LayoutContainer({
                    "class": "geometria_inputcontainer",
                    region: region
                });
                var rPane = new ContentPane({
                    "class": "geometria_inputpane",
                    region: "bottom",
                    content: ratioInput
                });
                rContainer.addChild(rPane);
                ratioContainer.addChild(rContainer);
            });
            bottomContainer.addChild(ratioContainer);
            bottomContainer.addChild(new ContentPane({
                region: "right",
                style: "width:50px;"
            }));
            container.addChild(bottomContainer);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_dividesegment",
                dict.get("DivideSegment"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()],
                        ratio: [ratioInputs[0].get("value").trim(),
                                 ratioInputs[1].get("value").trim()]
                    };
                    var results = { points: [{}, {}], ratio: [{}, {}] };
                    var valid = validate(inputProps, results);
                    if (results.points[inputIndex].error) {
                        this.invalidMessage = results.points[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results.points[inputIndex].valid ||
                        !inputProps.points[inputIndex].length;
                });
            });
            $.each(ratioInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()],
                        ratio: [ratioInputs[0].get("value").trim(),
                                 ratioInputs[1].get("value").trim()]
                    };
                    var results = { points: [{}, {}], ratio: [{}, {}] };
                    var valid = validate(inputProps, results);
                    if (results.ratio[inputIndex].error) {
                        this.invalidMessage = results.ratio[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results.ratio[inputIndex].valid ||
                        !inputProps.ratio[inputIndex].length;
                });
            });
            if (midpoint) {
                $.each([0, 1], function(index) {
                    ratioInputs[index].set("value", "1");
                    ratioInputs[index].set("disabled", true);
                });
            }
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    points: [pInputs[0].get("value").trim().toUpperCase(),
                             pInputs[1].get("value").trim().toUpperCase()],
                    ratio: [ratioInputs[0].get("value").trim(),
                             ratioInputs[1].get("value").trim()]
                };
                var outProps = apply(inputProps);
                mainContainer.setDocumentModified(true);
                dialogDeferred.resolve(outProps);
            });
            return dialogDeferred.promise;
        },
        
        validateSelection: validateSelection,
        
        validate: validate,
        
        undo: function(props) {
            var figure = figuresContainer.getFigure(props.figureName);
            var solid = figure.solid;
            solid.selection = {};
            var faces = solid.facesThroughPoints([props.points[0], props.points[1]]);
            var label = props.addedPoint;
            $.each(faces, function() {
                this.removePoint(label);
            });
            delete solid.points[label];
            solid.makeConfig();
            figure.draw(true);
            figuresContainer.select(props.figureName);
        },
        
        playBack: function(props) {
            if (external && !validateExternal(props)) {
                return null;
            }
            return apply(props);
        },
        
        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            return dict.get("DivideSegmentInRatio", props.points[0],
                props.points[1], props.ratio[0], props.ratio[1], props.figureName);
        },
        
        toJson: function(props) {
            return {
                "action": "divideSegmentAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points,
                    "ratio": props.ratio
                }
            };
        }
    };
});
