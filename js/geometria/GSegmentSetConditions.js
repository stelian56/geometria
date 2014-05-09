/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GAnswerUtils"
], function(answerUtils) {

    var hamiltonianCycle = {
        id: "HamiltonianCycle",
        stringValueFromSelection: answerUtils.segmentSetFromSelection,
        verify: function(segments, targetFigure) {
            var solid = targetFigure.solid;
            var vertices = {};
            $.each(solid.points, function(label, p) {
                if (p.isVertex) {
                    vertices[label] = p;
                }
            });
            var occurrences = {};
            valid = true;
            $.each(segments, function() {
                var segment = this;
                $.each([segment.p1, segment.p2], function(index, label) {
                    if (!vertices[label]) {
                        valid = false;
                        return false;
                    }
                    var occurrence = occurrences[label];
                    if (!occurrence) {
                        occurrence = 0;
                    }
                    else if (occurrence > 1) {
                        valid = false;
                        return false;
                    }
                    occurrences[label] = occurrence + 1;
                });
                if (!valid) {
                    return false;
                }
            });
            if (valid) {
                if (occurrences.length != vertices.length) {
                    valid = false;
                }
                $.each(occurrences, function(label, occurrence) {
                    if (occurrence != 2) {
                        valid = false;
                        return false;
                    }
                });
            }
            if (valid) {
                solid.selection = {};
                $.each(segments, function() {
                    var label1 = this.p1;
                    var label2 = this.p2;
                    var code = label1 < label2 ? label1 + label2 : label2 + label1;
                    var stick = {
                        p1: solid.points[label1],
                        p2: solid.points[label2]
                    };
                    solid.selection[code] = stick;
                });
                targetFigure.draw();
            }
            return valid;
        }
    };

    var conditions = {};
    $.each([hamiltonianCycle], function() {
        conditions[this.id] = this;
    });

    return conditions;
});
