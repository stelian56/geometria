/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "dijit/form/ValidationTextBox",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GProblem",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(Deferred, ValidationTextBox, ContentPane, LayoutContainer, dict, figuresContainer,
            mainContainer, notepadContainer, GProblem, utils, widgets) {

    var helpTopic = "RenameFigure";
    
    var validate = function(props, results) {
        results = results || {};
        if (figuresContainer.getFigure(props.newName)) {
            results.error = dict.get("FigureAlreadyExists", props.newName);
            return false;
        }
        if (!utils.figureNameRegex.test(props.newName)) {
            results.error = dict.get("FigureNameRule");
            return false;
        }
        results.valid = true;
        return true;
    };

    var validateExternal = function(props) {
        if (!props.oldName || !props.newName) {
            return false;
        }
        if (!figuresContainer.getFigure(props.oldName)) {
            return false;
        }
        if (mainContainer.currentDocument.problem.containsFigure(props.oldName)) {
            return false;
        }
        return validate(props);
    };

    var apply = function(props) {
        figuresContainer.renameFigure(props.oldName, props.newName);
        notepadContainer.figureRenamed(props.oldName, props.newName);
        return props;
    };

    return {
    
        loggable: true,
        
        figureSpecific: true,
        
        label: dict.get("action.RenameFigure"),

        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            var doc = mainContainer.currentDocument;
            var problem = doc instanceof GProblem ? doc : doc.problem;
            if (figure.name == problem.answer.figureName) {
                widgets.errorDialog(dict.get("CannotRenameReferencedFigure", figure.name));
                return null;
            }
            var dialogDeferred = new Deferred();
            var dialog;
            var container = new LayoutContainer();
            var nameInputDeferred = new Deferred();
            var nameInput = widgets.validationTextBox({
                placeHolder: dict.get("EnterNewFigureName"),
                invalidMessage: dict.get("FigureNameRule"),
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        nameInputDeferred.resolve();
                    }
                }
            });
            var namePane = new ContentPane({
                region: "center",
                content: nameInput
            });
            container.addChild(namePane);
            dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_renamefigure",
                dict.get("RenameFigure", figure.name), [nameInputDeferred.promise]);
            dialog.okButton.set("disabled", true);
            nameInput.set("validator", function() {
                var inputProps = {
                    oldName: figure.name,
                    newName: nameInput.get("value").trim()
                };
                var results = {};
                var valid = validate(inputProps, results);
                if (results.error) {
                    this.invalidMessage = results.error;
                }
                dialog.okButton.set("disabled", !valid);
                return results.valid || !inputProps.newName.length;
            });
            dialog.ok.then(function() {
                var inputProps = {
                    oldName: figure.name,
                    newName: nameInput.get("value").trim()
                };
                var outProps = apply(inputProps);
                notepadContainer.update();
                mainContainer.setDocumentModified(true);
                dialogDeferred.resolve(outProps);
            });
            return dialogDeferred.promise;
        },

        undo: function(props) {
            var undoProps = { oldName: props.newName, newName: props.oldName };
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
            return dict.get("RenameFigureTo", props.oldName, props.newName);
        },

        toJson: function(props) {
            return {
                "action": "renameFigureAction",
                "props": {
                    "oldName": props.oldName,
                    "newName": props.newName
                }
            };
        }
    };
});
