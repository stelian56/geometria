/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/Deferred",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GProblem",
    "geometria/GWidgets"
], function(Deferred, dict, figuresContainer, mainContainer, GProblem, widgets) {

    return {

        icon: "geometriaIcon24 geometriaIcon24LockProblem",

        label: dict.get("action.LockProblem"),

        execute: function() {
            var deferred = new Deferred();
            var dialog = widgets.yesNoDialog(dict.get("SureLockProblem"));
            dialog.yes.then(function() {
                var doc = mainContainer.currentDocument;
                doc.answer.locked = true;
                mainContainer.setDocumentModified(true);
                deferred.resolve();
            });
            return deferred.promise;
        },
        
        updateState: function() {
            var doc = mainContainer.currentDocument;
            this.base.enabled = doc instanceof GProblem && !doc.answer.locked;
        }
    };
});