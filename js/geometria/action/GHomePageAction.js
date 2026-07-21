/**
 * Copyright 2000-2026 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GUtils"
], function(dict, utils) {

    return {

        enableAtPlayBack: true,

        icon: "geometriaIcon24 geometriaIcon24Logo",

        label: dict.get("action.HomePage"),
        
        execute: function() {
            var url = utils.homeUrl;
            window.open(url, "_blank");
        },
        
        updateState: function() {
            this.base.enabled = true;
        }
    };
});
