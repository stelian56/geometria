/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "geometria/GMath"
], function(declare, math) {

    return declare(null, {
    
        constructor: function(labels) {
            if (labels) {
                this.labels = labels.slice(0);
            }
            this.face = null;
            this.twin = null;
        },

        clone: function() {
            var labels = this.labels.slice();
            var line = new this.constructor(labels);
            return line;
        },
        
        make: function(props) {
            if (!Array.isArray(props) || props.length != 2 || props[0] == props[1]) {
                throw "Bad line " + props;
            }
            this.labels = props.slice(0);
            return this;
        },

        firstLabel: function() {
            return this.labels[0];
        },
        
        lastLabel: function() {
            return this.labels[this.labels.length - 1];
        },

        contains: function(label) {
            return this.labels.indexOf(label) > -1;
        },
        
        pointAt: function(crds, solid) {
            var refLength = solid.getRefLength();
            var p;
            $.each(this.labels, function(index, label) {
                var pp = solid.points[label];
                if (math.areEpsilonEqual(pp.crds, crds, refLength)) {
                    p = pp;
                    return false;
                }
            });
            return p;
        },
        
        pointRenamed: function(oldLabel, newLabel) {
            for (var labelIndex = 0; labelIndex < this.labels.length; labelIndex++) {
                if (this.labels[labelIndex] == oldLabel) {
                    this.labels.splice(labelIndex, 1, newLabel);
                    return;
                }
            }
        },

        reverse: function() {
            this.labels.reverse();
        },
        
        // Add point p that is known to be locate between this line's ends, unless
        // p is already accounted for. Return true iff p was actually added.
        insert: function(p, solid) {
            var epsilon = solid.getRefLength() * math.EPSILON;
            if (vec3.dist(solid.points[this.labels[0]].crds, p.crds) < epsilon) {
                return false;
            }
            for (var labelIndex = 0; labelIndex < this.labels.length - 1; labelIndex++) {
                var pPrev = solid.points[this.labels[labelIndex]].crds;
                var pNext = solid.points[this.labels[labelIndex + 1]].crds;
                if (vec3.dist(pNext, p.crds) < epsilon) {
                    return false;
                }
                var ppPrev = vec3.sub([], pPrev, p.crds);
                var ppNext = vec3.sub([], pNext, p.crds);
                if (vec3.dot(ppNext, ppPrev) < 0) {
                    this.labels.splice(labelIndex + 1, 0, p.label);
                    return true;
                }
            }
            return false;
        },
        
        // Assume this line does not contain p, which lies on the same infinite line
        // as this line. Add point p to this line
        addPoint: function(p, solid) {
            var k = math.getRatio(solid.points[this.firstLabel()].crds,
                        solid.points[this.lastLabel()].crds, p.crds);
            if (k > 0) {
                this.insert(p, solid);
            }
            else if (k > -1) {
                this.labels.splice(0, 0, p.label);
            }
            else {
                this.labels.push(p.label);
            }
        },

        acquirePoint: function(p, solid) {
            var p1 = solid.points[this.labels[0]].crds;
            var p2 = solid.points[this.labels[this.labels.length - 1]].crds;
            var pr = math.project(p.crds, p1, p2);
            if (vec3.dist(pr, p.crds) > vec3.dist(p1, p2) * math.EPSILON) {
                return false;
            }
            return this.insert(p, solid);
        },
        
        acquireLine: function(line, solid) {
            var epsilon = solid.getRefLength() * math.EPSILON;
            var p1 = solid.points[this.labels[0]].crds;
            var p2 = solid.points[this.labels[this.labels.length - 1]].crds;
            var p3 = solid.points[line.labels[0]].crds;
            var p4 = solid.points[line.labels[line.labels.length - 1]].crds;
            var pr3 = math.project(p3, p1, p2);
            if (vec3.dist(pr3, p3) > epsilon) {
                return false;
            }
            var pr4 = math.project(p4, p1, p2);
            if (vec3.dist(pr4, p4) > epsilon) {
                return false;
            }
            // Now we know that segment to be acquired lies on the same infinite line as this
            var k3 = -math.getRatio(p3, p2, p1);
            var k4 = -math.getRatio(p4, p2, p1);
            if (k3 < -math.EPSILON && k4 < -math.EPSILON) {
                // p3, p4 < p1 < p2
                return false;
            }
            if (k3 > 1 + math.EPSILON && k4 > 1 + math.EPSILON) {
                // p1 < p2 < p3, p4
                return false;
            }
            if (k3 < -math.EPSILON) {
                // p3 < p1
                this.labels.splice(0, 0, line.firstLabel());
            }
            else if (k3 > 1 + math.EPSILON) {
                // p3 > p2
                this.labels.push(line.firstLabel());
            }
            else {
                // p1 <= p3 <= p2
                this.insert(solid.points[line.labels[0]], solid);
            }
            if (k4 < -math.EPSILON) {
                // p4 < p1
                this.labels.splice(0, 0, line.labels[line.labels.length - 1]);
            }
            else if (k4 > 1 + math.EPSILON) {
                // p4 > p2
                this.labels.push(line.labels[line.labels.length - 1]);
            }
            else {
                // p1 <= p4 <= p2
                this.insert(solid.points[line.labels[line.labels.length - 1]], solid);
            }
            for (var labelIndex = 1; labelIndex < line.labels.length - 1; labelIndex++) {
                var p = solid.points[line.labels[labelIndex]];
                this.insert(p, solid);
            }
            return true;
        },

        toVector: function(solid) {
            var p1 = solid.points[this.firstLabel()].crds;
            var p2 = solid.points[this.lastLabel()].crds;
            var v = vec3.sub([], p2, p1);
            return v;
        },
        
        getStickCode: function() {
            var label1 = this.labels[0];
            var label2 = this.labels[this.labels.length - 1];
            return label1 < label2 ? label1 + label2 : label2 + label1;
        },
        
        toJson: function() {
            var json = [ this.labels[0], this.labels[this.labels.length - 1]];
            return json;
        },
        
        toString: function() {
            return "[" + this.labels.join("") + "]";
        }
    });
});
