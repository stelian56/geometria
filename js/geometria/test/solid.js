/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GActions",
    "geometria/GMath",
    "geometria/GSolid",
    "geometria/GUtils"
], function(actions, math, GSolid, utils) {

    var tetrahedronProps = {
        points: {
            "A": [0, 1.15470053837925153, 0],
            "B": [-1, -0.57735026918962576, 0],
            "C": [1, -0.57735026918962576, 0],
            "D": [0, 0, 1.63299316185545204]
        },
        vertexCount: 4,
        faceCount: 4,
        vertexEdgeCount: 3,
        faceSideCount: 3
    };

        var cubeProps = {
        points: {
            "A": [-1, -1, -1],
            "B": [1, -1, -1],
            "C": [1, 1, -1],
            "D": [-1, 1, -1],
            "E": [-1, -1, 1],
            "F": [1, -1, 1],
            "G": [1, 1, 1],
            "H": [-1, 1, 1],
            
            "I": [0, -1, -1]
        },
        lines: [
            ["A", "C"],
            ["B", "D"]
        ],
        vertexCount: 8,
        faceCount: 6,
        vertexEdgeCount: 3,
        faceSideCount: 4
    };

    var octahedronProps = {
        points: {
            "A": [1, 0, 0],
            "B": [0, 1, 0],
            "C": [-1, 0, 0],
            "D": [0, -1, 0],
            "E": [0, 0, 1],
            "F": [0, 0, -1]
        },
        vertexCount: 6,
        faceCount: 8,
        vertexEdgeCount: 4,
        faceSideCount: 3
    };

    var dodecahedronProps = {
        points: {
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
        },
        vertexCount: 20,
        faceCount: 12,
        vertexEdgeCount: 3,
        faceSideCount: 5
    };

    var icosahedronProps = {
        points: {
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
        },
        vertexCount: 12,
        faceCount: 20,
        vertexEdgeCount: 5,
        faceSideCount: 3
    };

    var randomSolidProps = function() {
        var pointCount = 100;
        var ps = {};
        for (var pointIndex = 0, label = "A"; pointIndex < pointCount; pointIndex++) {
            var crds = vec3.fromValues(Math.random(), Math.random(), Math.random());
            ps[label] = crds;
            label = utils.getNextLabel(label);
        }
        var props = { points: ps };
        return props;
    };
    
    return {
        name: "solid",

        make: function() {
            var test = function(props) {
                var ok = true;
                var solid = new GSolid().make(props);
                ok = ok && solid.faces.length == props.faceCount;
                vertexCount = 0, vertexLineCount = 0;
                $.each(solid.points, function(label, p) {
                    var edgeCount = 0;
                    if (p.isVertex) {
                        vertexCount++;
                        var edgeCount = 0;
                        $.each(p.lines, function() {
                            if (this.twin) {
                                var face = this.face;
                                for (var lineIndex = 0; lineIndex < face.sideCount; lineIndex++) {
                                    var line = face.lines[lineIndex];
                                    if (line.labels[0] == p.label) {
                                        edgeCount++;
                                    }
                                }
                            }
                        });
                        ok = ok && edgeCount / 2 == props.vertexEdgeCount;
                    }
                });
                ok = ok && vertexCount == props.vertexCount;
                $.each(solid.faces, function() {
                    ok = ok && this.sideCount == props.faceSideCount;
                });
                return ok;
            };
            return test(tetrahedronProps) &&
                   test(cubeProps) &&
                   test(octahedronProps) &&
                   test(dodecahedronProps) &&
                   test(icosahedronProps);
        },
        
        computeVolume: function() {
            var test = function(props, value) {
                var solid = new GSolid().make(props);
                var volume = solid.computeVolume();
                return Math.abs(volume - value) < math.EPSILON;
            };
            return test(cubeProps, 8) &&
                    test(octahedronProps, 4/3);
        },
        
        cutJoin: function() {
            var test = function() {
                var solidProps = randomSolidProps();
                var solid = new GSolid().make(solidProps, true);
                var ps = [];
                var face = utils.anyProp(solid.faces);
                ps.push(solid.points[face.labelAt(0)].crds);
                ps.push(solid.points[face.labelAt(1)].crds);
                ps.push(solid.gCenter);
                var v1 = vec3.create();
                vec3.sub(v1, ps[1], ps[0]);
                var v2 = vec3.create();
                vec3.sub(v2, ps[2], ps[0]);
                var n = vec3.create();
                vec3.cross(n, v1, v2);
                vec3.normalize(n, n);
                var solid1 = solid.clone();
                var face1 = solid1.cutOff(ps[0], n);
                vec3.scale(n, n, -1);
                var solid2 = solid.clone();
                var face2 = solid2.cutOff(ps[0], n);
                var joinAction = actions.joinFiguresAction;
                var labels = [face1.labelAt(0), face1.labelAt(1), face1.labelAt(2)];
                if (!joinAction.matchFaces(solid1, solid2, face1, face2, 0, false)) {
                    return false;
                }
                if (!joinAction.matchSolids(solid1, solid2, labels, labels)) {
                    return false;
                }
                return true;
            };
            return test();
        }
    };
});
