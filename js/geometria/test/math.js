/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GMath"
], function(gmath) {

    return {
        name: "math",

        areEpsilonEqual: function() {
            var epsilon = gmath.EPSILON;
            var test = function(p1, p2) {
                return gmath.areEpsilonEqual(p1, p2, 1);
            };
            return test([0, 0, 0], [0.5*epsilon, -0.5*epsilon, 0.5*epsilon]) &&
                    !test([0, 0, 0], [epsilon, epsilon, epsilon]);
        },
        
        areCollinearPoints: function() {
            var test = function(ps) {
                return gmath.areCollinearPoints(ps, 1);
            };
            return test([[1, 1, 1]]) &&
                    test([[1, 1, 1], [2, 2, 2]]) &&
                    test([[1, 1, 1], [2, 2, 2], [1, 1, 1]]) &&
                    test([[1, 1, 1], [2, 2, 2], [3, 3, 3]]) &&
                    !test([[1, 1, 1], [2, 2, 2], [3, 3.1, 3]]);
        },
        
        areCollinearVectors: function() {
            var test = function(v1, v2) {
                return gmath.areCollinearVectors(v1, v2, 1);
            };
            return test([1, 1, 1], [1, 1, 1]) &&
                   test([1, 2, 3], [2, 4, 6]) &&
                    test([1, 2, 3], [-1, -2, -3]) &&
                    !test([1, 1, 1], [1, 1, 0]);
        },
        
        intersect: function() {
            var test = function(p11, p12, p21, p22, p) {
                var pInt = gmath.intersect(p11, p12, p21, p22, 1);
                if (!pInt) {
                    return false;
                }
                var dp = vec3.sub([], p, pInt);
                return vec3.length(dp) < gmath.EPSILON;
            }
            return test([1, 1, 1], [3, 3, 3], [3, 1, 2], [1, 3, 2], [2, 2, 2]) &&
                    test([1, 1, 1], [3, 3, 3], [-3, -1, -2], [-1, -3, -2], [-2, -2, -2]) &&
                    test([2, 0, 2], [0, 2, 2], [4, 0, 2], [6, 2, 2], [3, -1, 2]) &&
                    test([2, 0, 2], [0, 2, 2], [4, 2, 2], [6, 4, 2], [2, 0, 2]) &&
                    !test([1, 1, 1], [3, 3, 3], [2, 2, 0], [4, 4, 2]);
        },
                
        
        cross: function() {
            var test = function(p1, p2, p3, value) {
                return Math.abs(vec3.length(gmath.cross(p1, p2, p3)) - value) < gmath.EPSILON;
            };
            return test([1, 1, 1], [1, 1, 1], [2, 2, 2], 0) &&
                    test([1, 1, 1], [2, 2, 2], [3, 3, 3], 0) &&
                    test([1, 1, 1], [2, 1, 1], [1, 2, 1], 1);
        },

        getOrientation: function() {
            var test = function(p1, p2, p3, p4) {
                return gmath.getOrientation(p1, p2, p3, p4);
            };
            return test([1, 1, 1], [2, 1, 1], [1, 2, 1], [1, 1, 2]) > 0 &&
                    test([1, 1, 1], [1, 1, 2], [1, 2, 1], [2, 1, 1]) < 0 &&
                    test([1, 1, 1], [2, 1, 1], [1, 1, 2], [2, 1, 2]) == 0;
        },
   
        orthize: function() {
            var test = function(v1, v2, v1Res, v2Res) {
                gmath.orthize(v1, v2);
                return vec3.dist(v1, v1Res) < gmath.EPSILON &&
                    vec3.dist(v2, v2Res) < gmath.EPSILON;
            };
            return test([2, 0, 0], [1, 1, 0], [1, 0, 0], [0, 1, 0]) &&
                    test([2, 0, 0], [-1, 1, 0], [1, 0, 0], [0, 1, 0]) &&
                    test([2, 0, 0], [1, -1, 0], [1, 0, 0], [0, -1, 0]) &&
                    test([2, 0, 0], [-1, -1, 0], [1, 0, 0], [0, -1, 0]) &&
                    test([0, 2, 0], [0, 1, 1], [0, 1, 0], [0, 0, 1]) &&
                    test([0, 0, 2], [1, 0, 1], [0, 0, 1], [1, 0, 0]);
        },
   
        getRatio: function() {
            var test = function(p1, p2, p, value) {
                return Math.abs(gmath.getRatio(p1, p2, p) - value) < gmath.EPSILON;
            };
            return test([1, 1, 1], [3, 3, 3], [2, 2, 2], 1) &&
                    test([1, 1, 1], [3, 3, 3], [-1, -1, -1], -0.5) &&
                    test([1, 1, 1], [3, 3, 3], [5, 5, 5], -2);
        },
   
        isBetween: function() {
            var test = function(p, p1, p2) {
                return gmath.isBetween(p, p1, p2);
            };
            return test([2, 4, 6], [1, 2, 3], [3, 6, 9]) &&
                    !test([1, 2, 3], [2, 4, 6], [3, 6, 9]) &&
                    !test([3, 6, 9], [1, 2, 3], [2, 4, 6]) &&
                    !test([1, 0, 0], [0, 1, 0], [0, 0, 1]);
        },

        distanceToPlane: function() {
            var test = function(p0, p1, p2, p3, value) {
                return Math.abs(gmath.distanceToPlane(p0, p1, p2, p3) - value) < gmath.EPSILON;
            };
            return test([10, 20, 2], [0, 0, 1], [1, 0, 1], [0, 1, 1], 1) &&
                    test([20, 30, 0], [0, 0, 1], [1, 0, 1], [0, 1, 1], 1) &&
                    test([30, 40, 1], [0, 0, 1], [1, 0, 1], [0, 1, 1], 0);
        },
   
        angle: function() {
            var test = function(p, p1, p2, value) {
                return Math.abs(gmath.angle(p, p1, p2) - value) < gmath.EPSILON;
            };
            return test([2, 1, 1], [1, 1, 1], [1, 2, 1], Math.PI/2) &&
                    test([2, 1, 1], [1, 1, 1], [2, 1, 1], 0) &&
                    test([2, 1, 1], [1, 1, 1], [2, 2, 1], Math.PI/4) &&
                    test([2, 1, 1], [1, 1, 1], [-1, 1, 1], Math.PI);
        },

        areCooriented: function() {
            var test = function(v1, v2) {
                return gmath.areCooriented(v1, v2, 1);
            };
            return test([1, 2, 3], [2, 4, 6]) &&
                    !test([1, 2, 3], [-1, -2, -3]);
        },
        
        divideAngle: function() {
            var test = function(p0, p1, p2, k, v) {
                var vv = gmath.divideAngle(p0, p1, p2, k);
                return gmath.areCooriented(v, vv, 1);
            };
            return test([1, 1, 1], [2, 1, 1], [1, 2, 1], 1, [1, 1, 0]) &&
                    test([1, 1, 1], [1, 2, 1], [1, 1, 2], 1, [0, 1, 1]) &&
                    test([1, 1, 1], [1, 1, 2], [2, 1, 1], 1, [1, 0, 1]);
        },
   
        intersectPlane: function() {
            var test = function(p1, p2, p0, n, p) {
                var pInt = gmath.intersectPlane(p1, p2, p0, n);
                if (!pInt) {
                    return false;
                }
                return vec3.dist(pInt, p) < gmath.EPSILON;
            };
            return test([2, 3, 1], [5, 6, 2], [1, 1, 1], [0, 0, 1], [2, 3, 1]) &&
                    test([2, 5, 6], [1, 2, 3], [1, 1, 1], [1, 0, 0], [1, 2, 3]) &&
                    test([2, 3, 4], [-4, -3, -2], [1, 1, 1], [0, 0, 1], [-1, 0, 1]) &&
                    !test([2, 3, 1.1], [5, 6, 2], [1, 1, 1], [0, 0, 1], [2, 3, 1]);
        },
        
        isInPlane: function() {
            var test = function(p, p0, n) {
                return gmath.isInPlane(p, p0, n);
            };
            return test([2, 3, 1], [1, 1, 1], [0, 0, 1]) &&
                    test([1, 2, 3], [1, 1, 1], [1, 0, 0]) &&
                    test([3, 1, 2], [1, 1, 1], [0, 1, 0]) &&
                    !test([2, 3, 1.1], [1, 1, 1], [0, 0, 1]);
        },
   
        intersectSphere: function() {
            var test = function(p1, p2, c, r, ps) {
                var psComputed = gmath.intersectSphere(p1, p2, c, r);
                if (ps.length != psComputed.length) {
                    return false;
                }
                for (pIndex = 0; pIndex < psComputed.length; pIndex++) {
                    if (vec3.dist(ps[pIndex], psComputed[pIndex]) > gmath.EPSILON) {
                        return false;
                    }
                }
                return true;
            };
            return test([1, 1, 1], [1, 1, 4], [1, 1, 1], 2, [[1, 1, 3]]) &&
                    test([1, 1, -1], [1, 1, 3], [1, 1, 1], 2, [[1, 1, -1], [1, 1, 3]]) &&
                    test([1, 1, -1.1], [1, 1, 3.1], [1, 1, 1], 2, [[1, 1, -1],[1, 1, 3]]) &&
                    test([1, 1, -0.9], [1, 1, 2.9], [1, 1, 1], 2, []);
        },
   
        layAngle: function() {
            var test = function(v, n, angle, vs) {
                var vsComputed = gmath.layAngle(v, n, angle);
                if (vs.length != vsComputed.length) {
                    return false;
                }
                for (vIndex = 0; vIndex < vsComputed.length; vIndex++) {
                    if (vec3.dist(vs[vIndex], vsComputed[vIndex]) > gmath.EPSILON) {
                        return false;
                    }
                }
                return true;
            };
            return test([1, 0, 0], [0, 0, 1], Math.PI/2, [[0, 1, 0], [0, -1, 0]]) &&
                    test([0, 1, 0], [1, 0, 0], Math.PI/2, [[0, 0, 1], [0, 0, -1]]) &&
                    test([0, 0, 1], [0, 1, 0], Math.PI/2, [[1, 0, 0], [-1, 0, 0]]);
        },
        
        scale: function() {
            var test = function(p, p0, n, factor, result) {
                var pScaled = gmath.scale(p, p0, n, factor);
                return vec3.dist(pScaled, result) < gmath.EPSILON;
            };
            return test([2, 3, 4], [1, 1, 1], [0, 0, 1], 2, [2, 3, 7]) &&
                    test([4, 2, 3], [1, 1, 1], [1, 0, 0], 2, [7, 2, 3]) &&
                    test([3, 4, 2], [1, 1, 1], [0, 1, 0], 2, [3, 7, 2]);
        },

        shear: function() {
            var test = function(p, p0, p1, p2, result) {
                var pSheared = gmath.shear(p, p0, p1, p2);
                return vec3.dist(pSheared, result) < gmath.EPSILON;
            };
            return test([2, 2, 3], [1, 1, 1], [3, 3, 1], [2, 2, 2], [4, 4, 3]) &&
                    test([3, 2, 2], [1, 1, 1], [1, 3, 3], [2, 2, 2], [3, 4, 4]) &&
                    test([2, 3, 2], [1, 1, 1], [3, 1, 3], [2, 2, 2], [4, 3, 4]);
        },
   
        project: function() {
            var test = function(p, p1, p2, value) {
                return gmath.areEpsilonEqual(gmath.project(p, p1, p2), value, 1);
            };
            return test([2, 2, 2], [1, 1, 1], [3, 3, 3], [2, 2, 2]) &&
                    test([1, 1, 1], [1, 1, 1], [3, 3, 3], [1, 1, 1]) &&
                    test([3, 3, 3], [1, 1, 1], [3, 3, 3], [3, 3, 3]) &&
                    test([1, 10, 20], [2, 1, 1], [3, 1, 1], [1, 1, 1]);
        },
   
        boundingSphere: function() {
            var epsilon = 1e-2;
            var test = function(ps, c, r) {
                var bs = gmath.boundingSphere(ps);
                return vec3.dist(bs.center, c) < epsilon &&
                        Math.abs(bs.radius - r) < epsilon;
            };
            return test(
                [ [0, 0, 0], [2, 0, 0], [2, 2, 0], [0, 2, 0],
                  [0, 0, 2], [2, 0, 2], [2, 2, 2], [0, 2, 2] ],
                [1, 1, 1], Math.sqrt(3));
        }
    };
});