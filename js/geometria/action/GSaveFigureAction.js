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
    "geometria/GMainContainer",
    "geometria/GNavigator",
    "geometria/GWidgets"
], function(dict, figuresContainer, mainContainer, navigator, widgets) {

    return {
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24SaveFigure",

        label: dict.get("action.SaveFigure"),
        
        execute: function() {
            var onError = function(err) {
                widgets.showErrorDialog(err);
            };

            var figure = figuresContainer.getSelectedFigure();
            navigator.saveAs(figure.solid).then(function() {}, onError);
        },
        
        updateState: function() {
            this.base.enabled = !mainContainer.readOnly;
        },
        
        tooltip: function() {
            return mainContainer.readOnly ? dict.get("ReadOnly") : "";
        }
    };
});
