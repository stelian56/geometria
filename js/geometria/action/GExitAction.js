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

    var url = "http://localhost/geometria";

    return {

        label: dict.get("action.Exit"),
        
        execute: function() {
            var deferred = new Deferred();
            mainContainer.onCloseDocument().then(function() {
                window.document.location = url;
                deferred.resolve();
            });
            return deferred.promise;
        }
    };
});