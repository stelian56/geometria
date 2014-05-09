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
    "geometria/GMainContainer",
    "geometria/GProblem",
    "geometria/GWidgets"
], function(Deferred, ContentPane, LayoutContainer, dict, mainContainer, GProblem, widgets) {

    var helpTopic = "ExportDocument";

    return {

        label: dict.get("action.ExportDocument"),
        
        execute: function() {
            var dialog;
            var deferred = new Deferred();
            var container = new LayoutContainer();
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
            var doc = mainContainer.currentDocument;
            var json = doc.toJson();
            var content = JSON.stringify(json, null, "  ");
            textArea.set("value", content);
            container.addChild(textArea);
            var title = doc instanceof GProblem ? dict.get("ExportProblem") :
                dict.get("ExportSolution");
            dialog = widgets.okHelpDialog(container, helpTopic, "geometria_export",
                dict.get("Close"), title, [deferred.promise]);
        }
    };
});