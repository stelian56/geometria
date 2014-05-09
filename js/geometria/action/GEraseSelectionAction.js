/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "geometria/GDictionary",
    "geometria/GFace",
    "geometria/GFiguresContainer",
    "geometria/GLine",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GPoint",
    "geometria/GUtils"
], function(lang, dict, GFace, figuresContainer, GLine, mainContainer, notepadContainer, GPoint,
            utils) {

    var validate = function(props) {
        var solid = figuresContainer.getFigure(props.figureName).solid;
        var removedLines = {};
        var valid = true;
        $.each(props.lines, function() {
            stickCodes = this;
            var faces = solid.facesThroughPoints(stickCodes);
            if (utils.propCount(faces) > 1) {
                valid = false;
                return false;
            }
            var face = utils.anyProp(faces);
            var faceCode = face.getFaceCode();
            var removedLs = removedLines[faceCode];
            if (!removedLs) {
                removedLs = [];
                removedLines[faceCode] = removedLs;
            }
            var line = face.lineThroughPoints(stickCodes[0], stickCodes[1]);
            removedLs.push(line.labels.slice(0));
        });
        if (!valid) {
            return null;
        }
        var removedPoints = {};
        $.each(props.points, function(index, label) {
            var p = solid.points[label];
            removedPoints[label] = { crds: p.crds, affectedLines: [] };
        });
        $.each(props.points, function(index, label) {
            var faces = solid.facesThroughPoints([label]);
            $.each(faces, function() {
                var face = this;
                var lines = face.linesThroughPoint(label);
                var removedLs = removedLines[face.getFaceCode()];
                if (removedLs) {
                    $.each(removedLs, function() {
                        var labels = this;
                        var line = face.lineThroughPoints(labels[0], labels[1]);
                        utils.remove(lines, line);
                    });
                }
                $.each(lines, function() {
                    var line = this;
                    if (line.firstLabel() == label || line.lastLabel() == label) {
                        valid = false;
                        return false;
                    }
                });
                if (!valid) {
                    return false;
                }
            });
            if (!valid) {
                return false;
            }
        });
        if (valid) {
            var outProps = lang.mixin({
                removedPoints: removedPoints,
                removedLines: removedLines
            }, props);
            return outProps;
        }
        return null;
    };

    var validateExternal = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        if (!figure) {
            return null;
        }
        if (!Array.isArray(props.points) || !Array.isArray(props.lines) || !props.figureName) {
            return null;
        }
        var valid = true;
        $.each(props.lines, function() {
            if (!Array.isArray(this) || this.length != 2) {
                valid = false;
                return false;
            }
        });
        if (valid) {
            return validate(props);
        }
        return null;
    };
    
    var validateSelection = function() {
        var figure = figuresContainer.getSelectedFigure();
        var solid = figure.solid;
        var selectedElements = [];
        var pointLabels = [];
        var stickCodes = [];
        $.each(solid.selection, function(code, element) {
            if (element instanceof GPoint) {
                pointLabels.push(element.label);
            }
            else if (!(element instanceof GFace)) {
                stickCodes.push([element.p1.label, element.p2.label]);
            }
        });
        if (pointLabels.length > 0 || stickCodes.length > 0) {
            var props = {
                figureName: figure.name,
                points: pointLabels,
                lines: stickCodes
            }
            return validate(props);
        }
        return null;
    };

    var apply = function(props) {
        var figure = figuresContainer.getFigure(props.figureName);
        var solid = figure.solid;
        var danglingPoints = {};
        $.each(props.removedLines, function(faceCode, removedLs) {
            var face = solid.getFace(faceCode);
            $.each(removedLs, function() {
                var labels = this;
                var line = face.lineThroughPoints(labels[0], labels[1]);
                var danglingLabels = [];
                face.removeLine(line, danglingLabels);
                $.each(danglingLabels, function(index, label) {
                    danglingPoints[label] = solid.points[label];
                });
            });
        });
        $.each(props.removedPoints, function(label, removedP) {
            var affectedLs = [];
            var faces = solid.facesThroughPoints([label]);
            $.each(faces, function() {
                var face = this;
                var lines = face.linesThroughPoint(label);
                if (lines.length > 0) {
                    var removedLs = props.removedLines[face.getFaceCode()];
                    if (removedLs) {
                        $.each(removedLs, function() {
                            var labels = this;
                            var line = face.lineThroughPoints(labels[0], labels[1]);
                            utils.remove(lines, line);
                        });
                    }
                    affectedLs = affectedLs.concat(lines);
                }
            });
            if (affectedLs.length > 0) {
                $.each(affectedLs, function() {
                    var line = this;
                    utils.remove(line.labels, label);
                    removedP.affectedLines.push(line.toJson());
                });
            }
            delete solid.points[label];
        });
        $.each(danglingPoints, function(label) {
            if (!props.removedPoints[label]) {
                var crds = solid.points[label].crds;
                props.removedPoints[label] = { crds: crds, affectedLines: [] };
                delete solid.points[label];
            }
        });
        $.each(props.removedPoints, function(label) {
            notepadContainer.pointRemoved(label, props.figureName, props.executeId);
        });
        solid.makeConfig();
        solid.selection = {};
        figure.draw(true);
        return props;
    };

    return {

        loggable: true,
    
        figureSpecific: true,

        icon: "geometriaIcon24 geometriaIcon24Erase",

        label: dict.get("action.EraseSelection"),
        
        execute: function() {
            var figure = figuresContainer.getSelectedFigure();
            var solid = figure.solid;
            var selectionProps = validateSelection();
            selectionProps.executeId = this.getExecuteId();
            var outProps = apply(selectionProps);
            notepadContainer.update();
            mainContainer.setDocumentModified(true);
            return outProps;
        },

        validateSelection: validateSelection,
        
        undo: function(props) {
            var figure = figuresContainer.getFigure(props.figureName);
            var solid = figure.solid;
            solid.selection = {};
            $.each(props.removedPoints, function(label, removedP) {
                solid.points[label] = new GPoint(label, removedP.crds);
            });
            $.each(props.removedLines, function(faceCode, removedLs) {
                var face = solid.getFace(faceCode);
                $.each(removedLs, function() {
                    var labels = this;
                    var line = new GLine(labels);
                    face.lines.push(line);
                });
            });
            $.each(props.removedPoints, function(label, removedP) {
                if (removedP.affectedLines) {
                    $.each(removedP.affectedLines, function() {
                        var labels = this;
                        var lines = solid.linesThroughPoints(labels[0], labels[1]);
                        var p = solid.points[label];
                        $.each(lines, function() {
                            this.insert(p, solid);
                        });
                    });
                }
            });
            $.each(props.removedPoints, function(label) {
                notepadContainer.removePointUndone(props.executeId);
            });
            notepadContainer.update();
            solid.makeConfig();
            figure.draw(true);
            figuresContainer.select(props.figureName);
        },
        
        playBack: function(props, external) {
            var outProps = lang.mixin({}, props);
            if (external) {
                outProps.executeId = this.base.getExecuteId();
                outProps = validateExternal(outProps);
                if (!outProps) {
                    return null;
                }
            }
            outProps = apply(outProps);
            if (!external) {
                notepadContainer.update();
            }
            return outProps;
        },

        toTooltip: function(props) {
            return this.toLog(props);
        },
        
        toLog: function(props) {
            return dict.get("EraseSelectionInFigure", props.figureName);
        },

        updateState: function() {
            this.base.enabled = figuresContainer.getSelectedFigure() &&
                this.validateSelection() && true;
        },

        toJson: function(props) {
            return {
                "action": "eraseSelectionAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points,
                    "lines": props.lines
                }
            };
        }
    };
});
