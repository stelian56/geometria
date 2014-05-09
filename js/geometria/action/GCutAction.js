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
    "geometria/GFigure",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GMath",
    "geometria/GPoint",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict,
            GFace, GFigure, figuresContainer, mainContainer, math, GPoint, utils, widgets) {

    var helpTopic = "CutFigure";

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
        var faces = solid.facesThroughPoints(props.points);
        if (utils.anyProp(faces)) {
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
        return validate(props);
    };
    
    var validateSelection = function() {
        var figure = figuresContainer.getSelectedFigure();
        var solid = figure.solid;
        var ps = {};
        $.each(solid.selection, function(code, element) {
            if (element instanceof GFace) {
                ps = {};
                return false;
            }
            if (element instanceof GPoint) {
                ps[element.label] = element;
            }
            else {
                ps[element.p1.label] = element.p1;
                ps[element.p2.label] = element.p2;
            }
        });
        var crds = [];
        var labels = [];
        var refLength = solid.getRefLength();
        var n;
        $.each(ps, function(label, p) {
            if (labels.length < 2) {
                crds.push(p.crds);
                labels.push(label);
            }
            else {
                if (!math.areCollinearPoints([crds[0], crds[1], p.crds], refLength)) {
                    if (labels.length < 3) {
                        crds.push(p.crds);
                        labels.push(label);
                        var v1 = vec3.sub([], crds[1], crds[0]);
                        var v2 = vec3.sub([], crds[2], crds[0]);
                        n = vec3.cross([], v1, v2);
                        vec3.normalize(n, n);
                    }
                    else {
                        var v = vec3.sub([], p.crds, crds[0]);
                        if (Math.abs(vec3.dot(v, n)) > vec3.length(v)*math.EPSILON) {
                            labels = [];
                            return false;
                        }
                    }
                }
            }
        });
        if (labels.length > 0) {
            var props = { 
                figureName: figure.name,
                points: labels
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
        var ps = [];
        $.each(props.points, function(index, label) {
            ps.push(solid.points[label]);
        });
        var v1 = vec3.create();
        vec3.sub(v1, ps[1].crds, ps[0].crds);
        var v2 = vec3.create();
        vec3.sub(v2, ps[2].crds, ps[0].crds);
        var n = vec3.cross([], v1, v2);
        vec3.normalize(n, n);
        if (n[2] < - math.EPSILON) {
            vec3.scale(n, n, -1);
        }
        else if (n[2] < math.EPSILON) {
            if (n[1] < -math.EPSILON) {
                vec3.scale(n, n, -1);
            }
            else if (n[1] < math.EPSILON) {
                if (n[0] < -math.EPSILON) {
                    vec3.scale(n, n, -1);
                }
            }
        }
        var radius = solid.boundingSphere.radius;
        var childSolids = [];
        var childFigures = [];
        $.each([0, 1], function(index) {
            childSolid = solid.clone();
            childSolids.push(childSolid);
            var crds = solid.points[props.points[0]].crds;
            childSolid.cutOff(crds, n);
            var figureName = utils.getNewFigureName();
            var childFigure = new GFigure(figureName, childSolid);
            childFigure.wireframe = figure.wireframe;
            childFigure.labeled = figure.labeled;
            var camera = figure.camera;
            childFigure.camera.attitude = mat3.clone(camera.attitude);
            childFigure.camera.initialAttitude = mat3.clone(camera.initialAttitude);
            childFigure.baseColor = figure.baseColor;
            figuresContainer.addFigure(childFigure);
            var zoomFactor = childSolid.boundingSphere.radius/radius;
            childFigure.zoom(zoomFactor);
            childFigures.push(childFigure);
            vec3.scale(n, n, -1);
        });
        $.each(childSolids, function() {
            var childSolid = this;
            $.each(solid.relatedSolids, function() {
                this.relatedSolids[childSolid.id] = childSolid;
                childSolid.relatedSolids[this.id] = this;
            });
        });
        solid.selection = {};
        figure.draw();
        var outProps = lang.mixin({
            figure1Name: childFigures[0].name,
            figure2Name: childFigures[1].name
        }, props);
        return outProps;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24Cut",

        label: dict.get("action.Cut"),
        
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
            var container = new LayoutContainer();
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
            container.addChild(leftPane);
            container.addChild(rightPane);
            var iconPane = new ContentPane({
                "class": "geometriaIconCut",
                region: "center"
            });
            container.addChild(iconPane);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_cut",
                dict.get("CutFigure"), inputEnters);
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
            figuresContainer.removeFigure(props.figure1Name);
            figuresContainer.removeFigure(props.figure2Name);
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
            return dict.get("CutFigureThroughPoints", props.figureName, props.points[0],
                props.points[1], props.points[2]);
        },

        toJson: function(props) {
            return {
                "action": "cutAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points
                }
            };
        }
    };
});
