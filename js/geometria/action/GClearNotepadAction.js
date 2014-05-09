/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GActions",
    "geometria/GDictionary",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GProblem",
    "geometria/GWidgets"
], function(actions, dict, mainContainer, notepadContainer, GProblem, widgets) {

    return {

        label: dict.get("action.ClearNotepad"),
        
        execute: function() {
            var message = dict.get("SureClearNotepad");
            var dialog = widgets.yesNoDialog(message).yes.then(function() {
                mainContainer.currentDocument.notepad = [];
                actions.clearActionQueue();
                mainContainer.setDocumentModified(true);
                notepadContainer.clear();
            });
            return {};
        },
        
        updateState: function() {
            var doc = mainContainer.currentDocument;
            this.base.enabled = doc instanceof GProblem && doc.notepad.length > 0;
        }
    };
});
