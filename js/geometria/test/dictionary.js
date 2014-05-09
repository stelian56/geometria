/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/request"
], function(request) {

    var languages = ["en", "es"];
    var getRegex = /dict\.get\("([\w\.]+)"[,\)]/g;
    var srcRegex = /"(geometria\/.+)",/g;
    
    return {
        name: "dictionary",

        checkKeys: function() {
            var test = function() {
                var valid = true;
                var dictionaries = [];
                $.each(languages, function() {
                    var lang = this;
                    var path = "geometria/dictionary/dictionary-" + lang;
                    require([path], function(dictionary) {
                        dictionaries.push(dictionary);
                    });
                });
                var dictCount = 0;
                var match;
                $.each(dictionaries, function() {
                    var dictionary = this;

                    var srcOnSuccess = function(webdata) {
                        while ((match = getRegex.exec(webdata)) != null) {
                            var key = match[1];
                            if (!key.startsWith("menu.")) {
                                if (!dictionary[key]) {
                                    valid = false;
                                    break;
                                }
                            }
                        }
                    };
                   
                    var onError = function() {
                        valid = false;
                    };

                    var profileOnSuccess = function(webdata) {
                        while ((match = srcRegex.exec(webdata)) != null) {
                            var srcUrl = match[1] + ".js";
                            request(srcUrl, {
                                sync: true
                            }).then(srcOnSuccess, onError);
                            if (!valid) {
                                return false;
                            }
                        }
                    };
                    
                    request("profile.js", {
                        sync: true
                    }).then(profileOnSuccess, onError);
                    if (!valid) {
                        return false;
                    }
                    if (!valid) {
                        return false;
                    }
                });
                return valid;
            };
            
            return test();
        }
    };
});