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
    "geometria/GNavigator",
    "geometria/GMainContainer",
    "geometria/GActions"
], function(Deferred, dict, navigator, mainContainer, actions) {

    return {

        icon: "geometriaIcon24 geometriaIcon24NewProblem",

        label: dict.get("action.NewProblem"),
        
        execute: function(doNotConfirm) {
            if (doNotConfirm) {
                mainContainer.newProblem();
            }
            else {
                var deferred = new Deferred();
                mainContainer.onCloseDocument().then(function() {
                    mainContainer.newProblem();
                    navigator.selectItem(null);
                    deferred.resolve();
                });
                return deferred.promise;
            }
        },
        
        updateState: function() {
            this.base.enabled = true;
        }
    };
});
