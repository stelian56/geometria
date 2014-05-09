/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GDictionary",
    "geometria/GFace",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GMeasurement",
    "geometria/GNotepadContainer",
    "geometria/GNotepadRecord",
    "geometria/GPoint",
    "geometria/GUtils",
    "geometria/GVariable",
    "geometria/GWidgets"
], function(Deferred, ContentPane, LayoutContainer, dict, GFace, figuresContainer,
            mainContainer, GMeasurement, notepadContainer, GNotepadRecord, GPoint,
            utils, GVariable, widgets) {

    var helpTopic = "ComputeArea";
    var measurementType = "area";
    
    var validate = function(props, results) {
        results = results || {};
        if (!utils.variableRegex.test(props.variableName)) {
            results.error = dict.get("VariableRule");
            return false;
        }
        if (notepadContainer.getRecord(props.variableName)) {
            results.error = dict.get("VariableAlreadyExists", props.variableName);
            return false;
        }
        results.valid = true;
        return true;
    };
    
    var validateExternal = function(props) {
        if (!props.figureName) {
            return false;
        }
        if (!props.variableName) {
            return false;
        }
        var figure = figuresContainer.getFigure(props.figureName);
        if (!figure) {
            return false;
        }
        var labels = utils.getLabels(props.face);
        var faces = figure.solid.facesThroughPoints(labels);
        if (utils.propCount(faces) != 1) {
            return false;
        }
        return validate(props);
    };
    
    var validateSelection = function() {
        var figure = figuresContainer.getSelectedFigure();
        var solid = figure.solid;
        var labels = [];
        var faces = [];
        $.each(solid.selection, function(code, element) {
			if (element instanceof GPoint) {
				labels.push(element.label);
			}
            else if (element instanceof GFace) {
				faces.push(element);
            }
			else {
				labels.push(element.p1.label);
				labels.push(element.p2.label);
			}
		});
        var face;
        if (faces.length == 1) {
            face = faces[0];
        }
        else {
            var fs = solid.facesThroughPoints(labels);
            if (utils.propCount(fs) == 1) {
                face = utils.anyProp(fs);
            }
        }
        if (face) {
            var props = { 
                face: face.getFaceCode()
            };
            return props;
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var labels = utils.getLabels(props.face);
        var faces = figure.solid.facesThroughPoints(labels);
        var face = utils.anyProp(faces);
        var area = face.computeArea(solid);
        var variable = new GVariable(props.variableName, area);
        var expression = new GMeasurement(measurementType, props);
        var record = new GNotepadRecord(variable, expression);
        notepadContainer.add(record);
        return props;
    };

    return {

        loggable: true,
        
        figureSpecific: true,
    
        icon: "geometriaIcon24 geometriaIcon24Area",
        
        label: dict.get("action.Area"),

        measurementType: measurementType,

        expressionRegex: /^area\[(([A-Z][0-9]*){3})\]$/,
        
        execute: function(contextMenuTriggered) {
            var selectionProps = validateSelection();
            var dialogDeferred = new Deferred();
            var options = [];
            var figure = figuresContainer.getSelectedFigure();
            $.each(figure.solid.faces, function() {
                var code = this.getFaceCode();
                options.push({ label: code, value: code });
            });
            var container = new LayoutContainer();
            var topPane = new LayoutContainer({
                region: "top",
                style: "height:40px"
            });
            var facePane = new ContentPane({
                region: "left",
                content: dict.get("SelectFace")
            });
            topPane.addChild(facePane);
            var faceInput = widgets.select({
                options: options
            });
            if (contextMenuTriggered) {
                faceInput.set("value", selectionProps.face);
            }
            var facePane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: faceInput
            });
            topPane.addChild(facePane);
            container.addChild(topPane);
            var bottomContainer = new LayoutContainer({
                region: "center"
            });
            bottomContainer.addChild(new ContentPane({
                region: "left",
                content: dict.get("AssignToVariable")
            }));
            var variableInputDeferred = new Deferred();
            var variableInput = widgets.validationTextBox({
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        variableInputDeferred.resolve();
                    }
                }
            });
            var variablePane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: variableInput
            });
            bottomContainer.addChild(variablePane);
            container.addChild(bottomContainer);
            var dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_area",
                dict.get("AreaIn", figure.name), [variableInputDeferred.promise]);
            dialog.okButton.set("disabled", true);
            variableInput.set("validator", function() {
                var inputProps = {
                    variableName: variableInput.get("value").trim()
                };
                var results = {};
                var valid = validate(inputProps, results);
                if (results.error) {
                    this.invalidMessage = results.error;
                }
                dialog.okButton.set("disabled", !valid);
                return results.valid || !inputProps.variableName.length;
            });
            if (selectionProps) {
                faceInput.set("value", selectionProps.face);
            }
            dialog.ok.then(function() {
                var inputProps = {
                    figureName: figure.name,
                    face: faceInput.get("value"),
                    variableName: variableInput.get("value").trim()
                };
                var outProps = apply(inputProps);
                figure.solid.selection = {};
                figure.draw();
                mainContainer.setDocumentModified(true);
                dialogDeferred.resolve(outProps);
            });
            return dialogDeferred.promise;
        },
        
        getExpression: function(props) {
            return "area[" + props.face + "]";
        },
        
        validateSelection: validateSelection,

        undo: function() {
            notepadContainer.removeLastRecord();
        },
        
        playBack: function(props, external) {
            if (external && !validateExternal(props)) {
                return null;
            }
            return apply(props);
        },

        evaluate: function(props) {
            var outProps = {
                figureName: props.figureName,
                variableName: props.variableName
            };
            var match = new RegExp(this.expressionRegex).exec(props.expression);
            outProps.face = match[1];
            if (!validateExternal(outProps)) {
                return null;
            }
            return apply(outProps);
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            return dict.get("ComputeAreaOfFace", props.variableName, props.face, props.figureName);
        },
        
        toJson: function(props) {
            return {
                "action": "areaAction",
                "props": {
                    "figureName": props.figureName,
                    "face": props.face,
                    "variableName": props.variableName
                }
            };
        }
    };
});
