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

    var helpTopic = "LayDistance";

    var validatePoints = function(props, results) {
        results = results || { points: [{}, {}, {}], distance: {} };
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
        var lines = solid.linesThroughPoints(props.points[1], props.points[2]);
        if (!utils.anyProp(lines)) {
            return false;
        }
        return true;
    };

    var validate = function(props, results) {
        var solid = figuresContainer.getFigure(props.figureName).solid;
        results = results || { points: [{}, {}, {}], distance: {} };
        var pointsValid = validatePoints(props, results);
        var scope = notepadContainer.getScope();
        var distance = utils.eval(props.distance, scope);
        if (isNaN(distance)) {
            results.distance.error = dict.get("EnterValidExpression");
        }
        else if (distance <= 0) {
            results.distance.error = dict.get("EnterPositiveExpression");
        }
        else {
            results.distance.valid = true;
        }
        if (pointsValid && results.distance.valid) {
            var ps = [];
            $.each(props.points, function(index, label) {
                var p = solid.points[label];
                ps.push(p);
            });
            var crdsArray = math.intersectSphere(ps[1].crds, ps[2].crds, ps[0].crds, distance);
            if (crdsArray.length < 1) {
                // No point on segment at this distance
                return false;
            }
            var valid = false;
            $.each(crdsArray, function(index, crds) {
                valid |= !solid.pointAt(crds);
            });
            return valid;
        }
        return false;
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
            if (validatePoints(props)) {
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
        var scope = notepadContainer.getScope();
        var distance = utils.eval(props.distance, scope);
        var crdsArray = math.intersectSphere(ps[1].crds, ps[2].crds, ps[0].crds, distance);
        var faces = solid.facesThroughPoints([props.points[1], props.points[2]]);
        var addedLabels = []
        $.each(crdsArray, function(index, crds) {
            if (!solid.pointAt(crds)) {
                var p = solid.addPoint(crds);
                label = p.label;
                addedLabels.push(label);
                $.each(faces, function() {
                    this.addPoint(p, solid);
                });
            }
        });
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        var outProps = lang.mixin({
            addedPoints: addedLabels
        }, props);
        return outProps;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24LayDistance",

        label: dict.get("action.LayDistance"),
        
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
            var pPane1 = createInputPane("top");
            rightPane.addChild(pPane1);
            var pPane2 = createInputPane("bottom");
            leftPane.addChild(pPane2);
            var pPane3 = createInputPane("bottom");
            rightPane.addChild(pPane3);
            topContainer.addChild(leftPane);
            topContainer.addChild(rightPane);
            var iconPane = new ContentPane({
                "class": "geometriaIconLayDistance",
                region: "center"
            });
            topContainer.addChild(iconPane);
            container.addChild(topContainer);
            if (selectionProps) {
                $.each([0, 1, 2], function(index) {
                    pInputs[this].set("value", selectionProps.points[index]);
                });
            }
            var distanceInputDeferred = new Deferred();
            var distanceInput = widgets.validationTextBox({
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        distanceInputDeferred.resolve();
                    }
                }
            });
            var bottomContainer = new LayoutContainer({
                region: "bottom",
                style: "height:40px;"
            });
            var labelPane = new ContentPane({
                region: "left",
                content: dict.get("Distance")
            });
            bottomContainer.addChild(labelPane);
            var distancePane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: distanceInput
            });
            inputEnters.push(distanceInputDeferred.promise);
            bottomContainer.addChild(distancePane);
            container.addChild(bottomContainer);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_laydistance",
                dict.get("LayDistance"), inputEnters);
            dialog.okButton.set("disabled", true);
            $.each(pInputs, function(inputIndex) {
                this.set("validator", function() {
                    var inputProps = {
                        figureName: figure.name,
                        distance: distanceInput.get("value").trim(),
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase(),
                                 pInputs[2].get("value").trim().toUpperCase()]
                    };
                    var results = { points: [{}, {}, {}], distance: {} };
                    var valid = validate(inputProps, results);
                    if (results.points[inputIndex].error) {
                        this.invalidMessage = results.points[inputIndex].error;
                    }
                    dialog.okButton.set("disabled", !valid);
                    return results.points[inputIndex].valid ||
                        !inputProps.points[inputIndex].length;
                });
            });
            distanceInput.set("validator", function() {
                var inputProps = {
                        figureName: figure.name,
                        distance: distanceInput.get("value").trim(),
                        points: [pInputs[0].get("value").trim().toUpperCase(),
                                 pInputs[1].get("value").trim().toUpperCase(),
                                 pInputs[2].get("value").trim().toUpperCase()]
                };
                var results = { points: [{}, {}, {}], distance: {} };
                var valid = validate(inputProps, results);
                if (results.distance.error) {
                    this.invalidMessage = results.distance.error;
                }
                dialog.okButton.set("disabled", !valid);
                return results.distance.valid || !inputProps.distance.length;
            });
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var inputProps = {
                    figureName: figure.name,
                    distance: distanceInput.get("value").trim(),
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
            $.each(props.addedPoints, function(index, label) {
                var p = solid.points[label];
                var faces = solid.facesThroughPoints([props.points[1], props.points[2]]);
                $.each(faces, function() {
                    this.removePoint(label);
                });
                delete solid.points[label];
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
            return dict.get("LayDistanceInFigure", props.distance, props.points[0],
                props.points[1], props.points[2], props.figureName);
        },

        toJson: function(props) {
            return {
                "action": "layDistanceAction",
                "props": {
                    "figureName": props.figureName,
                    "distance": props.distance,
                    "points": props.points
                }
            };
        }
    };
});
