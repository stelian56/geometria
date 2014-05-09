/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GFigure",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GNavigator",
    "geometria/GNotepadContainer",
    "geometria/GSolid",
    "geometria/GUtils"
], function(GFigure, figuresContainer, mainContainer, navigator, notepadContainer, GSolid, utils) {

    var solidsProps = {
        "tetrahedron": {
            "points": {
                "A": [0, 1.15470053837925153, 0],
                "B": [-1, -0.57735026918962576, 0],
                "C": [1, -0.57735026918962576, 0],
                "D": [0, 0, 1.63299316185545204]
            }
        },
        "cube": {
            "points": {
                "A": [-1, -1, -1],
                "B": [1, -1, -1],
                "C": [1, 1, -1],
                "D": [-1, 1, -1],
                "E": [-1, -1, 1],
                "F": [1, -1, 1],
                "G": [1, 1, 1],
                "H": [-1, 1, 1]
            }
        },
        "octahedron": {
            "points": {
                "A": [1, 0, 0],
                "B": [0, 1, 0],
                "C": [-1, 0, 0],
                "D": [0, -1, 0],
                "E": [0, 0, 1],
                "F": [0, 0, -1]
            }
        },
        "dodecahedron": {
            "points": {
                "A": [1, 1, 1],
                "B": [1, 1, -1],
                "C": [1, -1, 1],
                "D": [1, -1, -1],
                "E": [-1, 1, 1],
                "F": [-1, 1, -1],
                "G": [-1, -1, 1],
                "H": [-1, -1, -1],
                "I": [0, 0.61803398874989485, 1.61803398874989484],
                "J": [0, 0.61803398874989485, -1.61803398874989484],
                "K": [0, -0.61803398874989485, 1.61803398874989484],
                "L": [0, -0.61803398874989485, -1.61803398874989484],
                "M": [0.61803398874989485, 1.61803398874989484, 0],
                "N": [0.61803398874989485, -1.61803398874989484, 0],
                "O": [-0.61803398874989485, 1.61803398874989484, 0],
                "P": [-0.61803398874989485, -1.61803398874989484, 0],
                "Q": [1.61803398874989484, 0, 0.61803398874989485],
                "R": [1.61803398874989484, 0, -0.61803398874989485],
                "S": [-1.61803398874989484, 0, 0.61803398874989485],
                "T": [-1.61803398874989484, 0, -0.61803398874989485]
            }
        },
        "icosahedron": {
            "points": {
                "A": [0, 1, 1.61803398874989484],
                "B": [0, 1, -1.61803398874989484],
                "C": [0, -1, 1.61803398874989484],
                "D": [0, -1, -1.61803398874989484],
                "E": [1, 1.61803398874989484, 0],
                "F": [1, -1.61803398874989484, 0],
                "G": [-1, 1.61803398874989484, 0],
                "H": [-1, -1.61803398874989484, 0],
                "I": [1.61803398874989484, 0, 1],
                "J": [1.61803398874989484, 0, -1],
                "K": [-1.61803398874989484, 0, 1],
                "L": [-1.61803398874989484, 0, -1]
           }
       }
    };
    
    return {

        addFigure: function(name) {
            var solidProps = solidsProps[name];
            var solid = new GSolid().make(solidProps);
            var figureName = utils.getNewFigureName();
            var figure = new GFigure(figureName, solid);
            figuresContainer.addFigure(figure);
            props = {
                figureName: figureName
            };
            return props;
        }
    }
});
