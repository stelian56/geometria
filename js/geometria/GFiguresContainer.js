/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dijit/layout/TabContainer",
    "geometria/GActions",
    "geometria/GMainContainer"
], function(TabContainer, actions, mainContainer) {

    var json;
    var container;
    var watchHandle;

    var watch = function() {
        return container.watch("selectedChildWidget", function(name, oldValue, newValue) {
            var name = newValue.title;
            var doc = mainContainer.currentDocument;
            $.each(doc.figures, function() {
                var figure = this;
                figure.camera.seize();
                if (figure.name == name) {
                    figure.selected = true;
                    figure.draw();
                }
                else {
                    figure.selected = false;
                }
            });
            actions.updateStates();
        });
    };
    
    return {
    
        selectorActive: true,
    
        startUp: function() {
            var figuresContainer = this;
            container = new TabContainer(arguments[0]);
            container.startup();
            watchHandle = watch();
            return container;
        },

        documentChanged: function() {
            watchHandle.unwatch();
            var children = container.getChildren();
            $.each(children, function() {
                container.removeChild(this);
            });
            var doc = mainContainer.currentDocument;
            $.each(doc.figures, function() {
                var figure = this;
                var figurePane = figure.startUp();
                container.addChild(figurePane);
                if (figure.selected) {
                    container.selectChild(figure.pane);
                    figure.draw();
                }
            });
            watchHandle = watch();
        },

        select: function(figureName) {
            var doc = mainContainer.currentDocument;
            $.each(doc.figures, function() {
                var selected = this.name == figureName;
                this.selected = selected;
                if (selected) {
                    container.selectChild(this.pane);
                }
            });
        },
        
        addFigure: function(figure, figureIndex) {
            watchHandle.unwatch();
            var doc = mainContainer.currentDocument;
            if (figureIndex) {
                doc.figures.splice(figureIndex, 0, figure);
            }
            else {
                doc.figures.push(figure);
            }
            var figurePane = figure.startUp();
            container.addChild(figurePane, figureIndex);
            this.select(figure.name);
            figure.draw();
            watchHandle = watch();
        },
        
        removeFigure: function(name) {
            watchHandle.unwatch();
            var figures = mainContainer.currentDocument.figures;
            var figureIndex;
            for (figureIndex = 0; figureIndex < figures.length; figureIndex++) {
                var figure = figures[figureIndex];
                if (figure.name == name) {
                    figure.camera.seize();
                    figures.splice(figureIndex, 1);
                    container.removeChild(figure.pane);
                    if (figure.selected && figures.length) {
                        figures[0].selected = true;
                        container.selectChild(figures[0].pane);
                    }
                    break;
                }
            }
            watchHandle = watch();
            return figureIndex;
        },

        renameFigure: function(oldName, newName) {
            watchHandle.unwatch();
            $.each(mainContainer.currentDocument.figures, function() {
                var figure = this;
                if (figure.name == oldName) {
                    figure.name = newName;
                    figure.pane.set("title", newName);
                    return false;
                }
            });
            watchHandle = watch();
        },
        
        getSelectedFigure: function() {
            var selectedFigure;
            $.each(mainContainer.currentDocument.figures, function() {
                if (this.selected) {
                    selectedFigure = this;
                    return false;
                }
            });
            return selectedFigure;
        },

        getFigure: function(figureName) {
            var figure;
            $.each(mainContainer.currentDocument.figures, function() {
                if (this.name == figureName) {
                    figure = this;
                    return false;
                }
            });
            return figure;
        }
    };
});
