/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GWidgets"
], function(Deferred, ContentPane, LayoutContainer, dict, figuresContainer, widgets) {

    var helpTopic = "PrintFigure";

    return {

        figureSpecific: true,
        
        enableAtPlayBack: true,
    
        icon: "geometriaIcon24 geometriaIcon24Print",
    
        label: dict.get("action.PrintFigure"),
        
        execute: function() {
            var dialog;
            var deferred = new Deferred();
            var container = new LayoutContainer();
            var textArea = widgets.textArea({
                region: "center",
                readOnly: true,
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                        deferred.resolve();
                    }
                }
            });
            var figure = figuresContainer.getSelectedFigure();
            var serializer = new XMLSerializer();
            var html = serializer.serializeToString(figure.pane.domNode);
            textArea.set("value", html);
            container.addChild(textArea);
            dialog = widgets.okHelpDialog(container, helpTopic, "geometria_export",
                dict.get("Close"), dict.get("PrintFigure", figure.name), [deferred.promise]);
        }
    };
});