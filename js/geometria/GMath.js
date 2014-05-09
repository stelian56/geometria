/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
], function() {

    glMatrix.setMatrixArrayType(Array);
    var EPSILON = 1e-10;

    return {
        EPSILON: EPSILON,

        validateCrds: function(crds) {
            var valid = true;
            if (Array.isArray(crds) && crds.length == 3) {
                $.each(crds, function() {
                    if (!(this > -Number.MAX_VALUE && this < Number.MAX_VALUE)) {
                        valid = false;
                        return false;
                    }
                });
                return valid;
            }
            return false;
        },
        
        validateQuat: function(q) {
            var valid = true;
            if (Array.isArray(q) && q.length == 4) {
                $.each(q, function() {
                    if (!(this >= -1 - EPSILON && this <= 1 + EPSILON)) {
                        valid = false;
                        return false;
                    }
                });
                return valid;
            }
            return false;
        },

        // Return true iff xyz distance between p1, p2 is less than refLength*EPSILON
        areEpsilonEqual: function(p1, p2, refLength) {
            var epsilon = refLength*EPSILON;
            return Math.abs(p2[0] - p1[0]) < epsilon &&
                    Math.abs(p2[1] - p1[1]) < epsilon &&
                    Math.abs(p2[2] - p1[2]) < epsilon;
        },
        
        // Return true iff ps are collinear
        areCollinearPoints: function(ps, refLength) {
            if (ps.length < 3) {
                return true;
            }
            var epsilon = refLength*EPSILON;
            var v, vLength;
            for (var i = 1; i < ps.length; i++) {
                if (v) {
                    if (vec3.length(vec3.cross([], v, vec3.sub([], ps[i], ps[0])))/vLength
                            >= epsilon) {
                        return false;
                    }
                }
                else if (!this.areEpsilonEqual(ps[0], ps[i])){
                    v = vec3.sub([], ps[i], ps[0]);
                    vLength = vec3.length(v);
                }
            }
            return true;
        },
        
        // Return true iff v1, v2 are collinear
        areCollinearVectors: function(v1, v2, refLength) {
            var ps = [vec3.create(), v1, v2];
            return this.areCollinearPoints(ps, refLength);
        },
        
        // Assume p11 distinct from p12, p21 distinct from p22 and lines p11p12, p21p22 are
        //  coplanar. Return the intersection point, null if lines are collinear.
        intersect: function(p11, p12, p21, p22, refLength) {
            var v1 = vec3.sub([], p12, p11);
            var v2 = vec3.sub([], p22, p21);
            if (this.areCollinearVectors(v1, v2, refLength)) {
                return null;
            }
            var epsilon = refLength*EPSILON;
            var t;
            var k = v1[0] * v2[1] - v1[1] * v2[0];
            var v1Length = vec3.length(v1);
            var v2Length = vec3.length(v2);
            if (Math.abs(k) / v1Length * v2Length > epsilon) {
                t = (v2[1] * (p21[0] - p11[0]) - v2[0] * (p21[1] - p11[1])) / k;
            }
            else {
                k = v1[1] * v2[2] - v1[2] * v2[1];
                if (Math.abs(k) / v1Length * v2Length > epsilon) {
                    t = (v2[2] * (p21[1] - p11[1]) - v2[1] * (p21[2] - p11[2])) / k;
                }
                else {
                    t = (v2[0] * (p21[2] - p11[2]) - v2[2] * (p21[0] - p11[0]))
                            / (v1[2] * v2[0] - v1[0] * v2[2]);
                }
            }
            return new vec3.fromValues(p11[0] + v1[0] * t, p11[1] + v1[1] * t, p11[2] + v1[2] * t);
        },
        
        // Cross product of vectors p1p2, p1p3
        cross: function(p1, p2, p3) {
            return vec3.cross([], vec3.sub([], p2, p1), vec3.sub([], p3, p1));
        },
        
        // Orientation of tetrahedron p1p2p3p4
        getOrientation: function(p1, p2, p3, p4) {
            var v = vec3.sub([], p4, p1);
            return this.getTriOrientation(p1, p2, p3, v);
        },
        
        // Orientation of triangle p1p2p3 vs vector v
        getTriOrientation: function(p1, p2, p3, v) {
            var v1 = vec3.sub([], p1, p2);
            var v2 = vec3.sub([], p1, p3);
            var v12 = vec3.cross([], v1, v2);
            vec3.normalize(v12, v12);
            var vLength = vec3.length(v);
            var signedVolume = vec3.dot(v12, v);
            if (signedVolume > vLength*EPSILON) {
                return 1;
            }
            if (signedVolume < -vLength*EPSILON) {
                return -1;
            }
            return 0;
        },

        // Assume v1, v2 are non-collinear vectors. Normalize v1 and transform v2 into a normalized
        // vector orthogonal to v1 so that v1xv2 preserves its direction
        orthize: function(v1, v2) {
            vec3.normalize(v1, v1);
            var v = vec3.create();
            vec3.scale(v, v1, vec3.dot(v1, v2));
            vec3.sub(v, v, v2);
            vec3.scale(v, v, -1);
            vec3.copy(v2, v);
            vec3.normalize(v2, v2);
        },
    
        // Assume p1, p2, p are distinct collinear points. Return ratio p1p / pp2
        getRatio: function(p1, p2, p) {
            var v1 = vec3.sub([], p, p1);
            var v2 = vec3.sub([], p2, p);
            var ratio = vec3.length(v1) / vec3.length(v2);
            return vec3.dot(v1, v2) > 0 ? ratio : -ratio;
        },
        
        // Assume p1, p2 are distinct points. Return true iff p is between p1 and p2
        isBetween: function(p, p1, p2) {
            var refLength = vec3.dist(p1, p2);
            return this.areEpsilonEqual(p, p1, refLength) || this.areEpsilonEqual(p, p2) ||
                this.isStrictlyBetween(p, p1, p2, refLength);
        },

        // Assume p1, p2 are distinct points.
        // Return true iff p is between p1 and p2, but not equal to any of them
        isStrictlyBetween: function(p, p1, p2, refLength) {
            return this.areCollinearPoints([p, p1, p2], refLength) && this.getRatio(p1, p2, p) > 0;
        },
    
        // Return 2D distance from point p to segment p1p2
        distanceToSegment: function(p, p1, p2) {
            p2 =vec2.sub([], p2, p1);
            p = vec2.sub([], p, p1);
            var dotprod = vec2.dot(p, p2);
            var projlenSq;
            if (dotprod <= 0) {
                projlenSq = 0;
            }
            else {
                vec2.sub(p, p2, p);
                dotprod = vec2.dot(p, p2);
                if (dotprod <= 0) {
                    projlenSq = 0;
                }
                else {
                    projlenSq = dotprod*dotprod / vec2.squaredLength(p2);
                }
            }
            var lenSq = vec2.squaredLength(p) - projlenSq;
            if (lenSq < 0) {
                return 0;
            }
            return Math.sqrt(lenSq);
        },
        
        // Assume p1, p2, p3 are non-collinear. Return distance from p to plane p1p2p3.
        distanceToPlane: function(p, p1, p2, p3) {
            var n = this.cross(p1, p2, p3);
            var v = vec3.sub([], p, p1);
            var distance = Math.abs(vec3.dot(v, n)) / vec3.length(n);
            return distance;
        },

        // Assume n has positive length, p1 and p2 are distinct points.
        // Return the intersection point of line p1p2 with plane (p0, n).
        // Return null if p1p2 is collinear with the plane or p1p2
        // does not intersect the plane.
        intersectPlane: function(p1, p2, p0, n) {
            var p1p2 = vec3.create();
            vec3.sub(p1p2, p2, p1);
            var p1p2n = vec3.dot(p1p2, n);
            if (Math.abs(p1p2n/(vec3.length(p1p2)*vec3.length(n))) < EPSILON) {
                return null;
            }
            var t = (n[0]*(p0[0] - p1[0]) + n[1]*(p0[1] - p1[1]) + n[2]*(p0[2] - p1[2]))/p1p2n;
            if (t < 1 + EPSILON && t > -EPSILON) {
                vec3.scale(p1p2, p1p2, t);
                var p = vec3.create();
                vec3.add(p, p1, p1p2);
                return p;
            }
            return null;
        },
    
        // Assume n has positive length. Return true iff p is in plane (p0, n).
        isInPlane: function(p, p0, n) {
            var p0p = vec3.create();
            vec3.sub(p0p, p, p0);
            var p0pLength = vec3.length(p0p);
            var nLength = vec3.length(n);
            if (p0pLength/nLength < EPSILON) {
                return true;
            }
            return Math.abs(vec3.dot(p0p, n)/(p0pLength*nLength)) < EPSILON;
        },
        
        // Assume p1, p2, p3 are non-collinear. Return area of triangle p1p2p3.
        area: function(p1, p2, p3) {
            var v = this.cross(p1, p2, p3);
            return 0.5*vec3.length(v);
        },

        // Assume p1, p2, p3 are distinct points. Return angle p1p2p3 in radians
        angle: function(p1, p2, p3) {
            var v1 = vec3.sub([], p1, p2);
            var v2 = vec3.sub([], p3, p2);
            var cos = vec3.dot(v1, v2)/(vec3.length(v1)*vec3.length(v2));
            return Math.acos(cos);
        },
   
        // Return true iff v1, v2 are cooriented
        areCooriented: function(v1, v2, refLength) {
            var epsilon = refLength*EPSILON;
            var v1Length = vec3.length(v1);
            if (v1Length < epsilon) {
                return true;
            }
            var v = vec3.create();
            vec3.scale(v, v1, vec3.length(v2)/v1Length);
            return this.areEpsilonEqual(v, v2, refLength);
        },

        // Assume p0, p1, p2 are non-collinear, k > 0.
        // Return vector that divides angle p1p0p2 in ratio k
        divideAngle: function(p0, p1, p2, k) {
            var v1 = vec3.create();
            vec3.sub(v1, p1, p0);
            vec3.normalize(v1, v1);
            var v2 = vec3.create();
            vec3.sub(v2, p2, p0);
            vec3.normalize(v2, v2);
            var phi = Math.acos(vec3.dot(v1, v2));
            var v = vec3.create();
            vec3.sub(v, v2, v1);
            vec3.scale(v, v, 0.5*(1 - Math.tan(0.5*phi * (1 - k)/(1 + k))/Math.tan(phi/2)));
            vec3.add(v, v, v1);
            return v;
        },
   
        // Assume p1, p2 are distinct points. Intersect line p1p2 with sphere (c, r)
        intersectSphere: function(p1, p2, c, r) {
            var ps = [], ts = [];
            var v12 = vec3.create();
            vec3.sub(v12, p2, p1);
            var v1c = vec3.create();
            vec3.sub(v1c, p1, c);
            var k = vec3.squaredLength(v12);
            var m = vec3.dot(v12, v1c);
            var n = vec3.squaredLength(v1c) - r*r;
            var d = m*m - k*n;
            if (d/(m*m) < -EPSILON) {
                return ps;
            }
            else if (d/(m*m) < EPSILON) {
                ts.push(-m/k);
            }
            else {
                var srd = Math.sqrt(d);
                ts.push((-m - srd)/k);
                ts.push((-m + srd)/k);
            }
            var sv12 = vec3.create();
            $.each(ts, function(index, t) {
                if (t < 1 + EPSILON && t > -EPSILON) {
                    vec3.scale(sv12, v12, t);
                    var p = vec3.create();
                    vec3.add(p, p1, sv12);
                    ps.push(p);
                }
            });
            return ps;
        },

        // Assume n has a positive length. Scale p about plane (p0, n) with factor 'factor'.
        scale: function(p, p0, n, factor) {
            var p0p = vec3.create();
            vec3.sub(p0p, p, p0);
            var pn = vec3.dot(p0p, n);
            var n2 = vec3.squaredLength(n);
            var sn = vec3.create();
            vec3.scale(sn, n, pn/n2*(factor - 1));
            vec3.add(sn, sn, p);
            return sn;
        },
        
        // Assume p0, p1, p2 are non-collinear points. Shear point p along a plane that
        // contains p0, p1 and is perpendicular to plane p0p1p2, so that p2 is the image
        // of a point z such as p0z is perpendicular to the plane
        shear: function(p, p0, p1, p2) {
            var v1 = vec3.create();
            vec3.sub(v1, p1, p0);
            var v2 = vec3.create();
            vec3.sub(v2, p2, p0);
            var v3 = vec3.create();
            vec3.scale(v3, v1, -vec3.dot(v1, v2)/vec3.squaredLength(v1));
            vec3.add(v3, v3, v2);
            var r = vec3.create();
            vec3.sub(r, p, p0);
            var m = vec3.dot(r, v3)/vec3.squaredLength(v3);
            vec3.sub(v2, v2, v3);
            vec3.scale(v2, v2, m);
            vec3.add(v2, p, v2);
            return v2;
        },
        
        // Assume v, n are orthogonal and have positive lengths, 0 < angle < pi.
        // Return the two unit vectors that are orthogonal to n and make angle
        // 'angle' with v.
        layAngle: function(v, n, angle) {

            // Return the two solutions (presumingly real and distinct) of the system of equations:
            // u dot n = 0
            // u dot v = k
            // length u = 1
            // where v.x*n.y - v.y*n.x != 0
            var solve = function(v, n, k) {
                var d = v[0]*n[1] - v[1]*n[0];
                var r1 = v[2]*n[1] - v[1]*n[2];
                var r2 = v[2]*n[0] - v[0]*n[2];
                var a = r1*r1 + r2*r2 + d*d;
                var b = r1*k*n[1] + r2*k*n[0];
                var c = k*k*(n[0]*n[0] + n[1]*n[1]) - d*d;
                var m = Math.sqrt(b*b - a*c);
                var z = [(b + m)/a, (b - m)/a];
                var u = [];
                $.each([0, 1], function(index) {
                    var x = (k*n[1] - z[index]*r1)/d;
                    var y = (-k*n[0] + z[index]*r2)/d;
                    u.push(vec3.fromValues(x, y, z[index]));
                });
                return u;
            }

            var lv = vec3.length(v);
            var k = lv*Math.cos(angle);
            var d = v[0]*n[1] - v[1]*n[0];
            var u;
            if (Math.abs(d/(lv*vec3.length(n))) > EPSILON) {
                u = solve(v, n, k);
            }
            else {
                d = v[1]*n[2] - v[2]*n[1];
                if (Math.abs(d/(lv*vec3.length(n))) > EPSILON) {
                    var vyzx = vec3.fromValues(v[1], v[2], v[0]);
                    var nyzx = vec3.fromValues(n[1], n[2], n[0]);
                    var w = solve(vyzx, nyzx, k);
                    u = [vec3.fromValues(w[0][2], w[0][0], w[0][1]),
                        vec3.fromValues(w[1][2], w[1][0], w[1][1])];
                }
                else {
                    d = v[0]*n[2] - v[2]*n[0];
                    var vxzy = vec3.fromValues(v[0], v[2], v[1]);
                    var nxzy = vec3.fromValues(n[0], n[2], n[1]);
                    var w = solve(vxzy, nxzy, k);
                    u = [vec3.fromValues(w[0][0], w[0][2], w[0][1]),
                        vec3.fromValues(w[1][0], w[1][2], w[1][1])];
                }
            }
            return u;
        },

        // Assume p1, p2 are distinct points. Return projection of p onto infinite line p1p2.
        project: function(p, p1, p2) {
            var p1p = vec3.sub([], p, p1);
            var p1p2 = vec3.sub([], p2, p1);
            var k = vec3.dot(p1p, p1p2) / vec3.squaredLength(p1p2);
            var pr = vec3.sub([], p2, p1);
            vec3.scaleAndAdd(pr, p1, pr, k);
            return pr;
        },

        // The smallest sphere that contains the given collection of points.
        // Center is approximate, away from theoretical by no more than epsilon.
        // http://www.inb.uni-luebeck.de/publications/pdfs/MaMa04a.pdf, equation (1)
        boundingSphere: function(ps) {
            var epsilon = 1e-2;
            var numIterations = 1 / epsilon;
            
            var furthestPoint = function(c) {
                var maxDist = -1;
                var fp;
                $.each(ps, function() {
                    var dist = vec3.dist(this, c);
                    if (dist > maxDist) {
                        maxDist = dist;
                        fp = this;
                    }   
                });
                return fp;
            };

            var c = vec3.create();
            var p = vec3.create();
            var i;
            for (i = 0; i < numIterations; i++) {
                var fp = furthestPoint(c);
                vec3.sub(p, fp, c);
                vec3.scaleAndAdd(p, c, p, 1 / (1 + i));
                vec3.copy(c, p);
            }
            p = furthestPoint(c);
            var r = vec3.dist(p, c);
            return {center: c, radius: r};
        }
    };
});