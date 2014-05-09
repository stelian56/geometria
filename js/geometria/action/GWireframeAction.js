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

        icon: "geometriaIcon24 geometriaIcon24Wireframe",

        label: dict.get("action.Wireframe"),

        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            figure.toggleWireframe();
            figure.draw();
            return {};
        },
        
        updateState: function() {
            var figure = figuresContainer.getSelectedFigure();
            this.base.active = figure && figure.isWireframe();
        }
    };
});