/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "dojo/mouse",
    "geometria/GFiguresContainer"
], function(declare, mouse, figuresContainer) {

    var MAX_SPIN_START_DELAY = 250; // ms

    return declare(null, {

        constructor: function(figure) {
            this.figure = figure;
            this._mouseProps = {};
        },

        startUp: function() {
            this.pane = new ContentPane({
                "class": "geometria_figure",
                title: this.name
            });
            addEvents(this);
            this.svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            $(this.svg).appendTo(this.pane.domNode);
            return this.pane;
        },
        
        mouseUp: function(event) {
            var figure = this.figure;
            if (mouse.isLeft(event)) {
                var $domNode = $(figure.pane.domNode);
                $domNode.css("cursor", "auto");
                dojo.mixin(this._mouseProps, {
                    down: null,
                    up: {
                        x: event.clientX + $domNode.scrollLeft(),
                        y: event.clientY + $domNode.scrollTop()
                    }
                });
            }
        },
        
        mouseLeave: function(event) {
            this._mouseProps.down = null;
        },
        
        mouseDown: function(event) {
            var figure = this.figure;
            var mouseProps = this._mouseProps;
            if (mouse.isLeft(event)) {
                var $domNode = $(figure.pane.domNode);
                dojo.mixin(mouseProps, {
                    up: null,
                    down: {
                        x: event.clientX + $domNode.scrollLeft(),
                        y: event.clientY + $domNode.scrollTop()
                    }
                });
                figure.camera.seize();
                if (figuresContainer.selectorActive) {
                    figure.select(event);
                }
                else {
                    $domNode.css("cursor", "move");
                }
            }
        },
        
        mouseDrag: function(event) {
            if (!figuresContainer.selectorActive) {
                var figure = this.figure;
                var mouseProps = this._mouseProps;
                var $domNode = $(figure.pane.domNode);
                var x = event.clientX + $domNode.scrollLeft();
                var y = event.clientY + $domNode.scrollTop();
                var dx = x - mouseProps.down.x;
                var dy = y - mouseProps.down.y;
                var axis = vec3.fromValues(dy, dx, 0);
                var length = Math.sqrt(dx*dx+ dy*dy);
                figure.camera.turn(axis, length);
                dojo.mixin(mouseProps, {
                    down: {
                        x: x,
                        y: y
                    },
                    lastStroke: {
                        length: Math.max(length, 1),
                        time: Math.max(new Date().getTime(), MAX_SPIN_START_DELAY)
                    },
                    spinEligible: true
                });
            }
        },
        
        mouseMove: function(event) {
            if (!figuresContainer.selectorActive) {
                var figure = this.figure;
                var mouseProps = this._mouseProps;
                if (this._mouseProps.down) {
                    this.mouseDrag(event);
                }
                else {
                    var $domNode = $(figure.pane.domNode);
                    var x = event.clientX + $domNode.scrollLeft();
                    var y = event.clientY + $domNode.scrollTop();
                    if (mouseProps.up) {
                        var dx = x - mouseProps.up.x;
                        var dy = y - mouseProps.up.y;
                    }
                    if (mouseProps.lastStroke && new Date().getTime() -
                            mouseProps.lastStroke.time < MAX_SPIN_START_DELAY &&
                            mouseProps.spinEligible) {
                        var axis = vec3.fromValues(dy, dx, 0);
                        figure.camera.spin(axis, mouseProps.lastStroke.length);
                    }
                    dojo.mixin(mouseProps, {
                        up: {
                            x: x,
                            y: y
                        },
                        spinEligible: false
                    });
                }
            }
        }
    });
});
