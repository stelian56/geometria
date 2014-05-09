/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "dijit/Tooltip",
    "dijit/form/SimpleTextarea",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GDictionary",
    "geometria/GMainContainer",
    "geometria/GProblem",
    "geometria/GWidgets"
], function(lang, Tooltip, SimpleTextarea, ContentPane, LayoutContainer, dict, mainContainer,
            GProblem, widgets) {

    var contentPane;

    return {
        
        startUp: function() {
            var args = lang.mixin({
                "class": "geometria_problemtext"
            }, arguments[0]);
            var container = new LayoutContainer(args);
            var textArea;
            textArea = widgets.textArea({
                "class": "geometria_problemtextarea",
                region: "center",
                placeholder: dict.get("TypeProblemText"),
                onBlur: function() {
                    var text = this.get("value");
                    mainContainer.currentDocument.text = text
                    mainContainer.setDocumentModified(true);
                    contentPane.set("content", text);
                    container.removeChild(textArea);
                    container.addChild(contentPane);
                }
            });
            contentPane = new ContentPane({
                region: "center",
                onMouseOver: function() {
                    var doc = mainContainer.currentDocument;
                    if (doc instanceof GProblem && !doc.text.length) {
                        Tooltip.show(dict.get("EditProblemText"), this.domNode);
                    }
                },
                onMouseOut: function() {
                    Tooltip.hide(this.domNode);
                },
                onDblClick: function() {
                    var doc = mainContainer.currentDocument;
                    if (doc instanceof GProblem) {
                        Tooltip.hide(this.domNode);
                        textArea.set("value", doc.text);
                        container.removeChild(this);
                        container.addChild(textArea);
                        textArea.focus();
                    }
                }
            });
            container.addChild(contentPane);
            return container;
        },
        
        documentChanged: function() {
            var doc = mainContainer.currentDocument;
            var text = doc instanceof GProblem ? doc.text : doc.problem.text;
            contentPane.set("content", text);
            
        }
    };
});
