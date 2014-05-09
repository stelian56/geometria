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

    var helpTopic = "DrawPerpendicular";

    var validate = function(props, results) {
        results = results || [{}, {}, {}];
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
        if (props.points[1] == props.points[2]) {
            return false;
        }
        var faces = solid.facesThroughPoints([props.points[0], props.points[1], props.points[2]]);
        if (!utils.anyProp(faces)) {
            return false;
        }
        var lines = solid.linesThroughPoints(props.points[1], props.points[2]);
        var line = utils.anyProp(lines);
        if (!line) {
            return false;
        }
        var ps = [];
        $.each(props.points, function(index, label) {
            var p = solid.points[label];
            ps.push(p);
        });
        if (line.contains(ps[0].label)) {
            // Raised perpendicular
            var valid = false;
            $.each(faces, function() {
                var face = this;
                var v = vec3.clone(ps[2].crds);
                vec3.sub(v, v, ps[1].crds);
                var iPoints = face.intersectPlane(ps[0].crds, v, solid);
                if (iPoints.length < 2) {
                    // Perpendicular is entirely outside the figure
                    return;
                }
                var p1 = solid.pointAt(iPoints[0]);
                var p2 = solid.pointAt(iPoints[1]);
                if (p1 && p2 && face.lineThroughPoints(p1.label, p2.label)) {
                    // Perpendicular already drawn
                    return;
                }
                valid = true;
                return false;
            });
            return valid;
        }
        else {
            // Dropped perpendicular
            var face = utils.anyProp(faces);
            var pr0 = math.project(ps[0].crds, ps[1].crds, ps[2].crds);
            var v = vec3.clone(pr0);
            vec3.sub(v, v, ps[0].crds);
            var result = face.intersectRay(ps[0].crds, v, solid);
            if (!result) {
                // Perpendicular is entirely outside the figure
                return false;
            }
            var crds = result.point;
            var p = solid.pointAt(crds);
            if (p) {
                var lines = solid.linesThroughPoints(p.label, ps[0].label);
                if (utils.anyProp(lines)) {
                    // Perpendicular already drawn
                    return false;
                }
            }
            return true;
        }
    };

    var validateExternal = function(props) {
        if (!Array.isArray(props.points) || props.points.length != 3 || !props.figureName) {
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
        var p, stick;
        if (selectedElements.length == 2) {
            if (selectedElements[0] instanceof GPoint &&
                    (!(selectedElements[1] instanceof GPoint) &&
                    !(selectedElements[1] instanceof GFace))) {
                p = selectedElements[0];
                stick = selectedElements[1];
            }
            else if (selectedElements[1] instanceof GPoint &&
                    (!(selectedElements[0] instanceof GPoint) &&
                    !(selectedElements[0] instanceof GFace))) {
                p = selectedElements[1];
                stick = selectedElements[0];
            }
        }
        if (p) {
            var props = { 
                figureName: figure.name,
                points: [p.label, stick.p1.label, stick.p2.label]
            };
            if (validate(props)) {
                return props;
            }
        }
        return null;
    };

    var apply = function(props) {
        var outProps = lang.mixin({}, props);
        outProps.faces = [];
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var faces = solid.facesThroughPoints([props.points[0], props.points[1], props.points[2]]);
        var lines = solid.linesThroughPoints(props.points[1], props.points[2]);
        var line = utils.anyProp(lines);
        var ps = [];
        $.each(props.points, function(index, label) {
            var p = solid.points[label];
            ps.push(p);
        });
        if (line.contains(ps[0].label)) {
            // Raised perpendicular
            $.each(faces, function() {
                var face = this;
                var v = vec3.clone(ps[2].crds);
                vec3.sub(v, v, ps[1].crds);
                var iPoints = face.intersectPlane(ps[0].crds, v, solid);
                if (iPoints.length < 2) {
                    // Perpendicular is entirely outside the figure
                    return;
                }
                var p1 = solid.pointAt(iPoints[0]);
                var p2 = solid.pointAt(iPoints[1]);
                if (p1 && p2 && face.lineThroughPoints(p1.label, p2.label)) {
                    // Perpendicular already drawn
                    return;
                }
                var addedPoints = [];
                var addedLabels = [];
                var label, crds;
                if (!p1) {
                    crds = iPoints[0];
                    p1 = solid.addPoint(crds);
                    label = p1.label;
                    addedPoints.push(p1);
                    addedLabels.push(label);
                }
                if (!p2) {
                    crds = iPoints[1];
                    p2 = solid.addPoint(crds);
                    label = p2.label;
                    addedPoints.push(p2);
                    addedLabels.push(label);
                }
                $.each(addedPoints, function() {
                    var p = this;
                    for (var lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                        var line = face.lines[lineIndex];
                        var pp1 = solid.points[line.firstLabel()];
                        var pp2 = solid.points[line.lastLabel()];
                        if (math.isBetween(p.crds, pp1.crds, pp2.crds, solid.getRefLength())) {
                            $.each([line, line.twin], function() {
                                this.face.addPoint(p, solid);
                            });
                            break;
                        }
                    }
                });
                removedLines = [];
                var addedLine = face.addLine(p1.label, p2.label, removedLines, solid);
                var faceProps = {
                    face: face.getFaceCode(),
                    addedPoints: addedLabels,
                    addedLine: addedLine.getStickCode(),
                    removedLines: removedLines
                };
                outProps.faces.push(faceProps);
            });
        }
        else {
            // Dropped perpendicular
            var face = utils.anyProp(faces);
            var pr0 = math.project(ps[0].crds, ps[1].crds, ps[2].crds);
            var p = solid.pointAt(pr0);
            var v = vec3.clone(pr0);
            vec3.sub(v, v, ps[0].crds);
            var result = face.intersectRay(ps[0].crds, v, solid);
            var crds = result.point;
            p = solid.pointAt(crds);
            var addedLabels = [];
            if (p == null) {
                p = solid.addPoint(crds);
                label = p.label;
                var line = result.line;
                $.each([line.face, line.twin.face], function() {
                    this.addPoint(p, solid);
                });
                addedLabels.push(label);
            }
            removedLines = [];
            var addedLine = face.addLine(ps[0].label, p.label, removedLines, solid);
            var faceProps = {
                face: face.getFaceCode(),
                addedPoints: addedLabels,
                addedLine: addedLine.getStickCode(),
                removedLines: removedLines
            };
            outProps.faces.push(faceProps);
        }
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        return outProps;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24Perpendicular",

        label: dict.get("action.Perpendicular"),
        
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

            var container = new LayoutContainer();
            var topContainer = new LayoutContainer({
                region: "top",
                style: "height: 40px"
            });
            topContainer.addChild(new ContentPane({
                region: "left",
                style: "width:93px;"
            }));
            topContainer.addChild(new ContentPane({
                region: "right",
                style: "width:93px;"
            }));
            var pPane1 = createInputPane("center");
            topContainer.addChild(pPane1);
            container.addChild(topContainer);
            var bottomContainer = new LayoutContainer({
                region: "center"
            });
            var leftPane = new LayoutContainer({
                "class": "geometria_inputcontainer",
                region: "left"
            });
            var rightPane = new LayoutContainer({
                "class": "geometria_inputcontainer",
                region: "right"
            });
            var iconPane = new ContentPane({
                "class": "geometriaIconPerpendicular",
                region: "center"
            });
            var pPane2 = createInputPane("bottom");
            leftPane.addChild(pPane2);
            var pPane3 = createInputPane("bottom");
            rightPane.addChild(pPane3);
            bottomContainer.addChild(leftPane);
            bottomContainer.addChild(rightPane);
            bottomContainer.addChild(iconPane);
            container.addChild(bottomContainer);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_perpendicular",
                dict.get("DrawPerpendicular"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase(),
                                 pInputs[2].get("value").trim().toUpperCase()]
                    };
                    var results = [{}, {}, {}];
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
                pInputs[1].set("value", selectionProps.points[1]),
                pInputs[2].set("value", selectionProps.points[2]);
            }
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    points: [pInputs[0].get("value").trim().toUpperCase(),
                             pInputs[1].get("value").trim().toUpperCase(),
                             pInputs[2].get("value").trim().toUpperCase()]
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
            $.each(props.faces, function() {
                var faceProps = this;
                var face = solid.getFace(faceProps.face);
                face.undoAddLine(faceProps.addedLine, faceProps.removedLines);
                $.each(faceProps.addedPoints, function(index, label) {
                    var faces = solid.facesThroughPoints([label]);
                    $.each(faces, function() {
                        this.removePoint(label);
                    });
                    delete solid.points[label];
                });
            });
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
            var key = props.faces.length < 2 ? "PerpendicularInFigure" : "PerpendicularsInFigure";
            return dict.get(key, props.points[1], props.points[2], props.points[0],
                props.figureName);
        },

        toJson: function(props) {
            return {
                "action": "perpendicularAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points,
                    "faces": props.faces
                }
            };
        }
    };
});
