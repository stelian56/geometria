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

        icon: "geometriaIcon24 geometriaIcon24Selector",

        label: dict.get("action.Selector"),

        execute: function() {
            figuresContainer.selectorActive = !figuresContainer.selectorActive;
            return {};
        },
        
        validateSelection: function() {
            return true;
        },
        
        updateState: function() {
            this.base.active = figuresContainer.selectorActive;
        }
    };
});