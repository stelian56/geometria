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

        label: dict.get("action.DeselectAll"),
        
        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            figure.deselectAll();
            return {};
        },
        
        validateSelection: function() {
            var figure = figuresContainer.getSelectedFigure();
            return utils.anyProp(figure.solid.selection) && true;
        },
        
        updateState: function() {
            var figure = figuresContainer.getSelectedFigure();
            return this.base.enabled = figure && utils.anyProp(figure.solid.selection);
        }
    };
});