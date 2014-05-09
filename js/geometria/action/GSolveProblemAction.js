/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "geometria/GActions",
    "geometria/GCalculator",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GProblem",
    "geometria/GProblemText",
    "geometria/GSolution"
], function(Deferred, actions, calculator, dict, figuresContainer, logContainer, mainContainer,
            notepadContainer, GProblem, problemText, GSolution) {

    return {

        icon: "geometriaIcon24 geometriaIcon24SolveProblem",

        label: dict.get("action.SolveProblem"),
        
        execute: function() {
            var deferred = new Deferred();
            mainContainer.onCloseDocument().then(function() {
                actions.clearActionQueue();
                var currentDocument = mainContainer.currentDocument;
                var problem;
                if (currentDocument instanceof GSolution) {
                    problem = currentDocument.problem;
                }
                else {
                    currentDocument.notepad = [];
                    problem = currentDocument;
                }
                var doc = new GSolution(null, problem);
                mainContainer.setCurrentDocument(doc);
                mainContainer.masterSolution = doc;
                logContainer.clear();
                mainContainer.setLogVisible(true);
                problemText.documentChanged();
                figuresContainer.documentChanged();
                notepadContainer.clear();
                calculator.clear();
                window.document.title = "Geometria: " + dict.get("UntitledSolution");
                deferred.resolve();
            });
            return deferred.promise;
        }
    };
});