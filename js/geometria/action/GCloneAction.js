/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GFigure",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GSolid",
    "geometria/GUtils"
], function(dict, GFigure, figuresContainer, mainContainer, GSolid, utils) {

    var validateExternal = function(props) {
        if (!props.figureName) {
            return false;
        }
        if (!figuresContainer.getFigure(props.figureName)) {
            return false;
        }
        return true;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solidProps = figure.solid.toJson().solid;
        var solid = new GSolid().make(solidProps);
        var destFigureName = utils.getNewFigureName();
        var destFigure = new GFigure(destFigureName, solid);
        figuresContainer.addFigure(destFigure);
        var outProps = {
            figureName: props.figureName,
            destFigureName: destFigureName
        };
        return outProps;
    };

    return {

        loggable: true,
        
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24Clone",

        label: dict.get("action.Clone"),
        
        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            var props = {
                figureName: figure.name
            }
            var outProps = apply(props);
            mainContainer.setDocumentModified(true);
            return outProps;
        },

        undo: function(props) {
            figuresContainer.removeFigure(props.destFigureName);
        },

        playBack: function(props, external) {
            if (external && !validateExternal(props)) {
                return null;
            }
            return apply(props);
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            return dict.get("Clone", props.figureName, props.destFigureName);
        },

        toJson: function(props) {
            return {
                "action": "cloneAction",
                "props": {
                    "figureName": props.figureName
                }
            };
        }
    };
});