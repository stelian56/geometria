/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GMainContainer",
    "geometria/GNavigator",
    "geometria/GWidgets"
], function(dict, mainContainer, navigator, widgets) {

    return {

        icon: "geometriaIcon24 geometriaIcon24NewFolder",

        label: dict.get("action.NewFolder"),
        
        execute: function() {
            var onError = function(message) {
                widgets.errorDialog(message);
            };
        
            navigator.newFolder().then(null, onError);
        },
        
        updateState: function() {
            this.base.enabled = !mainContainer.readOnly;
        },
        
        tooltip: function() {
            return mainContainer.readOnly ? dict.get("ReadOnly") : "";
        }
    };
});
