/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "geometria/GMainContainer"
], function(mainContainer) {
    return {
        startUp: function(baseUrl) {
            mainContainer.startUp(baseUrl);
        }
    };
});
