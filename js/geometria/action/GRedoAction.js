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
    
        icon: "geometriaIcon24 geometriaIcon24Redo",

        label: dict.get("action.Redo"),
        
        execute: function() {
            var record = actions.getNextAction();
            var action = record.action;
            action.base.playBack(record.props);
            if (action.loggable && mainContainer.currentDocument instanceof GSolution) {
                logContainer.add(record);
            }
            actions.actionRedone();
            mainContainer.setDocumentModified(true);
            return {};
        },
        
        updateState: function() {
            this.base.enabled = actions.getNextAction() && true;
        },
        
        tooltip: function() {
            var record = actions.getNextAction();
            if (record) {
                var actionTooltip = record.action.toTooltip(record.props);
                return dict.get("RedoAction", actionTooltip);
            }
            else {
                return "";
            }
        }
    };
});
