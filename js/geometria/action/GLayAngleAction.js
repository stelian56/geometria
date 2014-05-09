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
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict, GFace,
            figuresContainer, mainContainer, math, notepadContainer, GPoint, utils, widgets) {

    var helpTopic = "LayAngle";

    var validatePoints = function(props, results) {
        results = results || { points: [{}, {}], angle: {} };
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

    var addLines = function(props, doApply) {
        var outProps = lang.mixin({}, props);
        outProps.faces = [];
        var solid = figuresContainer.getFigure(props.figureName).solid;
        var ps = [];
        $.each(props.points, function(index, label) {
            var p = solid.points[label];
            ps.push(p);
        });
        var scope = notepadContainer.getScope();
        var angle = utils.eval(props.angle, scope);
        var faces = solid.facesThroughPoints([ps[0].label, ps[1].label]);
        $.each(faces, function(faceCode, face) {
            var faceProps;
            var addedLabels = [];
            var addedLines = [];
            removedLines = [];
            var v = vec3.clone(ps[1].crds);
            vec3.sub(v, v, ps[0].crds);
            var n = face.getNormal(solid);
            var vs = math.layAngle(v, n, angle);
            $.each(vs, function() {
                var result = face.intersectRay(ps[0].crds, this, solid);
                if (result) {
                    var line = result.line;;
                    var crds = result.point;
                    var p = solid.pointAt(crds);
                    if (p && face.lineThroughPoints(ps[0].label, p.label)) {
                        return;
                    }
                    if (!faceProps) {
                        faceProps = {
                            face: face.getFaceCode()
                        };
                    }
                    if (doApply) {
                        if (!p) {
                            p = solid.addPoint(crds);
                            label = p.label;
                            addedLabels.push(label);
                            $.each([line.face, line.twin.face], function() {
                                this.addPoint(p, solid);
                            });
                        }
                        var addedLine = face.addLine(ps[0].label, p.label, removedLines, solid);
                        addedLines.push(addedLine.getStickCode());
                    }
                }
            });
            if (faceProps) {
                lang.mixin(faceProps, {
                    addedPoints: addedLabels,
                    addedLines: addedLines,
                    removedLines: removedLines
                });
                outProps.faces.push(faceProps);
            }
        });
        return outProps.faces.length > 0 ? outProps : null;
    };
    
    var validate = function(props, results) {
        var solid = figuresContainer.getFigure(props.figureName).solid;
        results = results || { points: [{}, {}], angle: {} };
        var pointsValid = validatePoints(props, results);
        var scope = notepadContainer.getScope();
        var angle = utils.eval(props.angle, scope);
        if (isNaN(angle)) {
            results.angle.error = dict.get("EnterValidExpression");
        }
        else if (angle <= 0 || angle >= Math.PI) {
            results.angle.error = dict.get("EnterAngleBetween0AndPi");
        }
        else {
            results.angle.valid = true;
        }
        if (pointsValid && results.angle.valid) {
            return addLines(props, false) && true;
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
            var labels;
            if (stick.p1.label == p.label) {
                labels = [stick.p1.label, stick.p2.label];
            }
            else if (stick.p2.label == p.label) {
                labels = [stick.p2.label, stick.p1.label];
            }
            if (labels) {
                var props = { 
                    figureName: figure.name,
                    points: labels
                };
                if (validatePoints(props)) {
                    return props;
                }
            }
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var outProps = addLines(props, true);
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        return outProps;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24LayAngle",

        label: dict.get("action.LayAngle"),
        
        execute: function() {
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
                "class": "geometriaIconLayAngle",
                region: "center"
            });
            topContainer.addChild(iconPane);
            container.addChild(topContainer);
            if (selectionProps) {
                $.each([0, 1], function(index) {
                    pInputs[this].set("value", selectionProps.points[index]);
                });
            }
            var angleInputDeferred = new Deferred();
            var angleInput = widgets.validationTextBox({
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        angleInputDeferred.resolve();
                    }
                }
            });
            var bottomContainer = new LayoutContainer({
                region: "bottom",
                style: "height:40px;"
            });
            var labelPane = new ContentPane({
                region: "left",
                content: dict.get("Angle")
            });
            bottomContainer.addChild(labelPane);
            var anglePane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: angleInput
            });
            inputEnters.push(angleInputDeferred.promise);
            bottomContainer.addChild(anglePane);
            container.addChild(bottomContainer);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_layangle",
                dict.get("LayAngle"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        angle: angleInput.get("value").trim(),
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()]
                    };
                    var results = { points: [{}, {}], angle: {} };
                    var valid = validate(inputProps, results);
                    if (results.points[inputIndex].error) {
                        this.invalidMessage = results.points[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results.points[inputIndex].valid ||
                        !inputProps.points[inputIndex].length;
                });
            });
            angleInput.set("validator", function() {
                var inputProps = {
                        figureName: figure.name,
                        angle: angleInput.get("value").trim(),
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase()]
                };
                var results = { points: [{}, {}], angle: {} };
                var valid = validate(inputProps, results);
                if (results.angle.error) {
                    this.invalidMessage = results.angle.error;
                }
                dialog.okButton.set("disabled", !valid);
                return results.angle.valid || !inputProps.angle.length;
            });
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    angle: angleInput.get("value").trim(),
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
            $.each(props.faces, function() {
                var faceProps = this;
                var face = solid.getFace(faceProps.face);
                face.undoAddLines(faceProps.addedLines, faceProps.removedLines);
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
            return dict.get("LayAngleInFigure", props.angle, props.points[0],
                props.points[1], props.figureName);
        },

        toJson: function(props) {
            return {
                "action": "layAngleAction",
                "props": {
                    "figureName": props.figureName,
                    "angle": props.angle,
                    "points": props.points
                }
            };
        }
    };
});
