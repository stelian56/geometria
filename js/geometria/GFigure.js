/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "dojo/_base/lang",
    "dijit/Menu",
    "dijit/layout/ContentPane",
    "geometria/GActions",
    "geometria/GCamera",
    "geometria/GDictionary",
    "geometria/GFigureMouseAdapter",
    "geometria/GSolid",
    "geometria/GUtils"
], function(declare, lang, Menu, ContentPane,
            actions, GCamera, dict, GFigureMouseAdapter, GSolid, utils) {

    return declare(null, {

        constructor: function(name, solid) {
            this.name = name;
            this.scalingFactor = null;
            this.solid = solid;
            this.camera = new GCamera(this);
            this.selected = false;
            this.labeled = true;
            this._wireframe = true;
            this.color = utils.faceColor;
            this.pane = null;
            this.contextMenu = null;
            this.svg = null;
        },
        
        clone: function() {
            var solid = this.solid.clone();
            var figure = new this.constructor(this.name, solid);
            figure.camera.initialAttitude = mat3.clone(this.camera.initialAttitude);
            figure.camera.toInitialAttitude();
            return figure;
        },
        
        make: function(props) {
            if (!props.name || !utils.figureNameRegex.test(props.name)) {
                throw "Bad figure name: " + props.name;
            }
            this.name = props.name;
            this.labeled = props.labeled && true;
            this._wireframe = props.wireframe && true;
            if (!props.color) {
                throw "Figure " + this.name + " has no color";
            }
            this.color = props.color;
            if (!props.solid) {
                throw "Figure " + this.name + " has no solid";
            }
            this.solid = new GSolid().make(props.solid);
            this.camera.make(props.camera);
            return this;
        },

        startUp: function() {
            var figure = this;
            figure.scalingFactor = null;
            figure.svg = null;
            if (figure.pane) {
                figure.pane.destroyRecursive();
            }
            var mouseAdapter = new GFigureMouseAdapter(figure);
            figure.pane = new ContentPane({
                "class": "geometria_figure",
                title: figure.name,
                onMouseDown: function(event) {
                    dojo.stopEvent(event);
                    mouseAdapter.mouseDown(event);
                },
                onMouseUp: function(event) {
                    dojo.stopEvent(event);
                    mouseAdapter.mouseUp(event);
                },
                onMouseMove: function(event) {
                    dojo.stopEvent(event);
                    mouseAdapter.mouseMove(event);
                },
                onMouseLeave: function(event) {
                    dojo.stopEvent(event);
                    mouseAdapter.mouseLeave(event);
                }
            });
            figure.contextMenu = new Menu({
                targetNodeIds: [figure.pane.domNode],
                onOpen: function() {
                    figure.solid.updateContextMenu(figure.contextMenu);
                }
            });
            return figure.pane;
        },

        _getViewSize: function() {
            var $domNode = $(this.pane.domNode);
            return {width: $domNode.width(), height: $domNode.height()};
        },

        _getSvgSize: function() {
            var $svg = $(this.svg);
            return { width: $svg.attr("width"), height: $svg.attr("height") };
        },
        
        _getFittingScalingFactor: function() {
            var radius = this.solid.boundingSphere.radius;
            var size = this._getViewSize();
            return 0.5*Math.min(size.width/radius, size.height/radius) / (1 + utils.figureMargin);
        },
        
        zoom: function(factor) {
            this.scalingFactor *= factor;
            var span = 2*(this.scalingFactor*this.solid.boundingSphere.radius*(1 +
                utils.figureMargin));
            $svg = $(this.svg);
            var width, height;
            if (factor > 1) {
                width = Math.max($svg.width(), span);
                height = Math.max($svg.height(), span);
            }
            else {
                $domNode = $(this.pane.domNode);
                width = Math.min($svg.width(), span);
                width = Math.max(width, $domNode.width());
                height = Math.min($svg.height(), span);
                height = Math.max(height, $domNode.height());
            }
            $svg.attr({
                width: width - 16,
                height: height - 16
            });
            this.draw();
        },
        
        fitToView: function() {
            this.scalingFactor = this._getFittingScalingFactor();
            $domNode = $(this.pane.domNode);
            $(this.svg).attr({
                width: $domNode.width() - 16,
                height: $domNode.height() - 16
            });
            this.draw();
        },
        
        isInView: function() {
            return this.scalingFactor <= this._getFittingScalingFactor();
        },
        
        getOffset: function() {
            return $(this.pane.domNode).offset();
        },
        
        toggleWireframe: function() {
            this._wireframe = !this._wireframe;
            if (this._wireframe) {
                this.solid.hideFaces();
            }
            else {
                this.solid.hideWireframe();
            }
        },
   
        isWireframe: function() {
            return this._wireframe;
        },
   
        select: function(event) {
            var offset = this.getOffset();
            var $domNode = $(this.pane.domNode);
            var scrX = event.clientX + $domNode.scrollLeft() - offset.left;
            var scrY = event.clientY + $domNode.scrollTop() - offset.top;
            var size = this._getSvgSize();
            var paddingLeft = parseInt($domNode.css("padding-left"));
            var paddingTop = parseInt($domNode.css("padding-top"));
            var projX = (scrX - 0.5*size.width - paddingLeft)/this.scalingFactor;
            var projY = (0.5*size.height - scrY + paddingTop)/this.scalingFactor;
            this.solid.select(projX, projY, this);
            this.draw();
            actions.updateStates();
        },

        selectAll: function() {
            this.solid.selectAll();
            this.draw();
        },
        
        deselectAll: function() {
            this.solid.selection = {};
            this.draw();
        },
        
        draw: function(refresh) {
            if (!this.scalingFactor) {
                this.scalingFactor = this._getFittingScalingFactor();
            }
            var size = this.svg ? this._getSvgSize() : this._getViewSize();
            if (refresh) {
                $(this.svg).remove();
                this.svg = null;
            }
            if (!this.svg) {
                this.svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
                $(this.svg).attr({
                    width: size.width - 16,
                    height: size.height - 16
                });
                this.pane.setContent(this.svg);
                this.solid.initSvg(this.svg);
            }
            this.solid.draw(this.camera, this.svg, this.scalingFactor, size, this.labeled,
                    !this._wireframe && this.color);
        },
        
        toJson: function() {
            var json = {
                "name": this.name,
                "labeled": this.labeled,
                "wireframe": this._wireframe,
                "color": this.color,
                "camera": this.camera.toJson()
            };
            lang.mixin(json, this.solid.toJson());
            return json;
        }
    });
});