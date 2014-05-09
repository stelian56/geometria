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
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GPoint",
    "geometria/GProblem",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(Deferred, ContentPane, LayoutContainer, dict, figuresContainer, mainContainer,
            notepadContainer, GPoint, GProblem, utils, widgets) {

    var helpTopic = "RenamePoint";
    
    var validate = function(props, results) {
        results = results || {};
        if (!utils.labelRegex.test(props.newLabel)) {
            results.error = dict.get("LabelRule");
            return false;
        }
        var figure = figuresContainer.getFigure(props.figureName);
        if (figure.solid.points[props.newLabel]) {
            results.error = dict.get("PointAlreadyExists", props.newLabel, props.figureName);
            return false;
        }
        results.valid = true;
        return true;
    };
    
    var validateExternal = function(props) {
        if (!props.figureName || !props.oldLabel || !props.newLabel) {
            return false;
        }
        var figure = figuresContainer.getFigure(props.figureName);
        if (!figure) {
            return false;
        }
        if (!figure.solid.points[props.oldLabel]) {
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
        if (selectedElements.length == 1 && selectedElements[0] instanceof GPoint) {
            var props = { 
                oldLabel: [selectedElements[0].label]
            };
            return props;
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        solid.renamePoint(props.oldLabel, props.newLabel);
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        notepadContainer.pointRenamed(props.oldLabel, props.newLabel, props.figureName);
        return props;
    };

    return {

        loggable: true,

        figureSpecific: true,
        
        label: dict.get("action.RenamePoint"),

        execute: function(contextMenuTriggered) {
            var selectionProps = validateSelection();
            var dialogDeferred = new Deferred();
            var options = [];
            var figure = figuresContainer.getSelectedFigure();
            $.each(figure.solid.points, function(label, point) {
                options.push({ label: label, value: label });
            });
            var container = new LayoutContainer();
            var topPane = new LayoutContainer({
                region: "top",
                style: "height:40px"
            });
            var labelPane = new ContentPane({
                region: "left",
                content: dict.get("SelectPoint")
            });
            topPane.addChild(labelPane);
            var oldLabelInput = widgets.select({
                options: options
            });
            if (contextMenuTriggered && selectionProps) {
                oldLabelInput.set("value", selectionProps.oldLabel);
            }
            var oldLabelPane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: oldLabelInput
            });
            topPane.addChild(oldLabelPane);
            container.addChild(topPane);
            var newLabelInputDeferred = new Deferred();
            var newLabelInput = widgets.validationTextBox({
                placeHolder: dict.get("EnterNewPointLabel"),
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        newLabelInputDeferred.resolve();
                    }
                }
            });
            var newLabelPane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: newLabelInput
            });
            container.addChild(newLabelPane);
            var dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_renamepoint",
                dict.get("RenamePointIn", figure.name), [newLabelInputDeferred.promise]);
            dialog.okButton.set("disabled", true);
            newLabelInput.set("validator", function() {
                var inputProps = {
                    figureName: figure.name,
                    oldLabel: oldLabelInput.get("value"),
                    newLabel: newLabelInput.get("value").trim().toUpperCase()
                };
                var results = {};
                var valid = validate(inputProps, results);
                if (results.error) {
                    this.invalidMessage = results.error;
                }
                dialog.okButton.set("disabled", !valid);
                return results.valid || !inputProps.newLabel.length;

            });
            if (selectionProps) {
                oldLabelInput.set("value", selectionProps.oldLabel);
            }
            dialog.ok.then(function() {
                var inputProps = {
                    figureName: figure.name,
                    oldLabel: oldLabelInput.get("value"),
                    newLabel: newLabelInput.get("value").trim().toUpperCase()
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
            var undoProps = { oldLabel: props.newLabel, newLabel: props.oldLabel,
                figureName: props.figureName };
            apply(undoProps);
            notepadContainer.update();
        },
        
        playBack: function(props, external) {
            if (external && !validateExternal(props)) {
                return null;
            }
            var outProps = apply(props);
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
            return dict.get("RenamePointTo", props.oldLabel, props.newLabel, props.figureName);
        },
        
        toJson: function(props) {
            return {
                "action": "renamePointAction",
                "props": {
                    "figureName": props.figureName,
                    "oldLabel": props.oldLabel,
                    "newLabel": props.newLabel
                }
            };
        }
    };
});
