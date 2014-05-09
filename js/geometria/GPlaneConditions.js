/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GAnswerUtils",
    "geometria/GDictionary",
    "geometria/GFace",
    "geometria/GFiguresContainer",
    "geometria/GMath",
    "geometria/GNotepadContainer",
    "geometria/GPoint",
    "geometria/GUtils"
], function(answerUtils, dict, GFace, figuresContainer, math, notepadContainer, GPoint, utils) {

    var containsPoint = {
        id: "ContainsPoint",
        group: "Position",
        placeHolder: dict.get("EnterPoint"),
        invalidMessage: dict.get("EnterPoint"),
        validate: answerUtils.validatePoint,
        stringValueFromSelection: answerUtils.pointFromSelection,
        verify: function(ps, targetSolid, value) {
            var valuePs = answerUtils.pointSetStringToCrds(value);
            var p = valuePs[0];
            var n = math.cross(ps[0], ps[1], ps[2]);
            return math.isInPlane(p, ps[0], n);
        }
    };

    var containsLine = {
        id: "ContainsLine",
        group: "Position",
        placeHolder: dict.get("Enter2Points"),
        invalidMessage: dict.get("Enter2CommaSeparatedPoints"),
        validate: answerUtils.validateLine,
        stringValueFromSelection: answerUtils.lineFromSelection,
        verify: function(ps, targetSolid, value) {
            var valuePs = answerUtils.pointSetStringToCrds(value);
            var p1 = valuePs[0];
            var p2 = valuePs[1];
            var n = math.cross(ps[0], ps[1], ps[2]);
            return math.isInPlane(p1, ps[0], n) && math.isInPlane(p2, ps[0], n);
        }
    };

    var doesNotContainPoint = {
        id: "DoesNotContainPoint",
        group: "Position",
        placeHolder: dict.get("EnterPoint"),
        invalidMessage: dict.get("EnterPoint"),
        validate: answerUtils.validatePoint,
        stringValueFromSelection: answerUtils.pointFromSelection,
        verify: function(ps, targetSolid, value) {
            var valuePs = answerUtils.pointSetStringToCrds(value);
            var p = valuePs[0];
            var n = math.cross(ps[0], ps[1], ps[2]);
            return !math.isInPlane(p, ps[0], n);
        }
    };

    var doesNotContainLine = {
        id: "DoesNotContainLine",
        group: "Position",
        placeHolder: dict.get("Enter2Points"),
        invalidMessage: dict.get("Enter2CommaSeparatedPoints"),
        validate: answerUtils.validateLine,
        stringValueFromSelection: answerUtils.lineFromSelection,
        verify: function(ps, targetSolid, value) {
            var valuePs = answerUtils.pointSetStringToCrds(value);
            var p1 = valuePs[0];
            var p2 = valuePs[1];
            var n = math.cross(ps[0], ps[1], ps[2]);
            return !math.isInPlane(p1, ps[0], n) || !math.isInPlane(p2, ps[0], n);
        }
    };

    var containsNoVertex = {
        id: "ContainsNoVertex",
        group: "Position",
        verify: function(ps, targetSolid) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var valid = true;
            $.each(targetSolid.points, function(label, p) {
                if (p.isVertex && math.isInPlane(p.crds, ps[0], n)) {
                    valid = false;
                    return false;
                }
            });
            return valid;
        }
    };

    var containsNoEdge = {
        id: "ContainsNoEdge",
        group: "Position",
        verify: function(ps, targetSolid) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var valid = true;
            $.each(targetSolid.faces, function() {
                for (var lineIndex = 0; lineIndex < this.sideCount; lineIndex++) {
                    var line = this.lines[lineIndex];
                    var p1 = targetSolid.points[line.firstLabel()].crds;
                    var p2 = targetSolid.points[line.lastLabel()].crds;
                    if (math.isInPlane(p1, ps[0], n) && math.isInPlane(p2, ps[0], n)) {
                        valid = false;
                        return false;
                    }
                }
            });
            return valid;
        }
    };

    var parallelToLine = {
        id: "ParallelToLine",
        group: "Position",
        placeHolder: dict.get("Enter2Points"),
        invalidMessage: dict.get("Enter2CommaSeparatedPoints"),
        validate: answerUtils.validateLine,
        stringValueFromSelection: answerUtils.lineFromSelection,
        verify: function(ps, targetSolid, value) {
            var valuePs = answerUtils.pointSetStringToCrds(value);
            var p1 = valuePs[0];
            var p2 = valuePs[1];
            var v = vec3.sub([], p2, p1);
            vec3.normalize(v, v);
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            return Math.abs(vec3.dot(v, n)) < math.EPSILON;
        }
    };

    var parallelToPlane = {
        id: "ParallelToPlane",
        group: "Position",
        placeHolder: dict.get("Enter3Points"),
        invalidMessage: dict.get("Enter3NonCollinearCommaSeparatedPoints"),
        validate: answerUtils.validatePlane,
        stringValueFromSelection: answerUtils.planeFromSelection,
        verify: function(ps, targetSolid, value) {
            var valuePs = answerUtils.pointSetStringToCrds(value);
            var p1 = valuePs[0];
            var p2 = valuePs[1];
            var p3 = valuePs[2];
            var v = math.cross(p1, p2, p3)
            vec3.normalize(v, v);
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            return Math.abs(Math.abs(vec3.dot(v, n)) - 1) < math.EPSILON;
        }
    };

    var perpendicularToLine = {
        id: "PerpendicularToLine",
        group: "Position",
        placeHolder: dict.get("Enter2Points"),
        invalidMessage: dict.get("Enter2CommaSeparatedPoints"),
        validate: answerUtils.validateLine,
        stringValueFromSelection: answerUtils.lineFromSelection,
        verify: function(ps, targetSolid, value) {
            var valuePs = answerUtils.pointSetStringToCrds(value);
            var p1 = valuePs[0];
            var p2 = valuePs[1];
            var v = vec3.sub([], p2, p1);
            vec3.normalize(v, v);
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            return Math.abs(Math.abs(vec3.dot(v, n)) - 1) < math.EPSILON;
        }
    };

    var perpendicularToPlane = {
        id: "PerpendicularToPlane",
        group: "Position",
        placeHolder: dict.get("Enter3Points"),
        invalidMessage: dict.get("Enter3NonCollinearCommaSeparatedPoints"),
        validate: answerUtils.validatePlane,
        stringValueFromSelection: answerUtils.planeFromSelection,
        verify: function(ps, targetSolid, value) {
            var valuePs = answerUtils.pointSetStringToCrds(value);
            var p1 = valuePs[0];
            var p2 = valuePs[1];
            var p3 = valuePs[2];
            var v = math.cross(p1, p2, p3)
            vec3.normalize(v, v);
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            return Math.abs(vec3.dot(v, n)) < math.EPSILON;
        }
    };

    var cutsVolumeInRatio = {
        id: "CutsVolumeInRatio",
        group: "Volume",
        placeHolder: dict.get("EnterRatio"),
        invalidMessage: dict.get("EnterPositiveExpression"),
        validate: function(props) {
            var outProps = { value: [null, null] };
            var scope = notepadContainer.getScope();
            var numerator = utils.eval(props.value[0], scope);
            var denominator = utils.eval(props.value[1], scope);
            if (numerator > 0) {
                outProps.value[0] = numerator.toString();
            }
            if (denominator > 0) {
                outProps.value[1] = denominator.toString();
            }
            return outProps;
        },
        verify: function(ps, targetSolid, value) {
            var ratio = utils.eval(value[0])/utils.eval(value[1]);
            var denominator = value[1];
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var fragment = targetSolid.clone();
            if (!fragment.cutOff(ps[0], n)) {
                return false;
            }
            var v = fragment.computeVolume();
            var r = v / (targetSolid.computeVolume() - v);
            return Math.abs(r - ratio) < math.EPSILON || Math.abs(1 / r - ratio) < math.EPSILON;
        }
    };

    var isoscellesTriangle = {
        id: "IsoscellesTriangle",
        group: "Shape",
        verify: function(ps, targetSolid, value) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var fragment = targetSolid.clone();
            var section = fragment.cutOff(ps[0], n);
            if (!section) {
                return false;
            }
            return section.isIsoscellesTriangle(fragment);
        }
    };

    var rightTriangle = {
        id: "RightTriangle",
        group: "Shape",
        verify: function(ps, targetSolid, value) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var fragment = targetSolid.clone();
            var section = fragment.cutOff(ps[0], n);
            if (!section) {
                return false;
            }
            return section.isRightTriangle(fragment);
        }
    };

    var equilateralTriangle = {
        id: "EquilateralTriangle",
        group: "Shape",
        verify: function(ps, targetSolid, value) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var fragment = targetSolid.clone();
            var section = fragment.cutOff(ps[0], n);
            if (!section) {
                return false;
            }
            return section.isEquilateralTriangle(fragment);
        }
    };

    var parallelogram = {
        id: "Parallelogram",
        group: "Shape",
        verify: function(ps, targetSolid, value) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var fragment = targetSolid.clone();
            var section = fragment.cutOff(ps[0], n);
            if (!section) {
                return false;
            }
            return section.isParallelogram(fragment);
        }
    };

    var rhombus = {
        id: "Rhombus",
        group: "Shape",
        verify: function(ps, targetSolid, value) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var fragment = targetSolid.clone();
            var section = fragment.cutOff(ps[0], n);
            if (!section) {
                return false;
            }
            return section.isRhombus(fragment);
        }
    };

    var rectangle = {
        id: "Rectangle",
        group: "Shape",
        verify: function(ps, targetSolid, value) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var fragment = targetSolid.clone();
            var section = fragment.cutOff(ps[0], n);
            if (!section) {
                return false;
            }
            return section.isRectangle(fragment);
        }
    };

    var square = {
        id: "Square",
        group: "Shape",
        verify: function(ps, targetSolid, value) {
            var n = math.cross(ps[0], ps[1], ps[2]);
            vec3.normalize(n, n);
            var fragment = targetSolid.clone();
            var section = fragment.cutOff(ps[0], n);
            if (!section) {
                return false;
            }
            return section.isSquare(fragment);
        }
    };

    var conditions = {};
    $.each([
        containsPoint,
        containsLine,
        doesNotContainPoint,
        doesNotContainLine,
        containsNoVertex,
        containsNoEdge,
        parallelToLine,
        parallelToPlane,
        perpendicularToLine,
        perpendicularToPlane,
        cutsVolumeInRatio,
        isoscellesTriangle,
        rightTriangle,
        equilateralTriangle,
        parallelogram,
        rhombus,
        rectangle,
        square], function() {
            conditions[this.id] = this;
    });

    return conditions;
});
