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

        icon: "geometriaIcon24 geometriaIcon24NextTopic",

        label: dict.get("action.NextTopic"),
        
        execute: function() {
            help.nextTopic();
            return {};
        },
        
        updateState: function() {
            this.base.enabled = help.hasNextTopic();
        }
    };
});