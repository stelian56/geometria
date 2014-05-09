/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare"
], function(declare) {

    return declare(null, {

        constructor: function(name, value) {
            this.name = name;
            this.value = value;
        }
    });
});