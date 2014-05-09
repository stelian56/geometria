/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GNavigator",
    "geometria/GProblem",
    "geometria/GWidgets"
], function(dict, logContainer, mainContainer, navigator, GProblem, widgets) {

    return {
    
        label: dict.get("action.SaveDocumentAs"),
        
        execute: function() {
            var doc = mainContainer.currentDocument;

            var onSuccess = function() {
                var name = navigator.itemById(doc.navigatorItemId).name;
                window.document.title = "Geometria: " + name;
                mainContainer.setDocumentModified(false);
            };
            
            var onError = function(err) {
                widgets.errorDialog(err);
            };
            
            navigator.saveAs(doc).then(onSuccess, onError);
        },
        
        updateState: function() {
            this.base.enabled = !mainContainer.readOnly;
        },
        
        tooltip: function() {
            return mainContainer.readOnly ? dict.get("ReadOnly") : "";
        }
    };
});
