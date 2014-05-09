/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "dijit/layout/LayoutContainer",
    "dojox/widget/ColorPicker",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GWidgets"
], function(Deferred, LayoutContainer, ColorPicker, dict, figuresContainer, widgets) {

    var helpTopic = "Color";
    var color;
    
    return {

        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24Color",

        label: dict.get("action.Color"),
        
        execute: function() {
            var deferred = new Deferred();
            var figure = figuresContainer.getSelectedFigure();
            var container = new LayoutContainer();
            var colorPicker = new ColorPicker({
                region: "center",
                value: figure.color,
                animatePoint: false,
                showHsv: false
            });
            container.addChild(colorPicker);
            var figure = figuresContainer.getSelectedFigure();
            var dialog = widgets.okCancelHelpDialog(container, helpTopic,
                "geometria_colorpicker", dict.get("ColorOf", figure.name));
            dialog.ok.then(function() {
                var figure = figuresContainer.getSelectedFigure();
                var newColor = colorPicker.value;
                var props = {
                    figureName: figure.name,
                    oldColor: figure.color,
                    newColor: newColor
                };
                figure.color = newColor;
                figure.draw();
                deferred.resolve(props);
            });
            return deferred.promise;
        },
        
        undo: function(props) {
            var figure = figuresContainer.getFigure(props.figureName);
            figure.color = props.oldColor;
            figure.draw();
        },
        
        playBack: function(props) {
            var figure = figuresContainer.getFigure(props.figureName);
            figure.color = props.newColor;
            figure.draw();
        },
        
        toTooltip: function() {
            return dict.get("action.Color");
        },
        
        updateState: function() {
            var figure = figuresContainer.getSelectedFigure();
            this.base.enabled = figure && !figure.isWireframe();
        }
    }
});