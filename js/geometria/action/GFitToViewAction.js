/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GFiguresContainer"
], function(dict, figuresContainer) {

    return {

        figureSpecific: true,

        enableAtPlayBack: true,

        icon: "geometriaIcon24 geometriaIcon24FitToView",

        label: dict.get("action.FitToView"),
        
        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            figure.fitToView();
        },
        
        validateSelection: function() {
            return true;
        }
    };
});