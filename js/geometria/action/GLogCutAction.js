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
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GSolution",
    "geometria/GWidgets"
], function(Deferred, actions, dict, figuresContainer, logContainer, mainContainer,
            notepadContainer, GSolution, widgets) {

    return {

        enableAtPlayBack: true,

        icon: "geometriaIcon24 geometriaIcon24LogCut",

        label: dict.get("action.EraseRecordsBelow"),
        
        execute: function() {
            var deferred = new Deferred();
            widgets.yesNoDialog(dict.get("SureDeleteSolutionSteps")).yes.then(function() {
                var selectedIndex = logContainer.getSelectedIndex();
                var masterSolution = mainContainer.masterSolution;
                var doc = new GSolution(masterSolution.navigatorItemId, masterSolution.problem);
                var log = masterSolution.log.slice(0, selectedIndex + 1);
                mainContainer.setCurrentDocument(doc);
                mainContainer.masterSolution = doc;
                figuresContainer.documentChanged();
                notepadContainer.documentChanged();
                logContainer.clear();
                var record;
                $.each(log, function() {
                    record = this;
                    record.action.base.playBack(record.props);
                    logContainer.add(record);
                });
                actions.cropActionQueue(record);
                logContainer.stop();
                mainContainer.setDocumentModified(true);
                deferred.resolve();
            });
            return deferred.promise;
        },
        
        updateState: function() {
            var doc = mainContainer.currentDocument;
            if (doc instanceof GSolution && logContainer.isPlaybackActive()) {
                var selectedIndex = logContainer.getSelectedIndex();
                this.base.enabled = selectedIndex > -1 &&
                    selectedIndex < mainContainer.masterSolution.log.length - 1;
            }
            else {
                this.base.enabled = false;
            }
        }
    };
});
