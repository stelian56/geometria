/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GDictionary",
    "geometria/GFigure",
    "geometria/GFiguresContainer",
    "geometria/GGallery",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GProblem",
    "geometria/GSolid"
], function(dict, GFigure, figuresContainer, gallery, logContainer, mainContainer, GProblem,
        GSolid) {

    return {

        loggable: true,
    
        icon: "geometriaIcon24 geometriaIcon24Octahedron",

        label: dict.get("action.Octahedron"),
        
        execute: function() {
            var props = gallery.addFigure("octahedron");
            mainContainer.setDocumentModified(true);
            return props;
        },
        
        undo: function(props) {
            figuresContainer.removeFigure(props.figureName);
        },

        playBack: function(props) {
            var props = gallery.addFigure("octahedron");
            return props;
        },
        
        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            var figureName = props.figureName;
            return dict.get("NewOctahedron", figureName);
        },
        
        toJson: function() {
            return {
                "action": "octahedronAction"
            };
        }
    };
});
