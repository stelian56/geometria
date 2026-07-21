/**
 * Copyright 2000-2026 Geometria Contributors
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

        icon: "geometriaIcon24 geometriaIcon24Selector",

        label: dict.get("action.Selector"),

        execute: function() {
            figuresContainer.selectorActive = !figuresContainer.selectorActive;
            return {};
        },
        
        updateState: function() {
            this.base.enabled = figuresContainer.getSelectedFigure();
            this.base.active = figuresContainer.selectorActive;
        },
        
        validateSelection: function() {
            return true;
        }
    };
});
