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

        icon: "geometriaIcon24 geometriaIcon24RenameFile",

        label: dict.get("action.RenameFile"),
        
        execute: function() {
            var onSuccess = function(item) {
                if (mainContainer.currentDocument.navigatorItemId == item.id) {
                    window.document.title = "Geometria: " + item.name;
                }
            };
            
            var onError = function(message) {
                widgets.errorDialog(message);
            };
        
            navigator.rename().then(onSuccess, onError);
        },
        
        updateState: function() {
            this.base.enabled = !mainContainer.readOnly && navigator.isSelectedItemRemovable();
        },
        
        tooltip: function() {
            return mainContainer.readOnly ? dict.get("ReadOnly") : "";
        }
    };
});
