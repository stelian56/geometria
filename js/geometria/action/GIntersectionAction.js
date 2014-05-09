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
    "geometria/GPoint",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict,
            GFace, figuresContainer, mainContainer, math, GPoint, utils, widgets) {

    var helpTopic = "DrawIntersection";

    var validate = function(props, results) {
        results = results || [{}, {}, {}, {}];
        var solid = figuresContainer.getFigure(props.figureName).solid;
        var ps = [];
        var pointsValid = true;
        $.each(props.points, function(index, label) {
            var p = solid.points[label];
            if (p) {
                results[index].valid = true;
                ps.push(p);
            }
            else {
                pointsValid = false;
                results[index].error = dict.get("NoSuchPointInSelectedFigure");
            }
        });
        if (!pointsValid) {
            return false;
        }
        if (props.points[0] == props.points[1] || props.points[2] == props.points[3]) {
            return false;
        }
        var faces = solid.facesThroughPoints(props.points);
        var face = utils.anyProp(faces);
        if (!face) {
            return false;
        }
        var lines = solid.linesThroughPoints(props.points[0], props.points[1]);
        var line1 = utils.anyProp(lines);
        if (!line1) {
            return false;
        }
        lines = solid.linesThroughPoints(props.points[2], props.points[3]);
        var line2 = utils.anyProp(lines);
        if (!line2) {
            return false;
        }
        var refLength = solid.getRefLength();
        var crds = math.intersect(ps[0].crds, ps[1].crds, ps[2].crds, ps[3].crds, refLength);
        if (!crds || !face.covers(crds, solid)) {
            return false;
        }
        if (solid.pointAt(crds)) {
            return false;
        }
        return true;
    };
    
    var validateExternal = function(props) {
        if (!props.points || props.points.length != 4 || !props.figureName) {
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
        if (selectedElements.length == 2) {
            if (!(selectedElements[0] instanceof GPoint ||
                    selectedElements[0] instanceof GFace ||
                    selectedElements[1] instanceof GPoint ||
                    selectedElements[1] instanceof GFace)) {
                var labels1 = [];
                var labels2 = [];
                var labels = [selectedElements[0].p1.label, selectedElements[0].p2.label,
                    selectedElements[1].p1.label, selectedElements[1].p2.label];
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
        var refLength = solid.getRefLength();
        var ps = [];
        $.each(props.points, function(index, label) {
            ps.push(solid.points[label].crds);
        });
        var crds = math.intersect(ps[0], ps[1], ps[2], ps[3], refLength);
        var p = solid.addPoint(crds);
        var lines = solid.linesThroughPoints(props.points[0], props.points[1]);
        var line1 = utils.anyProp(lines);
        lines = solid.linesThroughPoints(props.points[2], props.points[3]);
        var line2 = utils.anyProp(lines);
        line1.addPoint(p, solid);
        line2.addPoint(p, solid);
        var faces = solid.facesThroughPoints([props.points[0], props.points[1]]);
        $.each(faces, function(code, face) {
            if (this.getFaceCode() != code) {
                this.addPoint(p, solid);
            }
        });
        faces = solid.facesThroughPoints([props.points[2], props.points[3]]);
        $.each(faces, function(code, face) {
            if (this.getFaceCode() != code) {
                this.addPoint(p, solid);
            }
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

        icon: "geometriaIcon24 geometriaIcon24Intersection",

        label: dict.get("action.IntersectLines"),
        
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
            
            var leftPane = new LayoutContainer({
                "class": "geometria_inputcontainer",
                region: "left"
            });
            var rightPane = new LayoutContainer({
                "class": "geometria_inputcontainer",
                region: "right"
            });
            var pPane11 = createInputPane("top");
            leftPane.addChild(pPane11);
            var pPane12 = createInputPane("bottom");
            rightPane.addChild(pPane12);
            var pPane21 = createInputPane("bottom");
            leftPane.addChild(pPane21);
            var pPane22 = createInputPane("top");
            rightPane.addChild(pPane22);
            topContainer.addChild(leftPane);
            topContainer.addChild(rightPane);
            var iconPane = new ContentPane({
                "class": "geometriaIconIntersection",
                region: "center"
            });
            topContainer.addChild(iconPane);
            container.addChild(topContainer);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_intersection",
                dict.get("IntersectLines"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase(),
                                 pInputs[2].get("value").trim().toUpperCase(),
                                 pInputs[3].get("value").trim().toUpperCase()]
                    };
                    var results = results || [{}, {}, {}, {}];
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
                pInputs[2].set("value", selectionProps.points[2]);
                pInputs[3].set("value", selectionProps.points[3]);
            }
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    points: [pInputs[0].get("value").trim().toUpperCase(),
                             pInputs[1].get("value").trim().toUpperCase(),
                             pInputs[2].get("value").trim().toUpperCase(),
                             pInputs[3].get("value").trim().toUpperCase()]
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
            var faces = lang.mixin(solid.facesThroughPoints([props.points[0], props.points[1]]),
                solid.facesThroughPoints([props.points[2], props.points[3]]));
            var label = props.addedPoint;
            $.each(faces, function() {
                this.removePoint(label);
            });
            delete solid.points[label];
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
            return dict.get("IntersectLinesInFigure", props.points[0],
                props.points[1], props.points[2], props.points[3], props.figureName);
        },

        toJson: function(props) {
            return {
                "action": "intersectionAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points
                }
            };
        }
    };
});
