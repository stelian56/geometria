/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GMainContainer"
], function(dict, mainContainer) {

    return {

        enableAtPlayBack: true,

        icon: "geometriaIcon24 geometriaIcon24Navigator",
        
        label: dict.get("action.Navigator"),
        
        execute: function() {
            mainContainer.toggleNavigator();
            return {};
        },
        
        updateState: function() {
            this.base.active = mainContainer.isNavigatorVisible();
        }
    };
});