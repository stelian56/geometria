/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "dojo/_base/lang",
    "dijit/MenuItem",
    "dijit/MenuSeparator",
    "geometria/GActions",
    "geometria/GFace",
    "geometria/GLine",
    "geometria/GMainContainer",
    "geometria/GMath",
    "geometria/GMenuBar",
    "geometria/GPoint",
    "geometria/GUtils"
    
], function(declare, lang, MenuItem, MenuSeparator,
            actions, GFace, GLine, mainContainer, math, menuBar, GPoint, utils) {

    var getCrds = function(points) {
        var ps = [];
        $.each(points, function(label, p) {
            ps.push(p.crds);
        });
        return ps;
    };

    var getFirstLength = function(points) {
        var p1, p2;
        $.each(points, function(label, p) {
            if (!p1) {
                p1 = p;
            }
            else {
                p2 = p;
                return false;
            }
        });
        return vec3.dist(p1.crds, p2.crds);
    };
    
    return declare(null, {
    
        constructor: function(points, faces) {
            this.id = utils.getUid();
            this.points = {};
            var solid = this;
            if (points) {
                $.each(points, function() {
                    solid.points[this.label] = this;
                });
            }
            this.faces = faces || [];
            this.stars = null;
            this.boundingSphere = null;
            this.gCenter = null;
            this.relatedSolids = {};
            this.relatedSolids[this.id] = this;
            this._sticks = null;
            this.selection = {};
        },

        clone: function() {
            var points = {};
            $.each(this.points, function(label, p) {
                points[label] = p.clone();
            });
            var faces = [];
            $.each(this.faces, function() {
                var face = this.clone();
                faces.push(face);
            });
            var solid = new this.constructor(points, faces);
            solid.gCenter = vec3.clone(this.gCenter);
            var bsCenter = vec3.clone(this.boundingSphere.center);
            solid.boundingSphere = { center: bsCenter, radius: this.boundingSphere.radius };
            solid.makeConfig();
            return solid;
        },
        
        make: function(props, ignoreInsidePoints) {
            var solid = this;
            
            var makePoints = function() {
                if (!props.points) {
                    throw "Solid has no points";
                }
                var points = {};
                if (!($.type(props.points) === "object")) {
                    throw "Bad format of 'points' property in solid";
                }
                if (utils.propCount(props.points) < 4) {
                    throw "Solid has fewer than 4 points";
                }
                $.each(props.points, function(label, crds) {
                    if (!utils.labelRegex.test(label)) {
                        throw "Bad point label " + label;
                    }
                    if (!math.validateCrds(crds)) {
                        throw "Bad point coordinates: " + crds;
                    }
                    $.each(props.points, function(l, cs) {
                        if (l != label && math.areEpsilonEqual(crds, cs, 1)) {
                            throw "Solid contains virtually indistiguishable points " + label +
                                "," + l;
                        }
                    });
                    points[label] = new GPoint(label, crds);
                });
                return points;
            };
        
            var makeLines = function() {
                var lines = [];
                if (props.lines) {
                    if (!Array.isArray(props.lines)) {
                        throw "Bad format of 'lines' property in solid";
                    }
                    $.each(props.lines, function() {
                        var line = new GLine().make(this);
                        lines.push(line);
                    });
                    lines.sort(function(lineA, lineB) {
                        return lineA.labels[0].localeCompare(lineB.labels[0])
                                    || lineA.labels[1].localeCompare(lineB.labels[1]);
                    });
                }
                return lines;
            };
        
            var buildFaces = function(points, lines) {

                var makeTriangle = function(ps, refPoint) {
                    var lines = [];
                    lines.push(new GLine([ps[0].label, ps[1].label]));
                    lines.push(new GLine([ps[1].label, ps[2].label]));
                    lines.push(new GLine([ps[2].label, ps[0].label]));
                    var face = new GFace(3, lines);
                    if (math.getOrientation(ps[0].crds, ps[1].crds, ps[2].crds, refPoint) > 0) {
                        face.reverse();
                    }
                    return face;
                };
                
                var addPoint = function(p, refPoint) {
                    front = {};
                    rear = [];
                    rim = [];
                    var frontNotEmpty;
                    $.each(solid.faces, function() {
                        var face = this;
                        var orientation = face.getOrientation(p.crds, solid);
                        if (orientation == 0) {
                            rim.push(face);
                        }
                        else if (orientation < 0) {
                            rear.push(face);
                        }
                        else {
                            front[face.code] = face;
                            frontNotEmpty = true;
                        }
                    });
                    if (!frontNotEmpty) {
                        return false;
                    }
                    var fs = [];
                    $.each(rim, function() {
                        var face = this;
                        face.addExternalPoint(p, solid);
                        fs.push(face);
                    });
                    $.each(rear, function() {
                        var face = this;
                        for (var lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                            var line = face.lines[lineIndex];
                            if (front[line.twin.face.code]) {
                                var ps = [ solid.points[line.labels[0]],
                                        solid.points[line.lastLabel()], p ];
                                var ff = makeTriangle(ps, refPoint);
                                fs.push(ff);
                            }
                        }
                        fs.push(face);
                    });
                    solid.faces = fs;
                    return true;
                };
                
                // Find vertices of a tetrahedron
                var refLength = getFirstLength(points);
                var toBeRemovedPoints = [];
                var v = vec3.create();
                $.each(points, function(label, p) {
                    var ps;
                    switch (utils.propCount(solid.points)) {
                    case 0:
                        solid.points[p.label] = p;
                        toBeRemovedPoints.push(p);
                        break;
                    case 1:
                        $.each(solid.points, function(label, sp) {
                            if (!math.areEpsilonEqual(sp.crds, p.crds, refLength)) {
                                solid.points[p.label] = p;
                                toBeRemovedPoints.push(p);
                            }
                        });
                        break;
                    case 2:
                        ps = getCrds(solid.points);
                        ps.push(p.crds);
                        if (!math.areCollinearPoints(ps, refLength)) {
                            solid.points[p.label] = p;
                            toBeRemovedPoints.push(p);
                        }
                        break;
                    case 3:
                        ps = getCrds(solid.points);
                        var n = math.cross(ps[0], ps[1], ps[2]);
                        vec3.normalize(n, n);
                        vec3.sub(v, p.crds, ps[0]);
                        if (Math.abs(vec3.dot(v, n)) >= refLength * math.EPSILON) {
                            solid.points[p.label] = p;
                            toBeRemovedPoints.push(p);
                        }
                    }
                });
                if (utils.propCount(solid.points) < 4) {
                    throw "Solid is virtually flat";
                }
                var ps = [];
                var refPoint = vec3.create();
                $.each(solid.points, function(label, sp) {
                    vec3.add(refPoint, refPoint, sp.crds);
                    ps.push(sp);
                });
                vec3.scale(refPoint, refPoint, 0.25);
                // Make faces of tetrahedron
                var face = makeTriangle([ ps[0], ps[1], ps[2] ], refPoint);
                solid.faces.push(face);
                face = makeTriangle([ ps[1], ps[2], ps[3] ], refPoint);
                solid.faces.push(face);
                face = makeTriangle([ ps[2], ps[3], ps[0] ], refPoint);
                solid.faces.push(face);
                face = makeTriangle([ ps[3], ps[0], ps[1] ], refPoint);
                solid.faces.push(face);
                solid.makeConfig();
                // Add new vertices
                $.each(points, function(label, p) {
                    if (!solid.points[p.label]) {
                        addPoint(p, refPoint);
                        solid.points[p.label] = p;
                        solid.makeConfig();
                    }
                });
                var vertexLabels = {};
                $.each(solid.faces, function() {
                    var face = this;
                    for (var lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                        var label = face.labelAt(lineIndex);;
                        vertexLabels[label] = 1;
                    }
                });
                var vertices = {};
                $.each(solid.points, function(label, p) {
                    if (vertexLabels[label]) {
                        vertices[label] = p;
                     }
                });
                solid.points = vertices;
                solid.makeConfig();
                // Add non-vertex points to faces' perimeters
                $.each(points, function(label, p) {
                    if (!solid.points[label]) {
                        var added;
                        $.each(solid.faces, function() {
                            if (this.addPoint(p, solid)) {
                                added = true;
                            }
                        });
                        if (added) {
                            solid.points[label] = p;
                        }
                    }
                });
                solid.makeConfig();
                // Add non-side lines to faces
                $.each(lines, function() {
                    var line = this;
                    var added;
                    $.each(solid.faces, function() {
                        var face = this;
                        var p1 = points[line.labels[0]];
                        if (!p1) {
                            throw "Line references non-existent point " + line.labels[0];
                        }
                        var p2 = points[line.labels[1]];
                        if (!p2) {
                            throw "Line references non-existent point " + line.labels[1];
                        }
                        if (face.covers(p1.crds, solid) && face.covers(p2.crds, solid)) {
                            if (!solid.points[p1.label]) {
                                solid.points[p1.label] = p1;
                            }
                            if (!solid.points[p2.label]) {
                                solid.points[p2.label] = p2;
                            }
                            var l = face.addLine(p1.label, p2.label, [], solid);
                            if (l) {
                                added = true;
                                return false;
                            }
                        }
                    });
                    if (!added) {
                        throw "Line " + line + " is contained in no face";
                    }
                });
                // Add remaining points to faces
                $.each(points, function(label, p) {
                    if (!solid.points[p.label]) {
                        var added;
                        $.each(solid.faces, function() {
                            if (this.addPoint(p, solid))
                                added = true;
                        });
                        if (!added && !ignoreInsidePoints) {
                            throw "Point " + p.label + " belongs to no face";
                        }
                        solid.points[p.label] = p;
                    }
                });
                solid.makeConfig();
            };
        
            var points = makePoints();
            var lines = makeLines();
            buildFaces(points, lines);
            solid.computeGCenter();
            solid.computeBoundingSphere();
            return solid;
        },

        makeConfig: function() {
            var solid = this;

            var isNeighbor = function(p, star) {
                var result;
                $.each(star.neighbors, function() {
                    if (this.label == p.label) {
                        result = true;
                        return false;
                    }
                });
                return result;
            };
        
            // Make stars
            solid.stars = {};
            $.each(solid.faces, function() {
                var face = this;
                for (var lineIndex = 0; lineIndex < face.lines.length; lineIndex++) {
                    var line = face.lines[lineIndex];
                    var labelIndex, star;
                    for (labelIndex = 0; labelIndex < line.labels.length - 1; labelIndex++) {
                        var p1 = solid.points[line.labels[labelIndex]];
                        var p2 = solid.points[line.labels[labelIndex + 1]];
                        star = solid.stars[p1.label];
                        if (!star) {
                            star = { owner: p1, neighbors: [] };
                            solid.stars[p1.label] = star;
                        }
                        if (!(isNeighbor(p2, star))) {
                            star.neighbors.push(p2);
                        }
                    }
                    for (labelIndex = line.labels.length - 1; labelIndex > 0; labelIndex--) {
                        var p1 = solid.points[line.labels[labelIndex]];
                        var p2 = solid.points[line.labels[labelIndex - 1]];
                        star = solid.stars[p1.label];
                        if (!star) {
                            star = { owner: p1, neighbors: [] };
                            solid.stars[p1.label] = star;
                        }
                        if (!(isNeighbor(p2, star))) {
                            star.neighbors.push(p2);
                        }
                    }
                }
            });
            $.each(solid.points, function(label, sp) {
                sp.resetLines();
            });
            // Match up line twins
            var edges = {};
            var fsThroughPoints = {};
            $.each(solid.faces, function() {
                var face = this;
                var fsThroughPoint;
                var lineIndex;
                for (lineIndex = 0; lineIndex < face.lines.length; lineIndex++) {
                    var line = face.lines[lineIndex];
                    line.face = face;
                    for (var labelIndex = 0; labelIndex < line.labels.length; labelIndex++) {
                        var p = solid.points[line.labels[labelIndex]];
                        p.lines.push(line);
                        fsThroughPoint = fsThroughPoints[p.label];
                        if (!fsThroughPoint) {
                            fsThroughPoint = {};
                            fsThroughPoints[p.label] = fsThroughPoint;
                        }
                        fsThroughPoint[face.code] = true;
                    }
                }
                for (lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                    var line = face.lines[lineIndex];
                    var label1 = line.labels[0];
                    var label2 = line.lastLabel();
                    var edges1 = edges[label1];
                    var edges2 = edges[label2];
                    var twin = edges1 && edges1[label2] || edges2 && edges2[label1];
                    if (!twin) {
                        if (!edges1) {
                            edges1 = edges[label1] = {};
                        }
                        edges1[label2] = line;
                    }
                    else {
                        line.twin = twin;
                        twin.twin = line;
                    }
                }
            });
            $.each(fsThroughPoints, function(label, fsThroughPoint) {
                solid.points[label].isVertex = utils.propCount(fsThroughPoint) > 2;
            });
        },

        computeGCenter: function() {
            var gCenter = vec3.create();
            var vertexCount = 0;
            $.each(this.points, function(label, p) {
                if (p.isVertex) {
                    vertexCount++;
                    vec3.add(gCenter, gCenter, p.crds);
                }
            });
            vec3.scale(gCenter, gCenter, 1.0 / vertexCount);
            this.gCenter = gCenter;
        },
        
        computeBoundingSphere: function() {
            var ps = [];
            $.each(this.points, function(label, p) {
                if (p.isVertex) {
                    ps.push(p.crds);
                }
            });
            this.boundingSphere = math.boundingSphere(ps);
        },
    
        addPoint: function(crds) {
            var label = utils.getNewLabel(this);
            p = new GPoint(label, crds);
            this.points[label] = p;
            return p;
        },
            
        computeVolume: function() {
            var solid = this;
            var volume = 0;
            $.each(this.faces, function() {
                var face = this;
                var p1 = solid.points[face.labelAt(0)].crds;
                var p2 = solid.points[face.labelAt(1)].crds;
                var p3 = solid.points[face.labelAt(2)].crds;
                volume +=
                    face.computeArea(solid) * math.distanceToPlane(solid.gCenter, p1, p2, p3);
            });
            return volume/3;
        },
   
        computeTotalArea: function() {
            var solid = this;
            var area = 0;
            $.each(this.faces, function() {
                area += this.computeArea(solid);
            });
            return area;
        },
   
        getRefLength: function() {
            return this.boundingSphere && this.boundingSphere.radius || getFirstLength(this.points);
        },

        pointAt: function(crds) {
            var solid = this;
            var refLength = solid.getRefLength();
            var p;
            $.each(solid.points, function(label, pp) {
                if (math.areEpsilonEqual(this.crds, crds, refLength)) {
                    p = pp;
                    return false;
                }
            });
            return p;
        },
        
        facesThroughPoints: function(labels) {
            if (!labels.length) {
                return {};
            }
            var faces = this.points[labels[0]].getFaces();
            var commonFaces;
            for (var labelIndex = 1; labelIndex < labels.length; labelIndex++) {
                var fs = this.points[labels[labelIndex]].getFaces();
                commonFaces = {};
                $.each(fs, function(code, face) {
                    if (faces[code]) {
                        commonFaces[code] = face;
                    }
                });
                if (!utils.anyProp(commonFaces)) {
                    return {};
                }
                faces = commonFaces;
            }
            return faces;
        },
        
        linesThroughPoints: function(label1, label2) {
            var p1 = this.points[label1];
            var p2 = this.points[label2];
            var lines = {};
            $.each(p1.lines, function() {
                var code = this.firstLabel() + this.lastLabel();
                lines[code] = this;
            });
            var commonLines = {};
            $.each(p2.lines, function() {
                var code = this.firstLabel() + this.lastLabel();
                if (lines[code]) {
                    commonLines[code] = this;
                }
            });
            return commonLines;
        },

        getFace: function(faceCode) {
            var face;
            $.each(this.faces, function() {
                if (this.getFaceCode() == faceCode) {
                    face = this;
                    return false;
                }
            });
            return face;
        },
        
        project: function(camera) {
            var boundingSphere = this.boundingSphere;
            $.each(this.points, function() {
                this.project(camera, boundingSphere.center);
            });
        },

        toScreen: function(scalingFactor, figureSize) {
            $.each(this.points, function(label, p) {
                p.toScreen(scalingFactor, figureSize);
            });
        },

        getScreenBounds: function() {
            var bounds = { left: Number.MAX_VALUE, right: -Number.MAX_VALUE,
                          top: Number.MAX_VALUE, bottom: -Number.MAX_VALUE };
            $.each(this.points, function(label, p) {
                bounds.left = Math.min(bounds.left, p.scrCrds[0]);
                bounds.top = Math.min(bounds.top, p.scrCrds[1]);
                bounds.right = Math.max(bounds.right, p.scrCrds[0]);
                bounds.bottom = Math.max(bounds.bottom, p.scrCrds[1]);
            });
            return bounds;
        },
        
        hideWireframe: function() {
            if (this._sticks) {
                $.each(this._sticks, function(stickCode, stick) {
                    stick.svg.style.display = "none";
                });
            }
            $.each(this.points, function(label, p) {
                p.hide();
            });
        },
        
        hideFaces: function() {
            $.each(this.faces, function() {
                this.hide();
            });
        },
   
        renamePoint: function(oldLabel, newLabel) {
            var p = this.points[oldLabel];
            p.label = newLabel;
            this.points[newLabel] = p;
            delete this.points[oldLabel];
            $.each(this.faces, function() {
                this.pointRenamed(oldLabel, newLabel);
            });
        },
   
        scale: function(label1, label2, factor) {
            var p1 = this.points[label1];
            var p2 = this.points[label2];
            var v = vec3.create();
            vec3.sub(v, p2.crds, p1.crds);
            $.each(this.points, function(label, p) {
                var scaledCrds = math.scale(p.crds, vec3.create(), v, factor);
                p.crds = scaledCrds;
            });
            this.computeGCenter();
            this.computeBoundingSphere();
        },
    
        shear: function(labels) {
            var solid = this;
            var ps = [];
            $.each(labels, function(index, label) {
                ps.push(solid.points[label].crds);
            });
            $.each(solid.points, function(label, p) {
                var shearedCrds = math.shear(p.crds, ps[0], ps[1], ps[2]);
                p.crds = shearedCrds;
            });
            this.computeGCenter();
            this.computeBoundingSphere();
        },

        // Assume n is of length 1. Cut this solid with plane (p0, n) and remove the fragment
        // to which vector n points. Return false if the plane does not intersect the solid's
        // interior.
        cutOff: function(p0, n) {
            var solid = this;
            var refLength = solid.getRefLength();
            var crdsInit;
            var valid = true;
            var face;
            $.each(solid.faces, function() {
                var f = this;
                var fInPlane = true;
                $.each([0, 1, 2], function(index) {
                    var label = f.labelAt(index);
                    var p = solid.points[label];
                    if (!math.isInPlane(p.crds, p0, n)) {
                        fInPlane = false;
                        return false;
                    }
                });
                if (fInPlane) {
                    // Face is contained in plane
                    valid = false;
                    return false;
                }
                if (!face) {
                    var crds = f.intersectPlane(p0, n, solid);
                    if (crds.length > 1) {
                        crdsInit = crds[0];
                        face = f;
                    }
                }
            });
            if (!valid) {
                return false;
            }
            if (!face) {
                // Plane does not intersect the solid's interior
                return false;
            }
            // Find a start point on the solid's wireframe
            var pInit = solid.pointAt(crdsInit);
            if (!pInit) {
                pInit = solid.addPoint(crdsInit);
                for (var lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                    var line = face.lines[lineIndex];
                    var p1 = solid.points[line.firstLabel()].crds;
                    var p2 = solid.points[line.lastLabel()].crds;
                    if (math.isBetween(crdsInit, p1, p2, refLength)) {
                        $.each([line, line.twin], function() {
                            this.face.addPoint(pInit, solid);
                            pInit.lines.push(this);
                        });
                        break;
                    }
                }
            }

            // Run along the section, map sectioned faces to section lines
            var pPrev = null;
            var pCurr = pInit;
            var pNext = null;
            var fCurr = null;
            var section = {};
            while (true) {
                var fs = solid.facesThroughPoints([pCurr.label]);
                var crds;
                $.each(fs, function() {
                    var f = this;
                    var ps = f.intersectPlane(p0, n, solid);
                    if (ps.length < 2) {
                        return;
                    }
                    var p1 = solid.pointAt(ps[0]);
                    var p2 = solid.pointAt(ps[1]);
                    if (p1 == pCurr) {
                        pNext = p2;
                        crds = ps[1];
                    }
                    else {
                        pNext = p1;
                        crds = ps[0];
                    }
                    if (pPrev && pNext == pPrev) {
                        return;
                    }
                    fCurr = f;
                    if (!pNext) {
                        pNext = solid.addPoint(crds);
                        for (var lineIndex = 0; lineIndex < fCurr.sideCount; lineIndex++) {
                            var line = fCurr.lines[lineIndex];
                            var pp1 = solid.points[line.firstLabel()];
                            var pp2 = solid.points[line.lastLabel()];
                            if (math.isStrictlyBetween(pNext.crds, pp1.crds,
                                    pp2.crds, refLength)) {
                                $.each([fCurr, line.twin.face], function() {
                                    this.addPoint(pNext, solid);
                                });
                                pNext.lines.push(line);
                                pNext.lines.push(line.twin);
                                break;
                            }
                        }
                    }
                    return false;
                });
                var line = fCurr.lineThroughPoints(pCurr.label, pNext.label);
                if (!line) {
                    var removedLines = [];
                    line = fCurr.addLine(pCurr.label, pNext.label, removedLines, solid);
                    $.each(removedLines, function() {
                        fCurr.removeLine(l, []);
                    });
                }
                var stickCode = line.getStickCode();
                section[stickCode] = { line: line };
                if (fCurr.lines.indexOf(line) >= fCurr.sideCount) {
                    section[stickCode].face = fCurr;
                }
                if (pNext == pInit) {
                    break;
                }
                pPrev = pCurr;
                pCurr = pNext;
            }

            // Collect faces located entirely on the wrong side of the section,
            // together with their vertices
            var toBeRemovedFaces = [];
            var toBeRemovedPoints = {}
            $.each(solid.faces, function() {
                var f = this;
                var fInSection = false;
                $.each(section, function() {
                    if (this.face == f) {
                        fInSection = true;
                    }
                });
                if (fInSection) {
                    return;
                }
                var v = f.computeGCenter(solid);
                vec3.sub(v, v, p0);
                if (vec3.dot(v, n) < 0) {
                    return;
                }
                toBeRemovedFaces.push(f);
                $.each(f.lines, function() {
                    var line = this;
                    $.each(line.labels, function(labelIndex, label) {
                        var p = solid.points[label];
                        if (!math.isInPlane(p.crds, p0, n)) {
                            toBeRemovedPoints[label] = p;
                        }
                    });
                });
            });

            // Clip sectioned faces. Add the new face to the end of the list
            var prevEndLabel;
            var ls = [];
            $.each(section, function(stickCode, lineFace) {
                var line = lineFace.line;
                var ff = lineFace.face;
                if (ff) {
                    var tbrPs = {};
                    ff.cutOff(line, n, tbrPs, solid);
                    lang.mixin(toBeRemovedPoints, tbrPs);
                }
                var l = line.clone();
                if (prevEndLabel && line.firstLabel() != prevEndLabel) {
                    l.reverse();
                }
                prevEndLabel = l.lastLabel();
                if (ff) {
                    l.twin = line;
                }
                else {
                    if (toBeRemovedFaces.indexOf(line.face) < 0) {
                        l.twin = line;
                    }
                    else {
                        l.twin = line.twin;
                    }
                }
                ls.push(l);
            });
            var newFace = new GFace(ls.length, ls);
            newFace.chainSides();
            var newFaceNormal = newFace.getNormal(solid);
            if (vec3.dot(newFaceNormal, n) < 0) {
                newFace.reverse();
            }
            $.each(toBeRemovedPoints, function(label) {
                delete solid.points[label];
            });
            $.each(toBeRemovedFaces, function() {
                utils.remove(solid.faces, this);
            });
            solid.faces.push(newFace);
            solid.makeConfig();
            solid.computeGCenter();
            solid.computeBoundingSphere();
            return newFace;
        },
    
        select: function(projX, projY, figure) {
            var solid = this;
            var epsilon = utils.selectEpsilon*Math.min(1, 1/figure.scalingFactor);
            var targetElement = null, code;
            if (figure.isWireframe()) {
                $.each(this.points, function() {
                    if (vec2.dist(this.projCrds, [projX, projY]) < epsilon) {
                        targetElement = this;
                        code = this.label;
                        return false;
                    }
                });
                if (!targetElement) {
                    $.each(this.faces, function() {
                        var line = this.getLineAt(projX, projY, epsilon, solid);
                        if (line) {
                            code = line.getStickCode();
                            targetElement = solid._sticks[code];
                            return false;
                        }
                    });
                }
            }
            else {
                $.each(solid.faces, function() {
                    var face = this;
                    if (figure.camera.isFaceVisible(face, solid, solid.gCenter)) {
                        targetElement = face.getElementAt(projX, projY, epsilon, solid);
                        if (targetElement) {
                            if (targetElement) {
                                if (targetElement instanceof GPoint) {
                                    code = targetElement.label;
                                }
                                else if (targetElement instanceof GLine) {
                                    code = targetElement.getStickCode();
                                    targetElement = solid._sticks[code];
                                }
                                else {
                                    code = targetElement.getFaceCode();
                                }
                            }
                            return false;
                        }
                    }
                });
            }
            if (targetElement) {
                if (solid.selection[code]) {
                    delete solid.selection[code];
                }
                else {
                    solid.selection[code] = targetElement;
                }
            }
        },
   
        selectAll: function() {
            var solid = this;
            solid.selection = {};
            $.each(solid.faces, function() {
                var face = this;
                $.each(face.lines, function() {
                    var line = this;
                    $.each(line.labels, function(index, label) {
                        var p = solid.points[label];
                        if (!p.isVertex) {
                            solid.selection[label] = p;
                        }
                    });
                });
                for (var lineIndex = face.sideCount; lineIndex < face.lines.length; lineIndex++) {
                    var line = face.lines[lineIndex];
                    code = line.getStickCode();
                    solid.selection[code] = solid._sticks[code];
                }
            });
        },
   
        updateContextMenu: function(contextMenu) {
            var addItem = function(action) {
                contextMenu.addChild(new MenuItem({
                    label: action.label,
                    iconClass: action.icon,
                    onClick: function() {
                        action.base.execute(true);
                    }
                }));
            };

            var solid = this;
            $.each(contextMenu.getChildren(), function() {
                contextMenu.removeChild(this);
            });
            var actionGroups = [];
            $.each([menuBar.actionGroups["View"], menuBar.actionGroups["Edit"],
                  menuBar.actionGroups["Draw"], menuBar.actionGroups["Measure"],
                  menuBar.actionGroups["Transform"]], function() {
                var group = [];
                $.each(this, function() {
                    if ("|" != this) {
                        var action = actions[this];
                        if (action.base.enabled && action.validateSelection
                                && action.validateSelection()) {
                            group.push(action);
                        }
                    }
                });
                actionGroups.push(group);
            });
            for (var groupIndex = 0; groupIndex < actionGroups.length; groupIndex++) {
                var group = actionGroups[groupIndex];
                $.each(group, function() {
                    addItem(this);
                });
                if (groupIndex < 4 && actionGroups[groupIndex + 1].length) {
                    contextMenu.addChild(new MenuSeparator());
                }
            }
        },

        initSvg: function(svg) {
            var solid = this;
            $.each(solid.faces, function() {
                this.initSvg(svg);
            });
            solid._sticks = {};
            $.each(solid.faces, function() {
                var face = this;
                $.each(face.lines, function() {
                    var line = this;
                    var stickCode = line.getStickCode();
                    var stick = solid._sticks[stickCode];
                    if (!stick) {
                        var p1 = solid.points[line.labels[0]];
                        var p2 = solid.points[line.lastLabel()];
                        var stickSvg =
                            document.createElementNS("http://www.w3.org/2000/svg", "line");
                        $(stickSvg).appendTo(svg);
                        solid._sticks[stickCode] =
                            {faces: [face], p1: p1, p2: p2, svg: stickSvg};
                    }
                    else {
                        stick.faces.push(face);
                    }
                });
            });
            $.each(solid.points, function() {
                this.initSvg(svg);
            });
        },
   
        draw: function(camera, svg, scalingFactor, size, labeled, baseColor) {
            var solid = this;
            solid.project(camera);
            solid.toScreen(scalingFactor, size);
            if (baseColor) {
                // Paint faces
                $.each(solid.faces, function() {
                    this.paint(baseColor, camera, solid);
                });
            }
            // Draw sticks
            $.each(solid._sticks, function(stickCode, stick) {
                var hidden = true;
                var selected = solid.selection[stickCode] && true;
                if (!baseColor || selected) {
                    var visible = camera.isFaceVisible(stick.faces[0], solid, solid.gCenter);
                    if (!visible && stick.faces.length > 1) {
                        visible = camera.isFaceVisible(stick.faces[1], solid, solid.gCenter);
                    }
                    if (!baseColor || visible) {
                        hidden = false;
                        $(stick.svg).attr({
                            x1: stick.p1.scrCrds[0],
                            y1: stick.p1.scrCrds[1],
                            x2: stick.p2.scrCrds[0],
                            y2: stick.p2.scrCrds[1],
                            stroke: selected ? utils.selectionColor: utils.stickColor,
                            "stroke-width": selected ? utils.selectionStrokeWidth: 1,
                            "stroke-dasharray": !visible && !selected ? utils.strokeDashArray : ""
                        });
                        stick.svg.style.display = "";
                    }
                }
                if (hidden) {
                    stick.svg.style.display = "none";
                }
            });
            // Draw points
            if (!baseColor) {
                $.each(solid.points, function(label, p) {
                    var selected = solid.selection[label] && true;
                    var label = p.label;
                    var star;
                    if (labeled) {
                        star = solid.stars[label];
                    }
                    p.draw(!baseColor && true, star, selected);
                });
            }
        },

        toJson: function() {
            var pointProps = {};
            $.each(this.points, function(label, p) {
                var crds = p.crds;
                pointProps[label] = [crds[0], crds[1], crds[2]];
            });
            var solidProps = {
                "points": pointProps
            };
            var lineProps = [];
            $.each(this.faces, function() {
                var face = this;
                for (var lineIndex = face.sideCount; lineIndex < face.lines.length; lineIndex++) {
                    var line = face.lines[lineIndex];
                    lineProps.push(line.toJson());
                }
            });
            if (lineProps.length) {
                lang.mixin(solidProps, {
                    "lines": lineProps
                });
            }
            var json = { "solid": solidProps };
            return json;
        },
        
        toString: function() {
            var stringValue = "";
            $.each(this.points, function(label, p) {
                if (p.isVertex) {
                    stringValue += label;
                }
            });
            return "[" + stringValue + "]";
        }
    });
});