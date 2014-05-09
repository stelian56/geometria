/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "dijit/form/NumberTextBox",
    "geometria/GDictionary",
    "geometria/GFace",
    "geometria/GFigure",
    "geometria/GFiguresContainer",
    "geometria/GLine",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GPoint",
    "geometria/GProblem",
    "geometria/GSolid",
    "geometria/GUtils"
], function(lang, NumberTextBox, dict, GFace, GFigure, figuresContainer, GLine, logContainer,
        mainContainer, GPoint, GProblem, GSolid, utils) {

    var maxSideCount = 100;

    var validateExternal = function(props) {
        return props.sideCount &&
               props.sideCount == Math.floor(props.sideCount) &&
               props.sideCount >= 3 &&
               props.sideCount <= 100;
    };
    
    var apply = function(props) {
        var sideCount = props.sideCount;
        var dAngle = 2*Math.PI/sideCount;
        var angle = 0;
        var ps = [];
        var label, crds, p;
        var sideIndex
        for (sideIndex = 0; sideIndex < sideCount; sideIndex++) {
            label = utils.getNextLabel(label);
            crds = vec3.fromValues(Math.cos(angle), Math.sin(angle), 0);
            p = new GPoint(label, crds);
            p.isVertex = true;
            ps.push(p);
            angle -= dAngle;
        }
        for (sideIndex = 0; sideIndex < sideCount; sideIndex++) {
            label = utils.getNextLabel(label);
            var pBaseCrds = ps[sideIndex].crds;
            crds = vec3.fromValues(pBaseCrds[0], pBaseCrds[1], 1);
            p = new GPoint(label, crds);
            p.isVertex = true;
            ps.push(p);
        }
        var faces = [];
        var upperEdges = [];
        var lowerEdges = [];
        var sideEdges, line, face, p1, p2, pBase1, pBase2;
        for (sideIndex = 0; sideIndex < sideCount; sideIndex++) {
            pBase1 = ps[sideIndex];
            pBase2 = ps[(sideIndex + 1) % sideCount];
            line = new GLine([pBase1.label, pBase2.label]);
            lowerEdges.push(line);
            p1 = ps[sideCount + sideIndex];
            p2 = ps[sideCount + (sideIndex + 1) % sideCount];
            line = new GLine([p2.label, p1.label]);
            upperEdges.push(line);
            sideEdges = [];
            line = new GLine([pBase1.label, p1.label]);
            sideEdges.push(line);
            line = new GLine([p1.label, p2.label]);
            sideEdges.push(line);
            line = new GLine([p2.label, pBase2.label]);
            sideEdges.push(line);
            line = new GLine([pBase2.label, pBase1.label]);
            sideEdges.push(line);
            face = new GFace(4, sideEdges);
            faces.push(face);
        }
        face = new GFace(sideCount, lowerEdges);
        faces.splice(0, 0, face);
        upperEdges.reverse();
        face = new GFace(sideCount, upperEdges);
        faces.splice(0, 0, face);
        var solid = new GSolid(ps, faces);
        solid.makeConfig();
        solid.computeGCenter();
        solid.computeBoundingSphere();
        var figureName = utils.getNewFigureName();
        var figure = new GFigure(figureName, solid);
        figuresContainer.addFigure(figure);
        var outProps = lang.mixin({}, props);
        outProps.figureName = figureName;
        return outProps;
    };

    return {

        loggable: true,

        icon: "geometriaIcon24 geometriaIcon24Prism",
        
        label: dict.get("action.Prism"),

        popup: function(parent) {
            var action = this;
            return new NumberTextBox({
                constraints: { min: 3, max: maxSideCount },
                placeHolder: dict.get("EnterNumberOfSides"),
                onKeyUp: function(event) {
                    if (event.keyCode == 13 && this.isValid()) {
                        parent.domNode.click();
                        var props = {
                            sideCount: parseInt(this.get("value"))
                        };
                        this.set("value", "");
                        action.base.execute(props);
                    }
                }
            });
        },

        execute: function(props) {
            var outProps = apply(props);
            mainContainer.setDocumentModified(true);
            return outProps;
        },
        
        undo: function(props) {
            figuresContainer.removeFigure(props.figureName);
        },

        playBack: function(props, external) {
            if (external && !validateExternal(props)) {
                return null;
            }
            return apply(props);
        },
        
        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            return dict.get("NewPrism", props.sideCount, props.figureName);
        },
        
        toJson: function(props) {
            return {
                "action": "prismAction",
                "props": {
                    "sideCount": props.sideCount
                }
            };
        }
    };
});
