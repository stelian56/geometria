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

    var helpTopic = "DivideAngle";

    var validatePoints = function(props, results) {
        results = results || { points: [{}, {}, {}], ratio: [{}, {}] };
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
        if (props.points[0] == props.points[1] || props.points[0] == props.points[2] ||
                props.points[1] == props.points[2]) {
            return false;
        }
        var faces = solid.facesThroughPoints(props.points);
        if (!utils.anyProp(faces)) {
            return false;
        }
        var lines = solid.linesThroughPoints(props.points[0], props.points[1]);
        var line1 = utils.anyProp(lines);
        if (!line1) {
            return false;
        }
        lines = solid.linesThroughPoints(props.points[1], props.points[2]);
        var line2 = utils.anyProp(lines);
        if (!line2) {
            return false;
        }
        return true;
    };

    var getDivisionVector = function(props) {
        var scope = notepadContainer.getScope();
        var ratio = [];
        $.each([0, 1], function(index) {
            ratio[index] = utils.eval(props.ratio[index], scope);
        });
        var solid = figuresContainer.getFigure(props.figureName).solid;
        var ps = [];
        $.each(props.points, function(index, label) {
            var p = solid.points[label];
            ps.push(p);
        });
        var v = math.divideAngle(ps[1].crds, ps[0].crds, ps[2].crds, ratio[0]/ratio[1]);
        return v;
    };
    
    var validate = function(props, results) {
        results = results || { points: [{}, {}, {}], ratio: [{}, {}] };
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
            var solid = figuresContainer.getFigure(props.figureName).solid;
            var faces = solid.facesThroughPoints(props.points);
            var face = utils.anyProp(faces);
            var ps = [];
            $.each(props.points, function(index, label) {
                var p = solid.points[label];
                ps.push(p);
            });
            var v = getDivisionVector(props);
            var lines = face.linesThroughPoint(props.points[1]);
            var valid = true;
            $.each(lines, function() {
                var line = this;
                var v1 = vec3.clone(solid.points[line.firstLabel()].crds);
                vec3.sub(v1, v1, ps[1].crds);
                var v2 = vec3.clone(solid.points[line.lastLabel()].crds);
                vec3.sub(v2, v2, ps[1].crds);
                var refLength = solid.getRefLength();
                if (line.firstLabel() != props.points[1] && math.areCooriented(v1, v, refLength)
                        || line.lastLabel() != props.points[1] &&
                        math.areCooriented(v2, v, refLength)) {
                    valid = false;
                    return false;
                }
            });
            return valid;
        }
        return false;
    };
    
    var validateExternal = function(props) {
        if (!Array.isArray(props.points) || props.points.length != 3 || !props.figureName) {
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
        if (selectedElements.length == 2) {
            if (!(selectedElements[0] instanceof GPoint ||
                    selectedElements[0] instanceof GFace ||
                    selectedElements[1] instanceof GPoint ||
                    selectedElements[1] instanceof GFace)) {
                var labels;
                var labels1 = [selectedElements[0].p1.label, selectedElements[0].p2.label];
                var labels2 = [selectedElements[1].p1.label, selectedElements[1].p2.label];
                if (labels1[0] == labels2[0]) {
                    labels = [labels1[1], labels1[0], labels2[1]];
                }
                else if (labels1[0] == labels2[1]) {
                    labels = [labels1[1], labels1[0], labels2[0]];
                }
                else if (labels1[1] == labels2[0]) {
                    labels = [labels1[0], labels1[1], labels2[1]];
                }
                else if (labels1[1] == labels2[1]) {
                    labels = [labels1[0], labels1[1], labels2[0]];
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
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var faces = solid.facesThroughPoints(props.points);
        var face = utils.anyProp(faces);
        var ps = [];
        $.each(props.points, function(index, label) {
            var p = solid.points[label];
            ps.push(p);
        });
        var v = getDivisionVector(props);
        var result = face.intersectRay(ps[1].crds, v, solid);
        var line = result.line;
        var crds = result.point;
        var p = solid.pointAt(crds);
        var label;
        if (!p) {
            p = solid.addPoint(crds);
            label = p.label;
            $.each([line, line.twin], function() {
                this.face.addPoint(p, solid);
            });
        }
        var removedLines = [];
        var addedLine = face.addLine(ps[1].label, p.label, removedLines, solid);
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        var outProps = lang.mixin({
            face: face.getFaceCode(),
            addedPoint: label,
            addedLine: addedLine.getStickCode(),
            removedLines: removedLines
        }, props);
        return outProps;
    };

    return {

        loggable: true,
        
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24DivideAngle",
        
        label: dict.get("action.DivideAngle"),
        
        execute: function(contextMenuTriggered, bisector) {
            var selectionProps = validateSelection();
            if (contextMenuTriggered && selectionProps && bisector) {
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
            var pPane1 = createInputPane("top");
            rightPane.addChild(pPane1);
            leftPane.addChild(new ContentPane({
                "style": "height:10%",
                region: "top"
            }));
            var pPane2 = createInputPane("center");
            leftPane.addChild(pPane2);
            leftPane.addChild(new ContentPane({
                "style": "height:10%",
                region: "bottom"
            }));
            var pPane3 = createInputPane("bottom");
            rightPane.addChild(pPane3);
            topContainer.addChild(leftPane);
            topContainer.addChild(rightPane);
            var iconPane = new ContentPane({
                "class": "geometriaIconDivideAngle",
                region: "center"
            });
            topContainer.addChild(iconPane);
            container.addChild(topContainer);
            if (selectionProps) {
                pInputs[0].set("value", selectionProps.points[0]);
                pInputs[1].set("value", selectionProps.points[1]);
                pInputs[2].set("value", selectionProps.points[2]);
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
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_divideangle",
                dict.get("DivideAngle"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase(),
                                 pInputs[2].get("value").trim().toUpperCase()],
                        ratio: [ratioInputs[0].get("value").trim(),
                                 ratioInputs[1].get("value").trim()]
                    };
                    var results = { points: [{}, {}, {}], ratio: [{}, {}] };
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
                                 pInputs[1].get("value").trim().toUpperCase(),
                                 pInputs[2].get("value").trim().toUpperCase()],
                        ratio: [ratioInputs[0].get("value").trim(),
                                 ratioInputs[1].get("value").trim()]
                    };
                    var results = { points: [{}, {}, {}], ratio: [{}, {}] };
                    var valid = validate(inputProps, results);
                    if (results.ratio[inputIndex].error) {
                        this.invalidMessage = results.ratio[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results.ratio[inputIndex].valid ||
                        !inputProps.ratio[inputIndex].length;
                });
            });
            if (bisector) {
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
                             pInputs[1].get("value").trim().toUpperCase(),
                             pInputs[2].get("value").trim().toUpperCase()],
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
            var face = solid.getFace(props.face);
            face.undoAddLine(props.addedLine, props.removedLines);
            if (props.addedPoint) {
                var faces = solid.facesThroughPoints([props.addedPoint]);
                $.each(faces, function() {
                    this.removePoint(props.addedPoint);
                });
                delete solid.points[props.addedPoint];
            }
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
            return dict.get("DivideAngleInRatio", props.points[0], props.points[1],
                props.points[2], props.ratio[0], props.ratio[1], props.figureName);
        },
        
        toJson: function(props) {
            return {
                "action": "divideAngleAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points,
                    "ratio": props.ratio
                }
            };
        }
    };
});