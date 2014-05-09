/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "geometria/GFace",
    "geometria/GFiguresContainer",
    "geometria/GMath",
    "geometria/GPoint",
    "geometria/GUtils"
], function(lang, GFace, figuresContainer, math, GPoint, utils) {

    var segmentRegex = /^([A-Z][0-9]*)([A-Z][0-9]*)$/;

    var stringToCrds = function(s) {
        var match = new RegExp(utils.crdsRegex).exec(s);
        if (match) {
            var crds = [];
            for (var index = 1; index <= 3; index++) {
                var crd = parseFloat(match[index]);
                if (isNaN(crd)) {
                    return null;
                }
                crds.push(crd);
            }
            return crds;
        }
        return null;
    };

    var crdsToString = function(crds) {
        return "[" + crds[0] + " " + crds[1] + " " + crds[2] + "]";
    };

    var pointSetStringToCrds = function(stringValue, solid) {
        var tokens = stringValue.toUpperCase().split(",");
        var ps = [];
        $.each(tokens, function() {
            var token = this.trim();
            if (solid && utils.labelRegex.test(token)) {
                var p = solid.points[token];
                if (p) {
                    ps.push(p.crds);
                }
                else {
                    ps = null;
                    return false;
                }
            }
            else {
                var crds = stringToCrds(token);
                if (crds) {
                    ps.push(crds);
                }
                else {
                    ps = null;
                    return false;
                }
            }
        });
        return ps;
    };

    var segmentSetStringToSegments = function(stringValue, solid) {
        var tokens = stringValue.toUpperCase().split(",");
        var segments = [];
        $.each(tokens, function() {
            var segment;
            var token = this.trim();
            var match = new RegExp(segmentRegex).exec(token);
            if (match) {
                var p1 = solid.points[match[1]];
                if (p1) {
                    var p2 = solid.points[match[2]];
                    if (p2) {
                        segment = { p1: p1.label, p2: p2.label };
                    }
                }
            }
            if (segment) {
                segments.push(segment);
            }
            else {
                segments = null;
                return false;
            }
        });
        return segments;
    };

    var arePointSetsEqual = function(ps1, ps2, refLength) {
        var ps2 = ps2.slice();
        var count = ps1.length;
        var matchFound = false;
        if (ps1.length = ps2.length) {
            for (var p1Index = 0; p1Index < ps1.length; p1Index++) {
                matchFound = false;
                for (var p2Index = 0; p2Index < ps2.length; p2Index++) {
                    if (math.areEpsilonEqual(ps1[p1Index], ps2[p2Index], refLength)) {
                        matchFound = true;
                        ps2.splice(p2Index, 1);
                        break;
                    }
                }
                if (!matchFound) { 
                    break;
                }
            }
        }
        return matchFound;
    };

    var areSegmentSetsEqual = function(ss1, ss2, refLength) {
        var ss2 = ss2.slice();
        var count = ss1.length;
        var matchFound = false;
        if (ss1.length = ss2.length) {
            for (var s1Index = 0; s1Index < ss1.length; s1Index++) {
                matchFound = false;
                for (var s2Index = 0; s2Index < ss2.length; s2Index++) {
                    if (math.areEpsilonEqual(ss1[s1Index].p1, ss2[s2Index].p1, refLength) &&
                            math.areEpsilonEqual(ss1[s1Index].p2, ss2[s2Index].p2, refLength) ||
                            math.areEpsilonEqual(ss1[s1Index].p1, ss2[s2Index].p2, refLength) &&
                            math.areEpsilonEqual(ss1[s1Index].p2, ss2[s2Index].p1, refLength)) {
                        matchFound = true;
                        ss2.splice(s2Index, 1);
                        break;
                    }
                }
                if (!matchFound) { 
                    break;
                }
            }
        }
        return matchFound;
    };
    
    var arePlanesEqual = function(ps1, ps2, refLength) {
        var n = math.cross(ps1[0], ps1[1], ps1[2]);
        for (var pIndex = 0; pIndex < ps2.length; pIndex++) {
            if (!math.isInPlane(ps2[pIndex], ps1[0], n)) {
                return false;
            }
        }
        return true;
    };
        
    var validatePoint = function(props, external) {
        var solid;
        if (!external) {
            var figure = figuresContainer.getSelectedFigure();
            solid = figure && figure.solid;
        }
        var ps = pointSetStringToCrds(props.value, solid);
        if (ps && ps.length == 1) {
            var crdsString = crdsToString(ps[0]);
            var outProps = { value: crdsString };
            return outProps;
        }
        return null;
    };
        
    var validatePointSet = function(props, external) {
        if (!props.value) {
            return null;
        }
        var outProps = lang.mixin({}, props);
        var figure;
        if (external) {
            figure = figuresContainer.getFigure(props.figureName);
        }
        else {
            figure = figuresContainer.getSelectedFigure();
        }
        var solid = figure && figure.solid;
        var ps = pointSetStringToCrds(props.value, solid);
        if (ps) {
            var crdsString = "";
            $.each(ps, function(pIndex) {
                crdsString += crdsToString(this);
                if (pIndex < ps.length - 1) {
                    crdsString += ",";
                }
            });
            outProps.value = crdsString;
            outProps.points = ps;
            return outProps;
        }
        return null;
    };
        
    var validateSegmentSet = function(props, external) {
        if (!props.value) {
            return null;
        }
        var outProps = lang.mixin({}, props);
        var figure;
        if (external) {
            figure = figuresContainer.getFigure(props.figureName);
        }
        else {
            figure = figuresContainer.getSelectedFigure();
        }
        var solid = figure && figure.solid;
        var ss = segmentSetStringToSegments(props.value, solid);
        if (ss) {
            outProps.segments = ss;
            return outProps;
        }
        return null;
    };

    var validateLine = function(props, external) {
        var figure;
        if (external) {
            figure = figuresContainer.getFigure(props.figureName);
        }
        else {
            figure = figuresContainer.getSelectedFigure();
        }
        var solid = figure && figure.solid;
        var ps = pointSetStringToCrds(props.value, solid);
        var refLength = solid.getRefLength();
        if (ps && ps.length == 2 && !math.areEpsilonEqual(ps[0], ps[1], refLength)) {
            var crdsString = "";
            $.each(ps, function(pIndex) {
                crdsString += crdsToString(this);
                if (pIndex < 1) {
                    crdsString += ",";
                }
            });
            var outProps = { value: crdsString };
            return outProps;
        }
        return null;
    };
    
    var validatePlane = function(props, external) {
        if (!props.value) {
            return null;
        }
        var outProps = lang.mixin({}, props);
        var figure;
        if (external) {
            figure = figuresContainer.getFigure(props.figureName);
        }
        else {
            figure = figuresContainer.getSelectedFigure();
        }
        var solid = figure && figure.solid;
        var ps = pointSetStringToCrds(props.value, solid);
        var refLength = solid && solid.getRefLength() || 1;
        if (ps && ps.length == 3 && !math.areCollinearPoints(ps, refLength)) {
            var crdsString = "";
            $.each(ps, function(pIndex) {
                crdsString += crdsToString(this);
                if (pIndex < 2) {
                    crdsString += ",";
                }
            });
            outProps.value = crdsString;
            outProps.points = ps;
            return outProps;
        }
        return null;
    };

    var pointFromSelection = function() {
        var value;
        var figure = figuresContainer.getSelectedFigure();
        if (utils.propCount(figure.solid.selection) == 1) {
            var element = utils.anyProp(figure.solid.selection);
            if (element instanceof GPoint) {
                value = element.label;
            }
        }
        return value;
    };

    var lineFromSelection = function() {
        var value;
        var figure = figuresContainer.getSelectedFigure();
        if (utils.propCount(figure.solid.selection) == 1) {
            var element = utils.anyProp(figure.solid.selection);
            if (!(element instanceof GPoint) && !(element instanceof GFace)) {
                value = element.p1.label + "," + element.p2.label;
            }
        }
        return value;
    };

    var pointSetFromSelection = function() {
        var value;
        var figure = figuresContainer.getSelectedFigure();
        if (figure) {
            var labels = [];
            $.each(figure.solid.selection, function(code, element) {
                if (element instanceof GPoint) {
                    labels.push(element.label);
                }
            });
            var pCount = labels.length;
            if (pCount > 0) {
                value = "";
                for (var pIndex = 0; pIndex < pCount; pIndex++) {
                    value += labels[pIndex];
                    if (pIndex < pCount - 1) {
                        value += ",";
                    }
                }
            }
        }
        return value;
    };

    var segmentSetFromSelection = function() {
        var value;
        var figure = figuresContainer.getSelectedFigure();
        if (figure) {
            var sticks = [];
            $.each(figure.solid.selection, function(code, element) {
                if (!(element instanceof GPoint) && !(element instanceof GFace)) {
                    sticks.push(element);
                }
            });
            var sCount = sticks.length;
            if (sCount > 0) {
                value = "";
                for (var sIndex = 0; sIndex < sCount; sIndex++) {
                    value += sticks[sIndex].p1.label;
                    value += sticks[sIndex].p2.label;
                    if (sIndex < sCount - 1) {
                        value += ",";
                    }
                }
            }
        }
        return value;
    };

    var planeFromSelection = function() {
        var value;
        var figure = figuresContainer.getSelectedFigure();
        if (figure) {
            var ps = {};
            $.each(figure.solid.selection, function(code, element) {
                if (element instanceof GPoint) {
                    ps[element.label] = element;
                }
                else if (element instanceof GFace) {
                    $.each([0, 1, 2], function(index) {
                        var label = element.labelAt(index);
                        ps[label] = figure.solid.points[label];
                    });
                }
                else {
                    ps[element.p1.label] = element.p1;
                    ps[element.p2.label] = element.p2;
                }
            });
            var crds = [];
            var labels = [];
            var refLength = figure.solid.getRefLength();
            var n;
            $.each(ps, function(label, p) {
                if (labels.length < 2) {
                    crds.push(p.crds);
                    labels.push(label);
                }
                else {
                    if (!math.areCollinearPoints([crds[0], crds[1], p.crds], refLength)) {
                        if (labels.length < 3) {
                            crds.push(p.crds);
                            labels.push(label);
                            var v1 = vec3.sub([], crds[1], crds[0]);
                            var v2 = vec3.sub([], crds[2], crds[0]);
                            n = vec3.cross([], v1, v2);
                            vec3.normalize(n, n);
                        }
                        else {
                            var v = vec3.sub([], p.crds, crds[0]);
                            if (Math.abs(vec3.dot(v, n)) > vec3.length(v)*math.EPSILON) {
                                labels = [];
                                return false;
                            }
                        }
                    }
                }
            });
            if (labels.length > 2) {
                value = "";
                for (var pIndex = 0; pIndex < 3; pIndex++) {
                    value += labels[pIndex];
                    if (pIndex < 2) {
                        value += ",";
                    }
                }
            }
        }
        return value;
    };
        
    return {

        pointSetStringToCrds: pointSetStringToCrds,
        segmentSetStringToSegments: segmentSetStringToSegments,
        crdsToString: crdsToString,
        arePointSetsEqual: arePointSetsEqual,
        areSegmentSetsEqual: areSegmentSetsEqual,
        arePlanesEqual: arePlanesEqual,
        validatePoint: validatePoint,
        validatePointSet: validatePointSet,
        validateSegmentSet: validateSegmentSet,
        validateLine: validateLine,
        validatePlane: validatePlane,
        pointFromSelection: pointFromSelection,
        lineFromSelection: lineFromSelection,
        pointSetFromSelection: pointSetFromSelection,
        segmentSetFromSelection: segmentSetFromSelection,
        planeFromSelection: planeFromSelection
    };
});
