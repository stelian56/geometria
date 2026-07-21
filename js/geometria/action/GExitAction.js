/**
 * Copyright 2000-2026 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "geometria/GDictionary",
    "geometria/GMainContainer"
], function(Deferred, dict, mainContainer) {

    return {

        label: dict.get("action.Exit"),
        
        execute: function() {
            var deferred = new Deferred();
            mainContainer.onCloseDocument().then(function() {
                window.document.open();
                window.document.close();
                deferred.resolve();
            });
            return deferred.promise;
        },
        
        updateState: function() {
            this.base.enabled = true;
        }
    };
});
