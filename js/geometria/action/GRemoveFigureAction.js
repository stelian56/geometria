/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "geometria/GDictionary",
    "geometria/GFigure",
    "geometria/GFiguresContainer",
    "geometria/GNotepadContainer",
    "geometria/GMainContainer",
    "geometria/GProblem",
    "geometria/GSolid",
    "geometria/GWidgets"
], function(lang, dict, GFigure, figuresContainer, notepadContainer, mainContainer, GProblem,
            GSolid, widgets) {

    var validateExternal = function(props) {
        if (!props.figureName) {
            return false;
        }
        if (!figuresContainer.getFigure(props.figureName)) {
            return false;
        }
        if (mainContainer.currentDocument.problem.containsFigure(props.figureName)) {
            return false;
        }
        return true;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var figureIndex = figuresContainer.removeFigure(props.figureName);
        notepadContainer.figureRemoved(props.figureName, props.executeId);
        var solid = figure.solid;
        $.each(solid.relatedSolids, function() {
            delete this.relatedSolids[solid.id];
        });
        var solidProps = figure.solid.toJson().solid;
        var outProps = lang.mixin({
            figureIndex: figureIndex,
            solid: solidProps
        }, props);
        return outProps;
    };

    return {
    
        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24RemoveFigure",

        label: dict.get("action.RemoveFigure"),
        
        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            var doc = mainContainer.currentDocument;
            var problem = doc instanceof GProblem ? doc : doc.problem;
            if (figure.name == problem.answer.figureName) {
                widgets.errorDialog(dict.get("CannotRemoveReferencedFigure", figure.name));
                return null;
            }
            var props = {
                figureName: figure.name,
                executeId: this.getExecuteId()
            };
            var results = {};
            var outProps = apply(props);
            notepadContainer.update();
            mainContainer.setDocumentModified(true);
            return outProps;
        },
        
        undo: function(props) {
            var solid = new GSolid().make(props.solid);
            var figure = new GFigure(props.figureName, solid);
            figuresContainer.addFigure(figure, props.figureIndex);
            notepadContainer.removeFigureUndone(props.executeId);
            notepadContainer.update();
        },

        playBack: function(props, external) {
            var outProps = lang.mixin({}, props);
            if (external) {
                outProps.executeId = this.base.getExecuteId();
                if (!validateExternal(outProps)) {
                    return null;
                }
            }
            outProps = apply(outProps);
            if (!external) {
                notepadContainer.update();
            }
            return outProps;
        },

        updateState: function() {
            var doc = mainContainer.currentDocument;
            if (!(doc instanceof GProblem)) {
                var figure = figuresContainer.getSelectedFigure();
                if (figure) {
                    if (doc.problem.containsFigure(figure.name)) {
                        this.base.enabled = false;
                    }
                }
            }
        },
        
        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            var figureName = props.figureName;
            return dict.get("RemoveFigure", figureName);
        },

        toJson: function(props) {
            return {
                "action": "removeFigureAction",
                "props": {
                    "figureName": props.figureName
                }
            };
        }
    };
});
