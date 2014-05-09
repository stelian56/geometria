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
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GSolution"
], function(actions, dict, logContainer, mainContainer, GSolution) {

    return {

        icon: "geometriaIcon24 geometriaIcon24Undo",
        
        label: dict.get("action.Undo"),
        
        execute: function() {
            var record = actions.getCurrentAction();
            var action = record.action;
            action.undo(record.props);
            var doc = mainContainer.currentDocument;
            if (doc instanceof GSolution && action.loggable) {
                logContainer.removeLastRecord();
            }
            actions.actionUndone();
            mainContainer.setDocumentModified(true);
            return {};
        },
        
        updateState: function() {
            this.base.enabled = actions.getCurrentActionIndex() > -1;
        },
        
        tooltip: function() {
            var record = actions.getCurrentAction();
            if (record) {
                var actionTooltip = record.action.toTooltip(record.props);
                return dict.get("UndoAction", actionTooltip);
            }
            else {
                return "";
            }
        }
    };
});