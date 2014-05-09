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
    "geometria/GMainContainer"
], function(Deferred, dict, mainContainer) {

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
                    deferred.resolve();
                });
                return deferred.promise;
            }
        }
    };
});