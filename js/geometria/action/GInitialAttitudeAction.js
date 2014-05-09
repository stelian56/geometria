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

        label: dict.get("action.InitialAttitude"),
        
        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            var camera = figure.camera;
            camera.seize();
            camera.toInitialAttitude();
            figure.draw();
        },
        
        validateSelection: function() {
            return true;
        }
    };
});