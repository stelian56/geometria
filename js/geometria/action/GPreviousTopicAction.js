/**
 * Copyright (C) 2000-2014 Geometria Contributors
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

        icon: "geometriaIcon24 geometriaIcon24PreviousTopic",

        label: dict.get("action.PreviousTopic"),
        
        execute: function() {
            help.previousTopic();
            return {};
        },
        
        updateState: function() {
            this.base.enabled = help.hasPreviousTopic();
        }
    };
});
