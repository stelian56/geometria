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
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GDictionary",
    "geometria/GFace",
    "geometria/GFigure",
    "geometria/GFiguresContainer",
    "geometria/GLine",
    "geometria/GMainContainer",
    "geometria/GMath",
    "geometria/GPoint",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, Deferred, ContentPane, LayoutContainer, dict, GFace, GFigure, figuresContainer,
            GLine, mainContainer, math, GPoint, utils, widgets) {

    var helpTopic = "JoinFigures";

    var matchFaces = function(solid1, solid2, face1, face2, index1, matchSelectedElements) {
        var sideCount = face1.sideCount;
        var scaleFactor;
        var p1Prev, p1Next, p2Prev, p2Next, v1Prev, v2Prev, length1Prev, length2Prev;
        for (var lineIndex = 0; lineIndex < sideCount; lineIndex++) {
            var line1 = face1.lines[(index1 + lineIndex)%sideCount];
            var line2 = face2.lines[sideCount - lineIndex - 1];
            if (p1Next) {
                p1 = p1Next;
                p2 = p2Next;
            }
            else {
                p1 = solid1.points[line1.firstLabel()];
                p2 = solid2.points[line2.lastLabel()];
            }
            if (matchSelectedElements) {
                if (p1.label in solid1.selection != p2.label in solid2.selection) {
                    return false;
                }
                if (line1.getStickCode() in solid1.selection !=
                        line2.getStickCode() in solid2.selection) {
                    return false;
                }
            }
            var line1Next = face1.lines[(index1 + lineIndex + 1)%sideCount];
            var line2Next = face2.lines[(2*sideCount - lineIndex - 2)%sideCount];
            p1Next = solid1.points[line1Next.firstLabel()];
            p2Next = solid2.points[line2Next.lastLabel()];
            var v1 = vec3.sub([], p1Next.crds, p1.crds);
            var v2 = vec3.sub([], p2Next.crds, p2.crds);
            var length1 = vec3.length(v1);
            var length2 = vec3.length(v2);
            if (!scaleFactor) {
                scaleFactor = length2/length1;
            }
            else {
                if (Math.abs(length2/length1 - scaleFactor) > math.EPSILON) {
                    return false;
                }
                var cos1 = vec3.dot(v1, v1Prev)/(length1*length1Prev);
                var cos2 = vec3.dot(v2, v2Prev)/(length2*length2Prev);
                if (Math.abs(cos1 - cos2) > math.EPSILON) {
                    return false;
                }
            }
            p1Prev = p1;
            p2Prev = p2;
            v1Prev = v1;
            v2Prev = v2;
            length1Prev = length1;
            length2Prev = length2;
        }
        return true;
    };
            
    var matchSolids = function(solid1, solid2, labels1, labels2) {
        var faces1 = solid1.facesThroughPoints(labels1);
        var face1 = utils.anyProp(faces1);
        var ps1 = [], ps2 = [];
        $.each([0, 1, 2], function(index) {
            ps1.push(solid1.points[labels1[index]]);
            ps2.push(solid2.points[labels2[index]]);
        });
        var v11 = vec3.sub([], ps1[2].crds, ps1[1].crds);
        var v12 = vec3.sub([], ps1[0].crds, ps1[1].crds);
        var v21 = vec3.sub([], ps2[2].crds, ps2[1].crds);
        var v22 = vec3.sub([], ps2[0].crds, ps2[1].crds);
        // Scale solid2
        var scaleFactor = vec3.length(v11)/vec3.length(v21);
        $.each(solid2.points, function(label, p) {
            vec3.scale(p.crds, p.crds, scaleFactor);
        });
        // Compute rotation matrix for solid2
        math.orthize(v11, v12);
        var v1 = vec3.cross([], v11, v12);
        var m1 = mat3.clone([v11[0], v11[1], v11[2], v12[0], v12[1], v12[2],
            v1[0], v1[1], v1[2]]);
        math.orthize(v21, v22);
        var v2 = vec3.cross([], v21, v22);
        var m2 = mat3.clone([v21[0], v21[1], v21[2], v22[0], v22[1], v22[2],
            v2[0], v2[1], v2[2]]);
        mat3.invert(m2, m2);
        mat3.mul(m1, m1, m2);
        // Rotate solid2
        var pivot = solid2.points[labels2[1]].crds;
        var v = vec3.create();
        $.each(solid2.points, function(label, p) {
            vec3.sub(v, p.crds, pivot);
            vec3.transformMat3(v, v, m1);
            vec3.add(p.crds, pivot, v);
        });
        // Translate solid2
        vec3.sub(v, solid1.points[labels1[1]].crds, solid2.points[labels2[1]].crds);
        $.each(solid2.points, function(label, p) {
            vec3.add(p.crds, p.crds, v);
        });
        solid2.computeGCenter();
        // Check convexity of the joint solid
        var epsilon = solid1.getRefLength()*math.EPSILON;
        var valid = true;
        for (var lineIndex = 0; lineIndex < face1.sideCount; lineIndex++) {
            var f = face1.lines[lineIndex].twin.face;
            var outerNormal = f.getNormal(solid1);
            var p1 = solid1.points[f.labelAt(0)].crds;
            $.each(solid2.points, function(label, p2) {
                vec3.sub(v, p2.crds, p1);
                if (vec3.dot(v, outerNormal) > epsilon) {
                    valid = false;
                    return false;
                }
            });
            if (!valid) {
                return false;
            }
        }
        return valid;
    };
    
    var validateInput = function(inputProps) {
        var solid1 = figuresContainer.getFigure(inputProps.figure1.name).solid;
        var solid2 = figuresContainer.getFigure(inputProps.figure2.name).solid;
        var solid2Clone = solid2.clone();
        var faces1, faces2;
        if (inputProps.joinArbitrarily) {
            faces1 = solid1.faces;
            faces2 = solid2.faces;
        }
        else {
            var selection1Props = inputProps.figure1.selection;
            var selection2Props = inputProps.figure2.selection;
            if (selection1Props.face && selection2Props.face) {
                faces1 = [selection1Props.face];
                faces2 = [selection2Props.face];
            }
            else if (!selection1Props.face && !selection2Props.face) {
                faces1 = selection1Props.facesThroughPoints || solid1.faces;
                faces2 = selection2Props.facesThroughPoints || solid2.faces;
            }
            else {
                return null;
            }
        }
        var outProps;

        var validateFaces = function(face1, face2) {
            if (face1.sideCount != face2.sideCount) {
                return null;
            }
            var sideCount = face1.sideCount;
            for (var index1 = 0; index1 < sideCount; index1++) {
                if (matchFaces(solid1, solid2, face1, face2, index1,
                        !inputProps.joinArbitrarily)) {
                    var labels1 = [], labels2 = [];
                    $.each([0, 1, 2], function(index) {
                        var label1 = face1.lines[(index1 + index)%sideCount].firstLabel();
                        labels1.push(label1);
                        var label2 = face2.lines[sideCount - index - 1].lastLabel();
                        labels2.push(label2);
                    });
                    $.each(solid2Clone.points, function(label, p) {
                        vec3.copy(p.crds, solid2.points[label].crds);
                    });
                    if (matchSolids(solid1, solid2Clone, labels1, labels2)) {
                        outProps = {
                            figure1: {
                                name: inputProps.figure1.name,
                                points: labels1
                            },
                            figure2: {
                                name: inputProps.figure2.name,
                                points: labels2
                            },
                            solid1: solid1,
                            solid2: solid2Clone,
                            index1: index1
                        };
                        return;
                    }
                }
            }
        };

        $.each(faces1, function() {
            var face1 = this;
            $.each(faces2, function() {
                var face2 = this;
                validateFaces(face1, face2, solid1, solid2);
                if (outProps) {
                    return false;
                }
            });
            if (outProps) {
                return false;
            }
        });
        return outProps;
    };
    
    var validateExternal = function(props) {
        if (!props.figure1 || !props.figure2) {
            return null;
        }
        var figure1 = figuresContainer.getFigure(props.figure1.name);
        if (!figure1) {
            return null;
        }
        var figure2 = figuresContainer.getFigure(props.figure2.name);
        if (!figure2) {
            return null;
        }
        var solid1 = figure1. solid;
        var solid2 = figure2.solid;
        var labels1 = props.figure1.points;
        var labels2 = props.figure2.points;
        if (!Array.isArray(labels1) || labels1.length != 3 ||
                !Array.isArray(labels2) || labels2.length != 3 ) {
            return null;
        }
        var faces1 = solid1.facesThroughPoints(labels1);
        var face1 = utils.anyProp(faces1);
        if (!face1) {
            return null;
        }
        var faces2 = solid2.facesThroughPoints(labels2);
        var face2 = utils.anyProp(faces2);
        if (!face2) {
            return null;
        }
        if (face1.sideCount != face2.sideCount) {
            return null;
        }
        var sideCount = face1.sideCount;
        if (face2.lines[sideCount - 1].lastLabel() != labels2[0] ||
                face2.lines[sideCount - 2].lastLabel() != labels2[1] ||
                face2.lines[sideCount - 3].lastLabel() != labels2[2]) {
            return null;
        }
        var index1, lineIndex;
        for (lineIndex = 0; lineIndex < sideCount; lineIndex++) {
            var label = face1.lines[lineIndex].firstLabel();
            if (label == labels1[0]) {
                if (face1.lines[(lineIndex + 1)%sideCount].firstLabel() != labels1[1] ||
                        face1.lines[(lineIndex + 2)%sideCount].firstLabel() != labels1[2]) {
                    return null;
                }
                index1 = lineIndex;
                break;
            }
        }
        if (!(index1 >= 0)) {
            return null;
        }
        var solid2Clone = solid2.clone();
        if (!matchFaces(solid1, solid2Clone, face1, face2, index1, false)) {
            return null;
        }
        if (matchSolids(solid1, solid2Clone, labels1, labels2)) {
            var outProps = lang.mixin({
                solid1: solid1,
                solid2: solid2Clone,
                index1: index1
            }, props);
            return outProps;
        }
        return null;
    };
    
    var getSelectionProps = function() {
        var selectionProps = {};
        $.each(mainContainer.currentDocument.figures, function() {
            var figure = this;
            var solid = figure.solid;
            var labels = [];
            var selectedPoints = [];
            var selectedSticks = [];
            var selectedFaces = [];
            $.each(solid.selection, function(code, element) {
                if (element instanceof GPoint) {
                    selectedPoints.push(element);
                    labels.push(element.label);
                }
                else if (element instanceof GFace) {
                    selectedFaces.push(element);
                }
                else {
                    selectedSticks.push(element);
                    labels.push(element.p1.label);
                    labels.push(element.p2.label);
                }
            });
            if (selectedFaces.length > 1) {
                return;
            }
            var facesThroughPoints;
            if (labels.length > 0) {
                facesThroughPoints = solid.facesThroughPoints(labels);
                if (!utils.anyProp(facesThroughPoints)) {
                    return;
                }
            }
            else {
                facesTroughPoints = solid.faces;
            }
            var figureProps = {};
            if (selectedFaces.length == 1) {
                var face = selectedFaces[0];
                var labelsInFace = true;
                $.each(labels, function(index, label) {
                    if (!face.contains(label)) {
                        labelsInFace = false;
                        return false;
                    }
                });
                if (!labelsInFace) {
                    return;
                }
                figureProps.face = face;
            }
            lang.mixin(figureProps, {
                points: selectedPoints,
                sticks: selectedSticks,
                facesThroughPoints: facesThroughPoints
            });
            selectionProps[figure.name] = figureProps;
        });
        return selectionProps;
    };

    var apply = function(props) {
        var figure1 = figuresContainer.getFigure(props.figure1.name);
        var figure2 = figuresContainer.getFigure(props.figure2.name);
        var solid1 = props.solid1.clone();
        var solid2 = props.solid2.clone();
        var fs1 = solid1.facesThroughPoints(props.figure1.points);
        var face1 = utils.anyProp(fs1);
        var fs2 = solid2.facesThroughPoints(props.figure2.points);
        var face2 = utils.anyProp(fs2);
        face1.startAt(props.index1);
        face2.reverse();
        var p2ToP1 = {};
        var faces2 = {};
        var newFaces1 = {};
        var lineIndex;
        for (lineIndex = 0; lineIndex < face1.sideCount; lineIndex++) {
            var line1 = face1.lines[lineIndex].twin;
            var line2 = face2.lines[lineIndex].twin;
            $.each(line2.labels, function(index, label) {
                var p2 = solid2.points[label];
                var p1 = line1.pointAt(p2.crds, solid1);
                if (!p1) {
                    p1 = solid1.addPoint(p2.crds);
                    line1.insert(p1, solid1);
                }
                p2ToP1[p2.label] = p1;
            });
            var f1 = line1.face;
            var f2 = line2.face;
            var index2 = f2.lines.indexOf(line2);
            f2.startAt(index2);
            var ls = [];
            var newL2 = new GLine(line1.labels);
            ls.push(newL2);
            var l2, labels;
            for (var l2Index = 1; l2Index < f2.lines.length; l2Index++) {
                l2 = f2.lines[l2Index];
                labels = [];
                $.each(l2.labels, function(index, label) {
                    var p2 = solid2.points[label];
                    var p1 = p2ToP1[label];
                    if (!p1) {
                        p1 = solid1.addPoint(p2.crds);
                        p2ToP1[label] = p1;
                    }
                    labels.push(p1.label);
                });
                newL2 = new GLine(labels);
                ls.push(newL2);
            }
            faces2[f2.code] = f2;
            var newFace1 = new GFace(f2.sideCount, ls);
            newFace1.chainSides();
            if (vec3.dot(f2.getNormal(solid2), f1.getNormal(solid1)) < 1 - math.EPSILON) {
                newFaces1[newFace1.code] = newFace1;
                continue;
            }
            // Merge f1 and newFace1 into nf1
            var index1 = f1.lines.indexOf(line1);
            f1.startAt(index1);
            if (f1.labelAt(0) == newFace1.labelAt(0)) {
                newFace1.reverse();
                newFace1.startAt(newFace1.sideCount - 1);
            }
            var sc1 = f1.sideCount;
            var sc2 = newFace1.sideCount;
            ls = [];
            var lIndex;
            for (lIndex = 1; lIndex < sc1; lIndex++) {
                var l = f1.lines[lIndex];
                ls.push(l);
            }
            for (lIndex = 1; lIndex < sc2; lIndex++) {
                var newL1 = newFace1.lines[lIndex];
                ls.push(newL1);
            }
            var lBegin = ls[0];
            var lEnd = ls[sc1 - 2];
            var newLBegin = ls[sc1 - 1];
            var newLEnd = ls[sc1 + sc2 - 3];
            if (lBegin.acquireLine(newLEnd, solid1)) {
                utils.remove(ls, newLEnd);
            }
            if (lEnd.acquireLine(newLBegin, solid1)) {
                utils.remove(ls, newLBegin);
            }
            var newSideCount = ls.length;
            var nf = new GFace(newSideCount, ls);
            nf.chainSides();
            var l;
            for (lIndex = sc1; lIndex < f1.lines.length; lIndex++) {
                l = f1.lines[lIndex];
                nf.lines.push(l);
            }
            for (lIndex = sc2; lIndex < newFace1.lines.length; lIndex++) {
                l = newFace1.lines[lIndex];
                for (var llIndex = sc1; llIndex < f1.lines.length; llIndex++) {
                    var ll = f1.lines[llIndex];
                    if (!ll.acquireLine(l, solid1)) {
                        nf.lines.push(l);
                    }
                }
            }
            nf.lines.push(line1);
            utils.remove(solid1.faces, f1);
            newFaces1[nf.code] = nf;
        }
        faces2[face2.code] = face2;
        $.each(solid2.faces, function() {
            var f2 = this;
            if (faces2[f2.code]) {
                return;
            }
            var ls = [];
            $.each(f2.lines, function() {
                var l2 = this;
                var labels = [];
                $.each(l2.labels, function(index, label) {
                    var p2 = solid2.points[label];
                    var p1 = p2ToP1[label];
                    if (!p1) {
                        p1 = solid1.addPoint(p2.crds);
                        p2ToP1[label] = p1;
                    }
                    labels.push(p1.label);
                });
                var newLine = new GLine(labels);
                ls.push(newLine);
            });
            faces2[f2.code] = f2;
            var newFace1 = new GFace(f2.sideCount, ls);
            newFace1.chainSides();
            newFaces1[newFace1.code] = newFace1;
        });
        utils.remove(solid1.faces, face1);
        var labels = {};
        for (lineIndex = 0; lineIndex < face1.sideCount; lineIndex++) {
            var line = face1.lines[lineIndex];
            $.each(line.labels, function(index, label) {
                labels[label] = true;
            });
        }
        for (lineIndex = face1.sideCount; lineIndex < face1.lines.length; lineIndex++) {
            var line = face1.lines[lineIndex];
            $.each(line.labels, function(index, label) {
                if (!labels[label]) {
                    delete solid1[label];
                }
            });
        }
        $.each(newFaces1, function() {
            solid1.faces.push(this);
        });
        solid1.makeConfig();
        solid1.computeGCenter();
        solid1.computeBoundingSphere();
        var figureName = utils.getNewFigureName();
        var jointFigure = new GFigure(figureName, solid1);
        jointFigure.wireframe = figure1.wireframe;
        jointFigure.labeled = figure1.labeled;
        var camera = figure1.camera;
        jointFigure.camera.attitude = mat3.clone(camera.attitude);
        jointFigure.camera.initialAttitude = mat3.clone(camera.initialAttitude);
        jointFigure.baseColor = figure1.baseColor;
        figuresContainer.addFigure(jointFigure);
        figure1.solid.selection = {};
        figure2.solid.selection = {}
        figure1.draw();
        figure2.draw();
        var outProps = lang.mixin({
            jointFigureName: figureName
        }, props);
        return outProps;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24JoinFigures",

        label: dict.get("action.JoinFigures"),
        
        execute: function() {
            var dialogDeferred = new Deferred();
            var figure = figuresContainer.getSelectedFigure();
            var options = [];
            $.each(mainContainer.currentDocument.figures, function() {
                options.push({ label: this.name, value: this.name });
            });
            var container = new LayoutContainer();
            var topContainer = new LayoutContainer({
                region: "center"
            });
            var faceInputs = [];
            var optionInputs = [];
            
            var getInputProps = function() {
                var selectionProps = getSelectionProps();
                var figure1Name = faceInputs[0].get("value");
                var figure2Name = faceInputs[1].get("value");
                var inputProps = {
                    figure1: {
                        name: figure1Name
                    },
                    figure2: {
                        name: figure2Name
                    }
                };
                if (optionInputs[0].get("value")) {
                    inputProps.joinArbitrarily = true;
                }
                else {
                    var selection1Props = selectionProps[figure1Name];
                    var selection2Props = selectionProps[figure2Name];
                    if (selection1Props && selection2Props) {
                        inputProps.figure1.selection = selection1Props;
                        inputProps.figure2.selection = selection2Props;
                    }
                    else {
                        return null;
                    }
                }
                return inputProps;
            };
            
            var validInputProps;
            $.each(["left", "right"], function(index, region) {
                var faceInput = widgets.select({
                    options: options,
                    value: figure.name,
                    onChange: function() {
                        var inputProps = getInputProps();
                        validInputProps = inputProps && validateInput(inputProps) || null;
                        dialog.okButton.set("disabled", !validInputProps);
                    }
                });
                faceInputs.push(faceInput);
                var fContainer = new LayoutContainer({
                    region: region,
                    style: "width:100px;"
                });
                $.each(["top", "bottom"], function(index, region) {
                    fContainer.addChild(new ContentPane({
                        region: region
                    }));
                });
                var fPane = new ContentPane({
                    "class": "geometria_inputpane",
                    region: "center",
                    content: faceInput
                });
                fContainer.addChild(fPane);
                topContainer.addChild(fContainer);
            });
            var iconPane = new ContentPane({
                "class": "geometriaIconJoinFigures",
                region: "center"
            });
            topContainer.addChild(iconPane);
            container.addChild(topContainer);
            bottomContainer = new LayoutContainer({
                region: "bottom",
                style: "height:60px;"
            });
            $.each([0, 1], function(index) {
                var button = widgets.radioButton({
                    region: "left",
                    name: "joinOption",
                    style: "height:10px;",
                    onChange: function(newValue) {
                        if (newValue) {
                            var inputProps = getInputProps();
                            validInputProps = inputProps && validateInput(inputProps) || null;
                            dialog.okButton.set("disabled", !validInputProps);
                        }
                    }
                });
                optionInputs.push(button);
            });
            optionInputs[0].set("checked", true);
            $.each([["top", dict.get("JoinArbitrarily")],
                    ["bottom", dict.get("JoinAtSelected")]], function(index) {
                var rContainer = new LayoutContainer({
                    region: this[0],
                    style: "height:50%;"
                });
                rContainer.addChild(new ContentPane({
                    "class": "geometria_radiobuttonpane",
                    region: "left",
                    content: optionInputs[index]
                }));
                rContainer.addChild(new ContentPane({
                    region: "center",
                    content: this[1],
                    style: "overflow:visible;"
                }));
                bottomContainer.addChild(rContainer);
            });
            container.addChild(bottomContainer);
            var dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_joinfigures",
                dict.get("JoinFigures"));
            dialog.ok.then(function() {
                var outProps = apply(validInputProps);
                mainContainer.setDocumentModified(true);
                dialogDeferred.resolve(outProps);
            });
            return dialogDeferred.promise;
        },

        matchFaces: matchFaces,
        
        matchSolids: matchSolids,
        
        undo: function(props) {
            var figure1 = figuresContainer.getFigure(props.figure1.name);
            var figure2 = figuresContainer.getFigure(props.figure2.name);
            figure1.solid.selection = {};
            figure2.solid.selection = {};
            figuresContainer.removeFigure(props.jointFigureName);
            figuresContainer.select(figure1.name);
        },

        playBack: function(props, external) {
            if (external) {
                var outProps = validateExternal(props);
                if (outProps) {
                    return apply(outProps);
                }
                else {
                    return null;
                }
            }
            return apply(props);
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },
        
        toLog: function(props) {
            return dict.get("JoinFiguresAtPoints", props.figure1.name, props.figure2.name,
                props.figure1.points[0], props.figure1.points[1], props.figure1.points[2],
                props.figure2.points[0], props.figure2.points[1], props.figure2.points[2]);
        },

        toJson: function(props) {
            return {
                "action": "joinFiguresAction",
                "props": {
                    "figure1": {
                        "name": props.figure1.name,
                        "points": props.figure1.points
                    },
                    "figure2": {
                        "name": props.figure2.name,
                        "points": props.figure2.points
                    }
                }
            };
        }
    };
});
