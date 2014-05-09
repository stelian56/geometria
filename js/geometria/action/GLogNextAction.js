/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GSolution"
], function(dict, logContainer, mainContainer, GSolution) {

    return {

        enableAtPlayBack: true,

        icon: "geometriaIcon24 geometriaIcon24LogNext",

        label: dict.get("action.Next"),
        
        execute: function() {
            logContainer.playNext();
            return {};
        },
        
        updateState: function() {
            var doc = mainContainer.currentDocument;
            if (doc instanceof GSolution) {
                var recordCount = mainContainer.masterSolution.log.length;
                if (recordCount < 1) {
                    this.base.enabled = false;
                }
                else {
                    this.base.enabled = logContainer.getSelectedIndex() < recordCount - 1;
                }
            }
            else {
                this.base.enabled = false;
            }
        }
    };
});
