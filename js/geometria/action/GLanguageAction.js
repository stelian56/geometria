/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dijit/form/Select",
    "geometria/GDictionary",
    "geometria/GMainContainer"
], function(Select, dict, mainContainer) {

    var languages = {
        "en": "English",
        "es": "Español"
    };

    return {

        label: dict.get("action.Language"),

        popup: function(parent) {
            var action = this;
            var options = [];
            $.each(languages, function(language, label) {
                options.push({ label: label, value: language });
            });
            return new Select({
                options: options,
                value: dict.language,
                onChange: function(newValue) {
                    var props = { language: newValue };
                    action.base.execute(props);
                }
            });
        },

        execute: function(props) {
            mainContainer.onCloseDocument().then(function() {
                dict.setLanguage(props.language);
                location.reload();
            });
        }
    };
});
