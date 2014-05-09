/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "dojo/Deferred",
    "dijit/Dialog",
    "dojo/on",
    "dijit/Tooltip",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "dijit/layout/TabContainer",
    "geometria/GDictionary",
    "geometria/GMainContainer",
    "geometria/GProblem",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, Deferred, Dialog, on, Tooltip, ContentPane,
        LayoutContainer, TabContainer, dict, mainContainer, GProblem, utils, widgets) {

    var helpTopic = "DocumentProperties";
        
    var createSpace = function(doc, tooltip, hint) {
        var container = new LayoutContainer();
        var contentPane, textArea;
        if (hint) {
            textArea = widgets.textArea({
                "class": "geometria_propertiespane",
                region: "center",
                placeholder: hint,
                value: doc.properties,
                onBlur: function() {
                    container.removeChild(textArea);
                    var content = utils.resolveMacros(this.get("value"));
                    contentPane.set("content", content);
                    container.addChild(contentPane);
                }
            });
        }
        var contentPane = new ContentPane({
            "class": "geometria_propertiespane",
            region: "center",
            content: utils.resolveMacros(doc.properties),
            onDblClick: function() {
                if (hint) {
                    Tooltip.hide(this.domNode);
                    container.removeChild(this);
                    container.addChild(textArea);
                    textArea.focus();
                }
            }
        });
        var tooltipHandle = on(contentPane.domNode, "mouseover", function() {
            Tooltip.show(tooltip, contentPane.domNode);
        });
        on(contentPane.domNode, "mouseout", function() {
            Tooltip.hide(contentPane.domNode);
        });
        container.addChild(contentPane);
        return {
            container: container,
            textArea: textArea,
            tooltipHandle: tooltipHandle
        };
    };

    var removeTooltipHandles = function(spaces) {
        $.each(spaces, function() {
            this.tooltipHandle.remove();
        });
    };
    
    return {

        icon: "geometriaIcon24 geometriaIcon24Properties",
        
        label: dict.get("action.Properties"),
        
        execute: function() {
            var deferred = new Deferred();
            var doc = mainContainer.currentDocument;
            var container = new LayoutContainer();
            var docTooltip = doc instanceof GProblem ? dict.get("EditProblemProperties") :
                dict.get("EditSolutionProperties");
            var docHint = doc instanceof GProblem ? dict.get("TypeProblemProperties") :
                dict.get("TypeSolutionProperties");
            var spaces = [];
            var docSpace = createSpace(doc, docTooltip, docHint);
            spaces.push(docSpace);
            docSpace.container.set("region", "center");
            container.addChild(docSpace.container);
            if (!(doc instanceof GProblem)) {
                var problemSpace = createSpace(doc.problem, dict.get("ProblemProperties"));
                spaces.push(problemSpace);
                problemSpace.container.set("region", "top");
                problemSpace.container.set("style", "height: 50%");
                container.addChild(problemSpace.container);
            }
            var dialog = widgets.okCancelHelpDialog(container, helpTopic,
                "geometria_properties", dict.get("Properties"));
            dialog.ok.then(function() {
                removeTooltipHandles(spaces);
                var properties = docSpace.textArea.get("value").trim();
                if (properties != doc.properties) {
                    doc.properties = properties;
                    mainContainer.setDocumentModified(true);
                }
                deferred.resolve();
            });
            dialog.cancel.then(function() {
                removeTooltipHandles(spaces);
            });
            dialog.help.then(function() {
                removeTooltipHandles(spaces);
            });
            return deferred.promise;
        }
    };
});
