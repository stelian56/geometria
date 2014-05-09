/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/test/math",
    "geometria/test/solid",
    "geometria/test/utils",
    "geometria/test/dictionary"
], function() {

    var allTestGroups = arguments;

    return {
        run: function() {
            console.log("Start Geometria tests");
            var testGroups;
            var query = window.location.search;
            if (!query) {
                testGroups = allTestGroups;
            }
            else {
                testGroups = [];
                var params = query.slice(1).split("&");
                for (var paramIndex = 0; paramIndex < params.length; paramIndex++) {
                    var param = params[paramIndex];
                    $.each(allTestGroups, function() {
                        if (this.name == param) {
                            testGroups.push(this);
                            return false;
                        }
                    });
                }
            }
            $.each(testGroups, function() {
                var testGroup = this;
                $.each(testGroup, function(name, f) {
                    if (typeof(this) == "function") {
                        var result;
                        try {
                            result = f();
                        }
                        catch (err) {
                            console.error(err);
                        }
                        if (result) {
                            console.info("Test " + testGroup.name + "." + name + " OK");
                        }
                        else {
                            console.warn("Test " + testGroup.name + "." + name + " FAILED");
                        }
                    }
                });
            });
            console.log("End Geometria tests");
        }
    };
});