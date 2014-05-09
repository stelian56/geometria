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

        icon: "geometriaIcon24 geometriaIcon24Labels",

        label: dict.get("action.Labels"),
        
        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            figure.labeled = !figure.labeled;
            figure.draw();
            return {};
        },
        
        updateState: function() {
            var figure = figuresContainer.getSelectedFigure();
            this.base.enabled = figure && figure.isWireframe();
            this.base.active = this.base.enabled && figure.labeled;
        }
    };
});