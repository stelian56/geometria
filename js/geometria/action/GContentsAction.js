/**
 * Copyright 2000-2026 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GHelp"
], function(dict, help) {

    return {

        enableAtPlayBack: true,
        
        icon: "geometriaIcon24 geometriaIcon24Contents",

        label: dict.get("action.Contents"),
        
        execute: function() {
            help.newTopic();
            return {};
        },
        
        updateState: function() {
            this.base.enabled = true;
        }
    };
});
