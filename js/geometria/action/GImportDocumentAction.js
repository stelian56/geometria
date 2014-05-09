/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GActions",
    "geometria/GCalculator",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GProblem",
    "geometria/GProblemText",
    "geometria/GSolution",
    "geometria/GWidgets"
], function(Deferred, ContentPane, LayoutContainer, actions, calculator, dict,
            figuresContainer, logContainer, mainContainer, notepadContainer, GProblem, problemText,
            GSolution, widgets) {

    var helpTopic = "ImportDocument";

    var apply = function(content) {
        try {
            var json = $.parseJSON(content);
        }
        catch (err) {
            return false;
        }
        var doc;
        if (json.problem) {
            mainContainer.setLogVisible(false);
            calculator.clear();
            notepadContainer.clear();
            doc = new GProblem();
            mainContainer.setCurrentDocument(doc);
            try {
                doc.makeAll(json.problem);
            }
            catch (err) {
                return false;
            }
            mainContainer.setLogVisible(false);
            figuresContainer.documentChanged();
            return true;
        }
        else if (json.solution) {
            notepadContainer.clear();
            doc = new GSolution();
            mainContainer.setCurrentDocument(doc);
            try {
                doc.make(json.solution);
            }
            catch (err) {
                return false;
            }
            logContainer.clear();
            mainContainer.setLogVisible(true);
            figuresContainer.documentChanged();
            $.each(json.solution.log, function() {
                var action = actions[this.action];
                var props = this.props || {};
                props = action.base.playBack(props);
                var record = { action: action, props: props };
                logContainer.add(record);
            });
            mainContainer.masterSolution = doc;
            return true;
        }
        return false;
    };
    
    return {

        label: dict.get("action.ImportDocument"),
        
        execute: function() {
            var deferred = new Deferred();
            mainContainer.onCloseDocument().then(function() {
                var container = new LayoutContainer();
                var textArea = widgets.textArea({
                    region: "center",
                    placeholder: dict.get("CopyPasteProblemSolution")
                });
                container.addChild(textArea);
                var dialog = widgets.okHelpDialog(container, helpTopic, "geometria_export", null,
                    dict.get("ImportDocument"));
                dialog.ok.then(function() {
                    var content = textArea.get("value");
                    if (apply(content)) {
                        actions.clearActionQueue();
                        problemText.documentChanged();
                        window.document.title = "Geometria: " + dict.get("UntitledProblem");
                        deferred.resolve();
                    }
                    else {
                        widgets.errorDialog(dict.get("ImportHasInvalidFormat"));
                        deferred.reject();
                    }
                });
            });
            return deferred.promise;
        }
    };
});
