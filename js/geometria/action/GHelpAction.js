/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GHelp",
    "geometria/GMainContainer"
], function(dict, help, mainContainer) {

    return {

        enableAtPlayBack: true,

        icon: "geometriaIcon24 geometriaIcon24Help",

        label: dict.get("action.Help"),
        
        execute: function() {
            if (mainContainer.isHelpVisible()) {
                mainContainer.toggleHelp();
            }
            else {
                help.newTopic();
            }
        },
        
        updateState: function() {
            this.base.active = mainContainer.isHelpVisible();
        }
    };
});
