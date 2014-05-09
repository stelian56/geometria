/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GUtils"
], function(dict, figuresContainer, utils) {

    return {

        figureSpecific: true,

        enableAtPlayBack: true,

        icon: "geometriaIcon24 geometriaIcon24ZoomOut",
        
        label: dict.get("action.ZoomOut"),
        
        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            figure.zoom(1/utils.zoomFactor);
        }
    };
});