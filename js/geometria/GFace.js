/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "geometria/GLine",
    "geometria/GMath",
    "geometria/GUtils"
], function(declare, GLine, math, utils) {

    var CONTRAST = 0.9;
    var code = 0;

    return declare(null, {
    
        constructor: function(sideCount, lines) {
            this.sideCount = sideCount;
            this.lines = lines;
            this._polygonSvg = null;
            this.code = code++;
        },
        
        clone: function() {
            var lines = [];
            $.each(this.lines, function() {
                var line = this.clone();
                lines.push(line);
            });
            var face = new this.constructor(this.sideCount, lines);
            return face;
        },
        
        labelAt: function(index) {
            return this.lines[index].labels[0];
        },
        
        pointRenamed: function(oldLabel, newLabel) {
            $.each(this.lines, function() {
                this.pointRenamed(oldLabel, newLabel);
            });
        },
        
        removePoint: function(label) {
            $.each(this.lines, function() {
                utils.remove(this.labels, label);
            });
        },
        
        reverse: function() {
            ls = [];
            var lineIndex;
            for (lineIndex = this.sideCount - 1; lineIndex >= 0; lineIndex--) {
                var line = this.lines[lineIndex];
                line.reverse();
                ls.push(line);
            }
            for (lineIndex = this.sideCount; lineIndex < this.lines.length; lineIndex++) {
                var line = this.lines[lineIndex];
                ls.push(line);
            }
            this.lines = ls;
        },
        
        startAt: function(startIndex) {
            var ls = [];
            var lineIndex;
            for (lineIndex = 0; lineIndex < this.sideCount; lineIndex++) {
                var line = this.lines[(lineIndex + startIndex) % this.sideCount];
                ls.push(line);
            }
            for (lineIndex = this.sideCount; lineIndex < this.lines.length; lineIndex++) {
                var line = this.lines[lineIndex];
                ls.push(line);
            }
            this.lines = ls;
        },
    
        getOrientation: function(refPoint, solid) {
            var p0 = solid.points[this.lines[0].labels[0]].crds;
            var p1 = solid.points[this.lines[1].labels[0]].crds;
            var p2 = solid.points[this.lines[2].labels[0]].crds;
            return math.getOrientation(p0, p1, p2, refPoint);
        },
        
        getNormal: function(solid) {
            var p1 = solid.points[this.labelAt(0)].crds;
            var p2 = solid.points[this.labelAt(1)].crds;
            var p3 = solid.points[this.labelAt(2)].crds;
            var edge1 = vec3.sub([], p2, p1);
            var edge2 = vec3.sub([], p3, p2);
            var normal = vec3.cross([], edge1, edge2);
            vec3.normalize(normal, normal);
            return normal;
        },

        getRefNormal: function(solid, refPoint) {
            var normal = this.getNormal(solid);
            var p1 = solid.points[this.labelAt(0)].crds;
            var toOutside = vec3.sub([], p1, refPoint);
            if (vec3.dot(normal, toOutside) < 0) {
                vec3.scale(normal, normal, -1);
            }
            return normal;
        },

        getLine: function(stickCode) {
            var line;
            $.each(this.lines, function() {
                if (this.getStickCode() == stickCode) {
                    line = this;
                    return false;
                }
            });
            return line;
        },
        
        computeArea: function(solid) {
            var area = 0;
            var p0 = solid.points[this.labelAt(0)].crds;
            for (var lineIndex = 1; lineIndex < this.sideCount - 1; lineIndex++) {
                var p1 = solid.points[this.labelAt(lineIndex)].crds;
                var p2 = solid.points[this.labelAt(lineIndex + 1)].crds;
                area += math.area(p0, p1, p2);
            }
            return area;
        },
        
        getLineAt: function(projX, projY, epsilon, solid) {
            var line = null;
            $.each(this.lines, function() {
                var p1 = solid.points[this.firstLabel()].projCrds;
                var p2 = solid.points[this.lastLabel()].projCrds;
                var dist = math.distanceToSegment([projX, projY], p1, p2);
                if (dist < epsilon) {
                    line = this;
                    return false;
                }
            });
            return line;
        },

        getElementAt: function(projX, projY, epsilon, solid) {
            var line = this.getLineAt(projX, projY, epsilon, solid);
            if (line) {
                var p1 = solid.points[line.firstLabel()];
                var p2 = solid.points[line.lastLabel()];
                var p = vec2.fromValues(projX, projY);
                if (vec2.distance(p1.projCrds, p) < epsilon) {
                    return p1;
                }
                if (vec2.distance(p2.projCrds, p) < epsilon) {
                    return p2;
                }
                return line;
            }
            var firstDet;
            for (var lineIndex = 0; lineIndex < this.sideCount; lineIndex++) {
                var line = this.lines[lineIndex];
                var p1 = solid.points[line.firstLabel()].projCrds;
                var p2 = solid.points[line.lastLabel()].projCrds;
                var det = (projX - p1[0])*(projY - p2[1]) - (projX - p2[0])*(projY - p1[1]);
                if (!firstDet) {
                    firstDet = det;
                }
                else if (firstDet*det <= 0) {
                    return null;
                }
            }
            return this;
        },
        
        addPoint: function(p, solid) {
            var added;
            $.each(this.lines, function() {
                added = this.acquirePoint(p, solid) || added;
            });
            return added;
        },

        addExternalPoint: function(p, solid) {
            var front = [];
            var rear = [];
            var rim = [];
            var refLength = solid.getRefLength();
            var n = this.getNormal(solid);
            $.each(this.lines, function() {
                var line = this;
                var p1 = solid.points[line.labels[0]];
                var p2 = solid.points[line.labels[line.labels.length - 1]];
                if (math.areCollinearPoints([p.crds, p1.crds, p2.crds], refLength)) {
                    rim.push(line);
                }
                else {
                    var orientation = math.getTriOrientation(p.crds, p1.crds, p2.crds, n);
                    if (orientation < 0) {
                        front.push(line);
                    }
                    else if (orientation > 0) {
                        rear.push(line);
                    }
                }
            });
            var startIndex = 0;
            var lineIndex;
            for (lineIndex = 1; lineIndex < rear.length; lineIndex++) {
                var line = rear[lineIndex - 1];
                var l = rear[lineIndex];
                if (line.labels[line.labels.length - 1] != l.labels[0]) {
                    startIndex = lineIndex;
                    break;
                }
            }
            var ls = [];
            for (lineIndex = 0; lineIndex < rear.length; lineIndex++) {
                var line = rear[(startIndex + lineIndex) % rear.length];
                ls.push(line);
            }
            var line1 = new GLine([p.label, ls[0].labels[0]]);
            var labels = ls[ls.length - 1].labels;
            var line2 = new GLine([labels[labels.length - 1], p.label]);
            ls.splice(0, 0, line1);
            ls.push(line2);
            this.lines = ls;
            this.sideCount = ls.length;
        },
        
        covers: function(p, solid) {
            // Check if p belongs to the perimeter
            var lineIndex;
            for (lineIndex = 0; lineIndex < this.sideCount; lineIndex++) {
                var labels = this.lines[lineIndex].labels;
                if (math.isBetween(p, solid.points[labels[0]].crds,
                        solid.points[labels[labels.length - 1]].crds)) {
                    return true;
                }
            }
            // Check if p belongs to the interior of this face
            var v1 = math.cross(p, solid.points[this.labelAt(0)].crds,
                                solid.points[this.labelAt(1)].crds);
            vec3.normalize(v1, v1);
            for (var lineIndex = 1; lineIndex < this.sideCount; lineIndex++) {
                var line = this.lines[lineIndex];
                var vi = math.cross(p, solid.points[line.labels[0]].crds,
                                solid.points[line.labels[line.labels.length - 1]].crds);
                vec3.normalize(vi, vi);
                if (Math.abs(vec3.dot(vi, v1) - 1) > math.EPSILON) {
                    return false;
                }
            }
            return true;
        },

        addLine: function(label1, label2, removedLines, solid) {
            var lineIndex = 0, l;
            for (lineIndex = 0; lineIndex < this.sideCount; lineIndex++) {
                l = this.lines[lineIndex];
                if (l.contains(label1) && l.contains(label2)) {
                    return null;
                }
            }
            var line = new GLine([label1, label2]);
            var removedLs = [];
            for (lineIndex = this.lines.length - 1; lineIndex >= this.sideCount; lineIndex--) {
                l = this.lines[lineIndex];
                if (line.acquireLine(l, solid)) {
                    removedLs.push(l);
                    removedLines.push(l.toJson());
                }
            }
            this.lines = utils.removeAll(this.lines, removedLs);
            for (lineIndex = this.sideCount; lineIndex < this.lines.length; lineIndex++) {
                l = this.lines[lineIndex];
                for (var labelIndex = 0; labelIndex < l.labels.length; labelIndex++) {
                    var p = solid.points[l.labels[labelIndex]];
                    line.acquirePoint(p, solid);
                }
            }
            this.lines.push(line);
            return line;
        },

        undoAddLine: function(addedLine, removedLines) {
            var face = this;
            var line = face.getLine(addedLine);
            utils.remove(face.lines, line);
            $.each(removedLines, function() {
                var labels = this;
                var line = new GLine(labels);
                face.lines.push(line);
            });
        },

        undoAddLines: function(addedLines, removedLines) {
            var face = this;
            $.each(addedLines, function(index, stickCode) {
                var line = face.getLine(stickCode);
                utils.remove(face.lines, line);
            });
            $.each(removedLines, function() {
                var labels = this;
                var line = new GLine(labels);
                face.lines.push(line);
            });
        },
        
        lineThroughPoints: function(label1, label2) {
            var line;
            $.each(this.lines, function() {
                if (this.contains(label1) && this.contains(label2)) {
                    line = this;
                    return false;
                }
            });
            return line;
        },
        
        linesThroughPoint: function(label) {
            var lines = [];
            $.each(this.lines, function() {
                if (this.contains(label)) {
                    lines.push(this);
                }
            });
            return lines;
        },

        contains: function(label) {
            var labelFound = false;
            $.each(this.lines, function() {
                if (this.contains(label)) {
                    labelFound = true;
                    return false;
                }
            });
            return labelFound;
        },
        
        removeLineThroughPoints: function(label1, label2, addedLines, removedLabels) {
            var face = this;
            var line = face.lineThroughPoints(label1, label2);
            var indexes = [];
            var labelIndex;
            for (labelIndex = 0; labelIndex < line.labels.length; labelIndex++) {
                var label = line.labels[labelIndex];
                if (label == label1 || label == label2) {
                    indexes.push(labelIndex);
                }
                if (indexes.length == 2) {
                    break;
                }
            }
            if (indexes[0] > 0) {
                var l = new GLine(line.labels.slice(0, indexes[0] + 1));
                addedLines.push(l.toJson());
                face.lines.push(l);
            }
            if (indexes[1] < line.labels.length - 1) {
                var l = new GLine(line.labels.slice(indexes[1]));
                addedLines.push(l.toJson());
                face.lines.push(l);
            }
            for (labelIndex = indexes[0]; labelIndex <= indexes[1]; labelIndex++) {
                var label = line.labels[labelIndex];
                if (face.linesThroughPoint(label).length < 2)
                    removedLabels.push(label);
            }
            utils.remove(face.lines, line);
            return line.labels;
        },

        undoRemoveLine: function(removedLine, addedLines) {
            var face = this;
            $.each(addedLines, function() {
                var labels = this;
                line = face.lineThroughPoints(labels[0], labels[1]);
                utils.remove(face.lines, line);
            });
            var line = new GLine(removedLine);
            face.lines.push(line);
        },

        removeLine: function(line, danglingLabels) {
            var face = this;
            $.each(line.labels, function(index, label) {
                if (face.linesThroughPoint(label).length < 2) {
                    danglingLabels.push(label);
                }
            });
            utils.remove(this.lines, line);
        },

        // Assume v has positive length, p is covered by this face and (p, v) is looking
        // to the interior of this face. Return:
        // 1. The line intersected by ray (p, v), any of them if there are two.
        // 2. The intersection point.
        intersectRay: function(p, v, solid) {
            var refLength = solid.getRefLength();
            var pv = vec3.create();
            vec3.add(pv, p, v);
            var v3 = vec3.create();
            for (var lineIndex = 0; lineIndex < this.sideCount; lineIndex++) {
                var line = this.lines[lineIndex];
                var p1 = solid.points[line.firstLabel()].crds;
                var p2 = solid.points[line.lastLabel()].crds;
                var p3 = math.intersect(p, pv, p1, p2, refLength);
                if (p3 && math.isBetween(p3, p1, p2, refLength)) {
                    vec3.sub(v3, p3, p);
                    if (vec3.length(v3) > refLength*math.EPSILON &&
                            math.areCooriented(v3, v, refLength)) {
                        return { line: line, point: p3 };
                    }
                }
            }
            return null;
        },
        
        // Assume:
        // 1. v has positive length
        // 2. This face is not contained in plane (p, v).
        // Return the end points of this face's intersection with plane (p, v).
        // If the intersection is a point or less, return an empty array.
        intersectPlane: function(p, n, solid) {
            var face = this;
            var ps = [];
            for (var lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                var line = face.lines[lineIndex];
                var p1 = solid.points[line.firstLabel()].crds;
                if (math.isInPlane(p1, p, n)) {
                    ps.push(p1);
                }
            }
            if (ps.length > 1) {
                return ps;
            }
            for (var lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                var line = face.lines[lineIndex];
                var p1 = solid.points[line.firstLabel()].crds;
                if (ps.indexOf(p1) < 0) {
                    var p2 = solid.points[line.lastLabel()].crds;
                    if (ps.indexOf(p2) < 0) {
                        var p3 = math.intersectPlane(p1, p2, p, n);
                        if (p3) {
                            ps.push(p3);
                            if (ps.length > 1) {
                                return ps;
                            }
                        }
                    }
                }
            }
            return ps;
        },
    
        // Assume 'line' is contained inside this face. Remove from this face the part boundered
        // by 'line' and pointed to n
        cutOff: function(line, n, toBeRemovedPoints, solid) {
            var face = this;
            var refLength = solid.getRefLength();
            var crdsLine1 = solid.points[line.firstLabel()].crds;
            var crdsLine2 = solid.points[line.lastLabel()].crds;
            var ls = [];
            var lineIndex, labelIndex;
            var v = vec3.create();
            for (lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                var l = face.lines[lineIndex];
                var crdsL1 = solid.points[l.firstLabel()].crds;
                var crdsL2 = solid.points[l.lastLabel()].crds;
                var crds = math.intersect(crdsL1, crdsL2, crdsLine1, crdsLine2, refLength);
                var pIndex = -1;
                if (crds) {
                    if (l.firstLabel() == line.firstLabel() || l.firstLabel() == line.lastLabel()) {
                        pIndex = 0;
                    }
                    else if (l.lastLabel() == line.firstLabel() ||
                            l.lastLabel() == line.lastLabel()) {
                        pIndex = l.labels.length - 1;
                    }
                    else {
                        if (math.isBetween(crds, crdsL1, crdsL2, refLength)) {
                            $.each(l.labels, function(labelIndex, label) {
                                if (math.areEpsilonEqual(solid.points[label].crds,
                                        crds, refLength)) {
                                    pIndex = labelIndex;
                                    return false;
                                }
                            });
                        }
                    }
                }
                if (pIndex < 0) {
                    vec3.sub(v, crdsL1, crdsLine1);
                    if (vec3.dot(v, n) > 0) {
                        for (labelIndex = 0; labelIndex < l.labels.length - 1; labelIndex++) {
                            var label = l.labels[labelIndex];
                            toBeRemovedPoints[label] = solid.points[label];
                        }
                    }
                    else {
                        var ll = l.clone();
                        ls.push(ll);
                    }
                }
                else {
                    vec3.sub(v, crdsL2, crdsL1);
                    if (vec3.dot(v, n) > 0) {
                        for (labelIndex = pIndex + 1; labelIndex < l.labels.length;
                                labelIndex++) {
                            var label = l.labels[labelIndex];
                            toBeRemovedPoints[label] = solid.points[label];
                        }
                        if (pIndex > 0) {
                            var ll = new GLine(l.labels.slice(0, pIndex + 1));
                            ls.push(ll);
                        }
                        if (pIndex > 0) {
                            ls.push(line);
                        }
                    }
                    else {
                        for (labelIndex = 0; labelIndex < pIndex; labelIndex++) {
                            var label = l.labels[labelIndex];
                            toBeRemovedPoints[label] = solid.points[label];
                        }
                        if (pIndex < l.labels.length - 1) {
                            var ll = new GLine(l.labels.slice(pIndex));
                            ls.push(ll);
                        }
                    }
                }
            }
            var sc = ls.length;

            // Intersect 'line' with interior lines
            for (lineIndex = face.sideCount; lineIndex < face.lines.length; lineIndex++) {
                var l = face.lines[lineIndex];
                if (l != line) {
                    var crdsL1 = solid.points[l.firstLabel()].crds;
                    var crdsL2 = solid.points[l.lastLabel()].crds;
                    var crds = math.intersect(crdsLine1, crdsLine2, crdsL1, crdsL2, refLength);
                    if (crds && math.isStrictlyBetween(crds, crdsLine1, crdsLine2, refLength) &&
                            math.isBetween(crds, crdsL1, crdsL2, refLength)) {
                        var p = solid.pointAt(crds);
                        if (!p) {
                            p = solid.addPoint(crds);
                        }
                        if (!line.contains(p.label)) {
                            line.insert(p, solid);
                        }
                        if (!l.contains(p.label)) {
                            l.insert(p, solid);
                        }
                    }
                }
            }

            // Clip interior lines
            for (lineIndex = face.sideCount; lineIndex < face.lines.length; lineIndex++) {
                var l = face.lines[lineIndex];
                if (l != line) {
                    var crdsL1 = solid.points[l.firstLabel()].crds;
                    var crdsL2 = solid.points[l.lastLabel()].crds;
                    var indexLine = -1;
                    var indexL = -1;
                    for (labelIndex = 0; labelIndex < l.labels.length; labelIndex++) {
                        indexLine = line.labels.indexOf(l.labels[labelIndex]);
                        if (indexLine >= 0) {
                            indexL = labelIndex;
                            break;
                        }
                    }
                    if (indexL >= 0) {
                        vec3.sub(v, crdsL2, crdsL1);
                        if (vec3.dot(v, n) > 0) {
                            for (labelIndex = indexL + 1; labelIndex < l.labels.length;
                                    labelIndex++) {
                                var label = l.labels[labelIndex];
                                var p = solid.points[label];
                                toBeRemovedPoints[label] = p;
                            }
                            if (indexL > 0) {
                                var ll = new GLine(l.labels.slice(0, indexL + 1));
                                ls.push(ll);
                            }
                        }
                        else {
                            for (labelIndex = 0; labelIndex < indexL; labelIndex++) {
                                var label = l.labels[labelIndex];
                                var p = solid.points[label];
                                toBeRemovedPoints[label] = p;
                            }
                            if (indexL < l.labels.length - 1) {
                                var ll = new GLine(l.labels.slice(indexL));
                                ls.push(ll);
                            }
                        }
                    }
                    else {
                        vec3.sub(v, crdsL1, crdsLine1);
                        if (vec3.dot(v, n) > 0) {
                            for (labelIndex = 0; labelIndex < l.labels.length; labelIndex++) {
                                var label = l.labels[labelIndex];
                                var p = solid.points[labelIndex];
                                toBeRemovedPoints[labelIndex] = p;
                            }
                        }
                        else {
                            var ll = l.clone();
                            ls.push(ll);
                        }
                    }
                }
            }
            face.lines = ls;
            face.sideCount = sc;
            face.chainSides();
        },
    
        chainSides: function() {
            var face = this;
            var ls = [];
            var line = face.lines[0];
            var nextLine = face.lines[1];
            if (line.lastLabel() != nextLine.firstLabel() &&
                    line.lastLabel() != nextLine.lastLabel()) {
                line.reverse();
            }
            ls.push(line);
            var lineIndex;
            for (lineIndex = 1; lineIndex < face.sideCount; lineIndex++) {
                nextLine = face.lines[lineIndex];
                if (nextLine.firstLabel() != line.lastLabel()) {
                    nextLine.reverse();
                }
                ls.push(nextLine);
                line = nextLine;
            }
            for (lineIndex = face.sideCount; lineIndex < face.lines.length; lineIndex++) {
                line = face.lines[lineIndex];
                ls.push(line);
            }
            face.lines = ls;
        },
    
        computeGCenter: function(solid) {
            var gCenter = vec3.create();
            for (var lineIndex = 0; lineIndex < this.sideCount; lineIndex++) {
                var line = this.lines[lineIndex];
                var crds = solid.points[line.firstLabel()].crds;
                vec3.add(gCenter, gCenter, crds);
            }
            vec3.scale(gCenter, gCenter, 1/this.sideCount);
            return gCenter;
        },
    
        getFaceCode: function() {
            return this.labelAt(0) + this.labelAt(1) + this.labelAt(2);
        },
        
        isIsoscellesTriangle: function(solid) {
            if (this.sideCount != 3) {
                return false;
            }
            var vs = [];
            for (lineIndex = 0; lineIndex < 3; lineIndex++) {
                var v = this.lines[lineIndex].toVector(solid);
                vs.push(v);
            }
            return Math.abs(vec3.length(vs[0])/vec3.length(vs[1]) - 1) < math.EPSILON
                || Math.abs(vec3.length(vs[1])/vec3.length(vs[2]) - 1) < math.EPSILON
                    || Math.abs(vec3.length(vs[0])/vec3.length(vs[2]) - 1) < math.EPSILON;
        },
    
        isRightTriangle: function(solid) {
            if (this.sideCount != 3) {
                return false;
            }
            var vs = [];
            for (lineIndex = 0; lineIndex < 3; lineIndex++) {
                var v = this.lines[lineIndex].toVector(solid);
                vec3.normalize(v, v);
                vs.push(v);
            }
            return Math.abs(vec3.dot(vs[0], vs[1])) < math.EPSILON
                || Math.abs(vec3.dot(vs[1], vs[2])) < math.EPSILON
                    || Math.abs(vec3.dot(vs[0], vs[2])) < math.EPSILON;
        },

        isEquilateralTriangle: function(solid) {
            if (this.sideCount != 3) {
                return false;
            }
            var vs = [];
            for (lineIndex = 0; lineIndex < 3; lineIndex++) {
                var v = this.lines[lineIndex].toVector(solid);
                vs.push(v);
            }
            return Math.abs(vec3.length(vs[0])/vec3.length(vs[1]) - 1) < math.EPSILON
                && Math.abs(vec3.length(vs[1])/vec3.length(vs[2]) - 1) < math.EPSILON;
        },
        
        isParallelogram: function(solid) {
            if (this.sideCount != 4) {
                return false;
            }
            var v1 = this.lines[0].toVector(solid);
            var v2 = this.lines[2].toVector(solid);
            vec3.add(v2, v1, v2);
            return vec3.length(v2) < solid.getRefLength()*math.EPSILON;
        },

        isRhombus: function(solid) {
            if (!this.isParallelogram(solid)) {
                return false;
            }
            var v1 = this.lines[0].toVector(solid);
            var v2 = this.lines[1].toVector(solid);
            return Math.abs(vec3.length(v1)/vec3.length(v2) - 1) < math.EPSILON;
        },

        isRectangle: function(solid) {
            if (!this.isParallelogram(solid)) {
                return false;
            }
            var v1 = this.lines[0].toVector(solid);
            vec3.normalize(v1, v1);
            var v2 = this.lines[1].toVector(solid);
            vec3.normalize(v1, v1);
            return Math.abs(vec3.dot(v1, v2)) < math.EPSILON;
        },

        isSquare: function(solid) {
            return this.isRectangle(solid) && this.isRhombus(solid);
        },
        
        hide: function() {
            if (this._polygonSvg) {
                this._polygonSvg.style.display = "none";
            }
        },

        initSvg: function(svg) {
            this._polygonSvg = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
            $(this._polygonSvg).appendTo(svg);
        },
        
        paint: function(baseColor, camera, solid) {
            if (camera.isFaceVisible(this, solid, solid.gCenter)) {
                var pointsAttr = "";
                var lineIndex;
                for (lineIndex = 0; lineIndex < this.sideCount; lineIndex++) {
                    var label = this.labelAt(lineIndex);
                    var p = solid.points[label].scrCrds;
                    pointsAttr += p[0] + "," + p[1];
                    if (lineIndex < this.sideCount - 1) {
                        pointsAttr += " ";
                    }
                }
                var faceCode = this.getFaceCode();
                if (solid.selection[faceCode]) {
                    color = utils.selectionColor;
                }
                else {
                    var on = vec3.create();
                    vec3.transformMat3(on, this.getRefNormal(solid, solid.gCenter),
                        camera.attitude);
                    var hsb = utils.colorCssToHsb(baseColor);
                    var light = vec3.fromValues(-1, 1, 0);
                    vec3.normalize(light, light);
                    var brightness = 0.5*(vec3.dot(on, light) + 1);
                    brightness = CONTRAST*brightness + 1 - CONTRAST;
                    hsb[2] = brightness;
                    color = utils.colorHsbToCss(hsb);
                }
                $(this._polygonSvg).attr({
                    points: pointsAttr,
                    fill: color,
                    stroke: color
                });
                $.each(this.lines, function() {
                    var line = this;
                    $.each(line.labels, function(index, label) {
                        var p = solid.points[label];
                        if (solid.selection[label]) {
                            p.draw(false, null, true);
                        }
                        else {
                            p.draw(false, null, false);
                        }
                    });
                });
                this._polygonSvg.style.display = "";
            }
            else {
                this._polygonSvg.style.display = "none";
            }
        },
        
        toString: function() {
            return this.lines[0].labels[0] + this.lines[1].labels[0] +
                    this.lines[2].labels[0];
        }
    });
});
