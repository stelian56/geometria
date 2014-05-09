/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/action/GActionBase",
    "geometria/GDictionary",
    "geometria/GMainContainer",
    "geometria/GProblem",
    "geometria/GProblemAnswer",
    "geometria/GSolutionAnswer"
], function(GActionBase, dict, mainContainer, GProblem, problemAnswer, solutionAnswer) {

    return {
        
        logEnd: true,

        loggable: !(mainContainer.currentDocument instanceof GProblem),

        label: dict.get("action.Answer"),

        execute: function() {
            if (mainContainer.currentDocument instanceof GProblem) {
                return problemAnswer.execute.apply(this, arguments);
            }
            else {
                return solutionAnswer.execute.apply(this, arguments);
            }
        },

        undo: function() {
            if (mainContainer.currentDocument instanceof GProblem) {
                return problemAnswer.undo.apply(this, arguments);
            }
        },

        playBack: function() {
            if (mainContainer.currentDocument instanceof GProblem) {
                return problemAnswer.playBack.apply(this, arguments);
            }
            else {
                return solutionAnswer.playBack.apply(this, arguments);
            }
        },

        toTooltip: function() {
            return dict.get("action.Answer");
        },
        
        toLog: solutionAnswer.toLog,

        toJson: (mainContainer.currentDocument instanceof GProblem) ?
            problemAnswer.toJson : solutionAnswer.toJson
    };
});
