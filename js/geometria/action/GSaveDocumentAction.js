/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "geometria/GDictionary",
    "geometria/GMainContainer",
    "geometria/GNavigator",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(Deferred, dict, mainContainer, navigator, utils, widgets) {

    return {

        icon: "geometriaIcon24 geometriaIcon24SaveDocument",

        label: dict.get("action.SaveDocument"),
        
        execute: function() {
            var deferred = new Deferred();
            var doc = mainContainer.currentDocument;

            var onSuccess = function() {
                var name = navigator.itemById(doc.navigatorItemId).name;
                window.document.title = "Geometria: " + name;
                mainContainer.setDocumentModified(false);
                deferred.resolve();
            };
            
            var onError = function(err) {
                widgets.errorDialog(err);
                deferred.reject();
            };
            
            navigator.save(doc).then(onSuccess, onError);
            return deferred.promise;
        },
        
        updateState: function() {
            this.base.enabled = !mainContainer.readOnly;
        },
        
        tooltip: function() {
            return mainContainer.readOnly ? dict.get("ReadOnly") : "";
        }
    };
});
