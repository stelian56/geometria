/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GUtils"
], function(utils) {

    return {
        name: "utils",

        getNewLabel: function() {
            var alphabet = function() {
                var chars = [];
                for (var charCode = "A".charCodeAt(0); charCode <= "Z".charCodeAt(0); charCode++) {
                    chars.push(String.fromCharCode(charCode));
                }
                return chars;
            };
            var test = function(currentLabels, newLabel) {
                var points = {};
                $.each(currentLabels, function(index, label) {
                    points[label] = true;
                });
                var solid = { id: "0", points: points, relatedSolids: {} };
                solid.relatedSolids[solid.id] = solid;
                return utils.getNewLabel(solid) == newLabel;
            };
            var alphabetLessA = alphabet();
            alphabetLessA.splice(0, 1);
            var alphabetLessY = alphabet();
            alphabetLessY.splice(24, 1);
            var alphabetLessZ = alphabet();
            alphabetLessZ.splice(25, 1);
            var alphabetPlusA1 = alphabet();
            alphabetPlusA1.splice(10, 0, "A1");
            var alphabetPlusB1 = alphabet();
            alphabetPlusB1.splice(10, 0, "B1");
            return test(alphabet(), "A1") &&
                    test(alphabet().reverse(), "A1") &&
                    test(alphabetLessA, "A1") &&
                    test(alphabetLessY, "A1") &&
                    test(alphabetLessZ, "Z") &&
                    test(alphabetPlusA1, "B1") &&
                    test(alphabetPlusB1, "C1");
                    
        },
        
        getNextLabel: function() {
            var test = function(currentLabel, nextLabel) {
                return utils.getNextLabel(currentLabel) == nextLabel;
            };
            return test("Y", "Z") &&
                    test("Z", "A1") &&
                    test("Y1", "Z1") &&
                    test("Z1", "A2");
        },
        
        getLabels: function() {
            var test = function(stringValue, labels) {
                var ls = utils.getLabels(stringValue);
                if (ls.length != labels.length) {
                    return false;
                }
                for (var labelIndex = 0; labelIndex < ls.length; labelIndex++) {
                    if (ls[labelIndex] != labels[labelIndex]) {
                        return false;
                    }
                }
                return true;
            };
            return test("AB", ["A", "B"]) &&
                    test("Y12Z1", ["Y12", "Z1"]) &&
                    test("X123Y12Z1", ["X123", "Y12", "Z1"]);
        }
    };
});