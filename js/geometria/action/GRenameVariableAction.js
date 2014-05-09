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
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(Deferred, ContentPane, LayoutContainer, dict, mainContainer, notepadContainer,
            utils, widgets) {

    var helpTopic = "RenameVariable";
    
    var validate = function(props, results) {
        results = results || {};
        if (!utils.variableRegex.test(props.newName)) {
            results.error = dict.get("VariableRule");
            return false;
        }
        if (notepadContainer.getRecord(props.newName)) {
            results.error = dict.get("VariableAlreadyExists", props.newName);
            return false;
        }
        results.valid = true;
        return true;
    };
    
    var validateExternal = function(props) {
        if (!props.oldName || !props.newName || !notepadContainer.getRecord(props.oldName)) {
            return false;
        }
        return validate(props);
    };
    
    var apply = function(props) {
        notepadContainer.variableRenamed(props.oldName, props.newName);
        return props;
    };

    return {

        loggable: true,
        
        label: dict.get("action.RenameVariable"),

        execute: function() {
            var dialogDeferred = new Deferred();
            var options = [];
            $.each(mainContainer.currentDocument.notepad, function() {
                options.push({ label: this.variable.name, value: this.variable.name });
            });
            var container = new LayoutContainer();
            var topPane = new LayoutContainer({
                region: "top",
                style: "height:40px"
            });
            var labelPane = new ContentPane({
                region: "left",
                content: dict.get("SelectVariable")
            });
            topPane.addChild(labelPane);
            var oldNameInput = widgets.select({
                options: options
            });
            var oldNamePane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: oldNameInput
            });
            topPane.addChild(oldNamePane);
            container.addChild(topPane);
            var newNameInputDeferred = new Deferred();
            var newNameInput = widgets.validationTextBox({
                placeHolder: dict.get("EnterNewVariableName"),
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        newNameInputDeferred.resolve();
                    }
                }
            });
            var newNamePane = new ContentPane({
                "class": "geometria_inputpane",
                region: "center",
                content: newNameInput
            });
            container.addChild(newNamePane);
            var dialog = widgets.okCancelHelpDialog(container, helpTopic, "geometria_renamevariable",
                dict.get("RenameVariable"), [newNameInputDeferred.promise]);
            dialog.okButton.set("disabled", true);
            newNameInput.set("validator", function() {
                var inputProps = {
                    newName: newNameInput.get("value").trim()
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
                    oldName: oldNameInput.get("value"),
                    newName: newNameInput.get("value").trim()
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
            this.base.enabled = mainContainer.currentDocument.notepad.length > 0;
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            return dict.get("RenameVariableTo", props.oldName, props.newName);
        },
        
        toJson: function(props) {
            return {
                "action": "renameVariableAction",
                "props": {
                    "oldName": props.oldName,
                    "newName": props.newName
                }
            };
        }
    };
});
