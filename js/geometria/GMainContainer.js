/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "exports",
    "dojo/Deferred",
    "dijit/form/SimpleTextarea",
    "dijit/layout/BorderContainer",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GActions",
    "geometria/GCalculator",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GHelp",
    "geometria/GLogContainer",
    "geometria/GMenuBar",
    "geometria/GNavigator",
    "geometria/GNavigatorToolBar",
    "geometria/GNotepadContainer",
    "geometria/GProblem",
    "geometria/GProblemText",
    "geometria/GToolBar",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(exports, Deferred, SimpleTextarea, BorderContainer, ContentPane, LayoutContainer,
        actions, calculator, dict, figuresContainer, help, logContainer, menuBar, navigator,
        navigatorToolBar, notepadContainer, GProblem, problemText, toolBar, utils, widgets) {

    var container, centerPane, logPane, navigatorPane, figuresPane, helpPane;
    
    exports.currentDocument = null;
    exports.masterSolution = null;
    exports.baseUrl = null;
    exports.readOnly = null;
    
    exports.setCurrentDocument = function(doc) {
        if (this.currentDocument) {
            $.each(this.currentDocument.figures, function() {
                this.camera.seize();
            });
        }
        this.currentDocument = doc;
    };
    
    exports.startUp = function(baseUrl) {
        exports.baseUrl = baseUrl;
        var queryParams = utils.getQueryParams();
        lang = queryParams["lang"];
        if (lang) {
            dict.setLanguage(lang);
        }
        container = new BorderContainer({
            "class": "geometria_maincontainer",
            design: "headline"
        });
        var topPane = new LayoutContainer({
            region: "top",
            layoutPriority: 2,
            style: "height:30px"
        });
        var dojoMenuBar = menuBar.startUp({
            region: "top"
        });
        topPane.addChild(dojoMenuBar);
        var dojoToolBar = toolBar.startUp({
            region: "center",
            resize: function(changeSize, resultSize) {
                if (!resultSize) {
                    var height = toolBar.getPreferredHeight(changeSize.w);
                    topPane.set("style", "height:" + height + "px");
                    $(this.domNode).width($(dojoMenuBar.domNode).width());
                }
            }
        });
        topPane.addChild(dojoToolBar);
        container.addChild(topPane);
        
        var leftPane = new BorderContainer({
            "class": "geometria_left",
            region: "left",
            layoutPriority: 3,
            splitter: true
        });
        var problemTextArea = problemText.startUp({
            region: "top",
            splitter: true
        });
        leftPane.addChild(problemTextArea);
        notepadPane = notepadContainer.startUp({
            region: "center",
            splitter: true
        });
        leftPane.addChild(notepadPane);
        var calculatorPane = calculator.startUp({
            region: "bottom",
            splitter: false
        });
        leftPane.addChild(calculatorPane);
        container.addChild(leftPane);

        centerPane = new BorderContainer({
            region: "center",
            layoutPriority: 4,
            splitter: true
        });
        figuresPane = figuresContainer.startUp({
            "class": "geometria_figures",
            region: "center",
            splitter: true
        });
        centerPane.addChild(figuresPane);
        container.addChild(centerPane);
        logPane = logContainer.startUp({
            "class": "geometria_log",
            region: "bottom",
            splitter: true
        });

        navigatorPane = new LayoutContainer({
            "class": "geometria_navigator",
            region: "left",
            layoutPriority: 1,
            splitter: true
        });
        dojoToolBar = navigatorToolBar.startUp({
            "class": "geometria_navigatortop",
            region: "top"
        });
        navigatorPane.addChild(dojoToolBar);
        nPane = navigator.startUp({
            region: "center"
        });
        navigatorPane.addChild(nPane);
        var navigatorReady = exports.toggleNavigator();
        helpPane = help.startUp({
            "class": "geometria_help",
            design: "headline",
            region: "right",
            layoutPriority: 1,
            splitter: true
        });
        container.placeAt(dojo.body(), "last");
        container.startup();
        window.onbeforeunload = function(event) {
            if (!exports.readOnly && exports.currentDocument.modified) {
                return exports.currentDocument instanceof GProblem ?
                    dict.get("ProblemModifiedQuit") : dict.get("SolutionModifiedQuit");
            }
        };
        navigator.isReadOnly().then(function(readOnly) {
            exports.readOnly = readOnly;
            var id = queryParams["id"];
            if (id) {
                navigatorReady.then(function() {
                    var item = navigator.itemById(id);
                    if (item && (item.type == 'p' || item.type == 's')) {
                        navigator.selectItem(id);
                        actions["openAction"].base.execute(id);
                        return;
                    }
                });
            }
            actions["newProblemAction"].base.execute();
        });
    };
    
    exports.setDocumentModified = function(modified) {
        exports.currentDocument.modified = modified;
        var asterisked = window.document.title.charAt(0) == "*";
        if (modified && !asterisked) {
            window.document.title = "*" + window.document.title;
        }
        else if (!modified && asterisked) {
            window.document.title = window.document.title.substring(1);
        }
    };

    exports.onCloseDocument = function() {
        var deferred = new Deferred();
        if (exports.readOnly) {
            deferred.resolve();
        }
        else {
            var doc = exports.currentDocument instanceof GProblem ? exports.currentDocument :
                    exports.masterSolution;
            if (!doc || !doc.modified) {
                deferred.resolve();
            }
            else {
                var message = doc instanceof GProblem ? dict.get("ProblemModified") :
                    dict.get("SolutionModified");
                var promises = widgets.yesNoCancelDialog(message);
                promises.yes.then(function() {
                    actions.saveDocumentAction.execute().then(function() {
                        deferred.resolve();
                    });
                });
                promises.no.then(function() {
                    deferred.resolve();
                });
            }
        }
        return deferred.promise;
    };
    
    exports.toggleNavigator = function() {
        var deferred = new Deferred();

        var onSuccess = function() {
            deferred.resolve();
        }
        
        var onError = function(message) {
            widgets.errorDialog(message);
            deferred.reject();
        };
    
        if (container.getIndexOfChild(navigatorPane) < 0) {
            container.addChild(navigatorPane);
            navigator.populate().then(onSuccess, onError);
        }
        else {
            container.removeChild(navigatorPane);
            navigator.destroy();
        }
        return deferred.promise;
    };

    exports.isNavigatorVisible = function() {
        return container.getIndexOfChild(navigatorPane) > -1;
    };
    
    
    exports.setLogVisible = function(visible) {
        if (visible) {
            if (centerPane.getIndexOfChild(logPane) < 0) {
                centerPane.addChild(logPane);
            }
        }
        else {
            centerPane.removeChild(logPane);
        }
    };

    exports.toggleHelp = function() {
        if (container.getIndexOfChild(helpPane) < 0) {
            container.addChild(helpPane);
        }
        else {
            container.removeChild(helpPane);
        }
    };
    
    exports.isHelpVisible = function() {
        return container.getIndexOfChild(helpPane) > -1;
    };
    
    exports.newProblem = function() {
        actions.clearActionQueue();
        exports.setLogVisible(false);
        var doc = new GProblem();
        exports.setCurrentDocument(doc);
        problemText.documentChanged();
        figuresContainer.documentChanged();
        notepadContainer.clear();
        calculator.clear();
        window.document.title = "Geometria: " + dict.get("UntitledProblem");
    };
});
