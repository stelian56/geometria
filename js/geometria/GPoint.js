/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "geometria/GUtils"
], function(declare, utils) {

    var fitLabel = function(star, width) {
        var owner = star.owner;
        // Angles measured from X axis
        var angles = [];
        $.each(star.neighbors, function() {
            var p = this;
            var v = vec2.sub([], p.scrCrds, owner.scrCrds);
            vec2.normalize(v, v);
            var angle = Math.acos(v[0]);
            if (v[1] < 0) {
                angle = 2*Math.PI - angle;
            }
            angles.push(angle);
        });
        angles.sort();
        angles.push(angles[0] + 2*Math.PI);
        var angle1 = 0;
        var angle2 = 2*Math.PI;
        var gap = 0;
        for (var angleIndex = 0; angleIndex < angles.length - 1; angleIndex++) {
            if (angles[angleIndex + 1] - angles[angleIndex] > gap) {
                angle1 = angles[angleIndex];
                angle2 = angles[angleIndex + 1];
                gap = angle2 - angle1;
                // A pi/2 gap is sufficient to fit in a label
                if (gap > Math.PI / 2) {
                    break;
                }
            }
        }
        var angle = (angle1 + angle2) / 2;
        var height = 16;
        var labelPos = {
            x: owner.scrCrds[0] + width * Math.cos(angle) - width/2,
            y: owner.scrCrds[1] + height * Math.sin(angle) + height/3
        };
        return labelPos;
    };

    return declare(null, {

        constructor: function(label, crds) {
            this.label = label;
            this.crds = crds;
            this.isVertex = false;
            this.projCrds = null;
            this.scrCrds = null;
            this.lines = [];
            this._circleSvg = null;
            this._labelSvg = null;
        },

        clone: function() {
            var crds = vec3.clone(this.crds);
            var p = new this.constructor(this.label, crds);
            p.isVertex = this.isVertex;
            return p;
        },
        
        make: function(props) {
            var label = utils.firstKey(props);
            this.label = label;
            this.crds = props[label];
        },

        resetLines: function() {
            this.lines = [];
        },
        
        getFaces: function() {
            var faces = {};
            $.each(this.lines, function() {
                var face = this.face;
                faces[face.code] = face;
            });
            return faces;
        },
        
        project: function(camera, center) {
            var cs = vec3.sub([], this.crds, center);
            vec3.transformMat3(cs, cs, camera.attitude);
            this.projCrds = vec2.fromValues(cs[0], cs[1]);
        },
        
        toScreen: function(scalingFactor, figureSize) {
            var scrX = 0.5*figureSize.width + scalingFactor*this.projCrds[0];
            var scrY = 0.5*figureSize.height - scalingFactor*this.projCrds[1];
            this.scrCrds = vec2.fromValues(scrX, scrY);
        },
        
        hide: function() {
            if (this._circleSvg) {
                this._circleSvg.style.display = "none";
                this._labelSvg.style.display = "none";
            }
        },
   
        initSvg: function(svg) {
            this._circleSvg = document.createElementNS("http://www.w3.org/2000/svg", "ellipse");
            $(this._circleSvg).appendTo(svg);
            this._labelSvg = document.createElementNS("http://www.w3.org/2000/svg", "text");
            $(this._labelSvg).appendTo(svg);
        },
   
        draw: function(wireframe, star, selected) {
            var radius = selected ? utils.selectedPointRadius : utils.pointRadius;
            var color = selected ? utils.selectionColor : utils.pointColor;
            $(this._circleSvg).attr({
                cx: this.scrCrds[0],
                cy: this.scrCrds[1],
                rx: radius,
                ry: radius,
                stroke: color,
                fill: color
            });
            this._circleSvg.style.display = wireframe || selected ? "" : "none";
            if (star) {
                this._labelSvg.textContent = this.label;
                var width = this._labelSvg.getComputedTextLength();
                width = Math.max(width, 12);
                var labelPos = fitLabel(star, width);
                this._labelSvg.setAttribute("x", labelPos.x);
                this._labelSvg.setAttribute("y", labelPos.y);
                this._labelSvg.style.display = "";
            }
            else if (!selected) {
                this._labelSvg.style.display = "none";
            }
        },
        
        toJson: function() {
            var json = {};
            json[this.label] = this.crds;
            return json;
        },
        
        toString: function() {
            return this.label;
        }
    });
});