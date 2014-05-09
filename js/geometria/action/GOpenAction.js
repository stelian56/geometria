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
    "geometria/GFigure",
    "geometria/GFiguresContainer",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GNavigator",
    "geometria/GNotepadContainer",
    "geometria/GProblem",
    "geometria/GProblemText",
    "geometria/GSolid",
    "geometria/GSolution",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(Deferred, actions, calculator, dict, GFigure, figuresContainer, logContainer,
        mainContainer, navigator, notepadContainer, GProblem, problemText, GSolid, GSolution,
        utils, widgets) {

    return {

        icon: "geometriaIcon24 geometriaIcon24Open",

        label: dict.get("action.Open"),
        
        execute: function(itemId) {
            var deferred = new Deferred();

            var onError = function(err) {
                widgets.errorDialog(err);
                deferred.reject();
            };

            var onDeployDocumentSuccess = function(name) {
                actions.clearActionQueue();
                problemText.documentChanged();
                window.document.title = "Geometria: " + name;
                deferred.resolve();
            };

            var deployProblem = function(json, navigatorItemId, name) {
                mainContainer.setLogVisible(false);
                calculator.clear();
                notepadContainer.clear();
                var doc = new GProblem(navigatorItemId);
                mainContainer.setCurrentDocument(doc);
                try {
                    doc.makeAll(json.problem);
                }
                catch (err) {
                    console.error(err);
                    onError(dict.get("ProblemFileCorrupted", name));
                    mainContainer.newProblem();
                    return;
                }
                figuresContainer.documentChanged();
                if (json.problem.answer.locked) {
                    actions.solveProblemAction.base.execute();
                }
                else {
                    onDeployDocumentSuccess(name);
                }
            };
            
            var deploySolution = function(json, navigatorItemId, name) {
                mainContainer.setLogVisible(true);
                calculator.clear();
                logContainer.clear();
                var doc = new GSolution(navigatorItemId);
                mainContainer.setCurrentDocument(doc);
                mainContainer.masterSolution = doc;
                try {
                    doc.make(json.solution);
                    figuresContainer.documentChanged();
                    notepadContainer.clear();
                    $.each(json.solution.log, function() {
                        if (!this.action) {
                            throw "Missing action in solution log";
                        }
                        var action = actions[this.action];
                        if (!action) {
                            throw "Bad action " + this.action + " in solution log";
                        }
                        var logProps = this.props || {};
                        var props = action.base.playBack(logProps, true);
                        if (!props) {
                            throw "Bad action " + this.action + " in solution log";
                        }
                        var record = { action: action, props: props, comments: this.comments };
                        logContainer.add(record);
                    });
                    onDeployDocumentSuccess(name);
                }
                catch (err) {
                    console.error(err);
                    onError(dict.get("SolutionFileCorrupted", name));
                    mainContainer.newProblem();
                    return;
                }
            };
            
            var deployFigure = function(json, name) {
                var solid = new GSolid();
                try {
                    solid.make(json.solid);
                }
                catch (err) {
                    onError(dict.get("FigureFileCorrupted", name));
                    deferred.reject();
                    return;
                }
                var figureName = utils.getNewFigureName();
                var figure = new GFigure(figureName, solid);
                figuresContainer.addFigure(figure);
                mainContainer.setDocumentModified(true);
                var solidProps = solid.toJson().solid;
                var props = {
                    figureName: figureName,
                    fileName: name,
                    solid: solidProps
                }
                deferred.resolve(props);
            };
            
            var onNavigatorSuccess = function(results) {
                var navigatorItemId = results.id;
                var name = navigator.itemById(navigatorItemId).name;
                try {
                    var json = $.parseJSON(results.content);
                }
                catch (err) {
                    onError(dict.get("navigator.FileHasInvalidFormat", name));
                    return;
                }
                if (json.problem || json.solution) {
                    mainContainer.onCloseDocument().then(function() {
                        if (json.problem) {
                            deployProblem(json, navigatorItemId, name);
                        }
                        else if (json.solution) {
                            deploySolution(json, navigatorItemId, name);
                        }
                    });
                }
                else if (json.solid) {
                    if (mainContainer.currentDocument instanceof GProblem) {
                        deployFigure(json, name);
                    }
                    else {
                        onError(dict.get("FileContainsFigure", name));
                    }
                }
                else {
                    onError(dict.get("NotAGeometriaFile", name));
                }
            };

            if (!itemId) {
                itemId = navigator.getSelectedItem().id;
            }
            var doc = mainContainer.currentDocument;
            if (doc && itemId == doc.navigatorItemId) {
                mainContainer.onCloseDocument().then(function() {
                    mainContainer.newProblem();
                    navigator.open().then(onNavigatorSuccess, onError);
                });
            }
            else {
                navigator.open(itemId).then(onNavigatorSuccess, onError);
            }
            return deferred.promise;
        },
        
        undo: function(props) {
            figuresContainer.removeFigure(props.figureName);
        },
        
        playBack: function(props) {
            var solid = new GSolid();
            solid.make(props.solid);
            var figure = new GFigure(props.figureName, solid);
            figuresContainer.addFigure(figure);
            return props;
        },

        toTooltip: function(props) {
            return dict.get("OpenFigureFromFile", props.figureName, props.fileName);
        },

        updateState: function() {
            var item = navigator.getSelectedItem();
            this.base.enabled = item && item.type != 'd';
        }
    };
});
