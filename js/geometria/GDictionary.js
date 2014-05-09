/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/cookie"
], function(cookie) {

    var cookieName = "Language";
    var dictionaries = {};
    var dictionary;
    var language;

    var setLanguage = function(lang) {
        dictionary = dictionaries[lang];
        if (!dictionary) {
            var path = "geometria/dictionary/dictionary-" + lang;
            require([path], function(dict) {
                dictionaries[lang] = dict;
                dictionary = dict;
            });
        }
        language = lang;
        dojo.cookie("Language", lang, {expires: 365});
    };

    var init = function() {
        var lang;
        var query = window.location.search;
        if (query) {
            var params = query.slice(1).split("&");
            for (var paramIndex = 0; paramIndex < params.length; paramIndex++) {
                var param = params[paramIndex];
                var tokens = param.split("=");
                if (tokens.length == 2 && tokens[0] == "lang") {
                    lang = tokens[1];
                    break;
                }
            }
        }
        if (!lang) {
            var lang = dojo.cookie(cookieName);
            if (!lang) {
                lang = "en";
            }
        }
        setLanguage(lang);
    }

    init();
    
    return {
        get: function() {
            var key = arguments[0];
            var value = dictionary[key];
            if (!value) {
                throw "No such key '" + key + "' in dictionary";
            }
            var argIndex;
            for (argIndex = 1; argIndex < arguments.length; argIndex++) {
                var arg = arguments[argIndex];
                var regex = new RegExp("\\$\\{" + argIndex + "\\}", "g");
                value = value.replace(regex, arg);
            }
            return value;
        },

        setLanguage: function(lang) {
            setLanguage(lang);
        },

        language: language
    };
});
