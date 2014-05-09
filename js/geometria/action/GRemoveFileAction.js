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
    "geometria/action/GNewProblemAction",
    "geometria/GWidgets"
], function(dict, mainContainer, navigator, newProblemAction, widgets) {

    return {

        icon: "geometriaIcon24 geometriaIcon24RemoveFile",

        label: dict.get("action.Delete"),
        
        execute: function() {
            var selectedItem = navigator.getSelectedItem();
            var name = selectedItem.name;
            var message = selectedItem.type == 'd' ? dict.get("SureDeleteFolder", name) :
                dict.get("SureDeleteFile", name);
            var dialog = widgets.yesNoDialog(message).yes.then(function() {
                var onSuccess = function() {
                    var id = mainContainer.currentDocument.navigatorItemId;
                    if (!navigator.itemById(id)) {
                        newProblemAction.execute(true);
                    }
                };
                
                var onError = function(message) {
                    widgets.errorDialog(message);
                };
                
                navigator.remove().then(onSuccess, onError);
            });
        },
        
        updateState: function() {
            this.base.enabled = !mainContainer.readOnly && navigator.isSelectedItemRemovable();
        },
        
        tooltip: function() {
            return mainContainer.readOnly ? dict.get("ReadOnly") : "";
        }
    };
});
