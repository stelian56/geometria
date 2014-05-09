/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "dojo/promise/Promise",
    "geometria/GActions",
    "geometria/GFiguresContainer",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GSolution"
], function(declare, Promise, actions, figuresContainer, logContainer, mainContainer, GSolution) {

    var executeId = 0;

    return declare(null, {
        constructor: function(action) {
            this.action = action;
            this.enabled = null;
            this.active = false;
            this.stateObservers = [];
        },

        addStateObserver: function(observer) {
            this.stateObservers.push(observer);
        },
        
        execute: function() {
            var action = this.action;

            var onSuccess = function(props) {
                var doc = mainContainer.currentDocument;
                if (action.loggable && doc instanceof GSolution) {
                    var record = { action: action, props: props };
                    logContainer.add(record);
                }
                if (action.undo && props) {
                    var record = { action: action, props: props };
                    actions.queueAction(record);
                }
                actions.updateStates();
            };
            
            var result = action.execute.apply(this, arguments);
            if (result instanceof Promise) {
                result.then(function(props) {
                    onSuccess(props);
                });
            }
            else if (result) {
                onSuccess(result);
            }
        },

        playBack: function(props, external) {
            if (this.action.figureSpecific) {
                figuresContainer.select(props.figureName);
            }
            return this.action.playBack(props, external);
        },
        
        updateState: function() {
            this.enabled = true;
            var action = this.action;
            if (action.updateState) {
                action.updateState();
            }
            if (this.enabled) {
                var doc = mainContainer.currentDocument;
                if (action.figureSpecific && doc.figures.length < 1) {
                    this.enabled = false;
                }
                if (this.enabled && doc instanceof GSolution) {
                    if (action.loggable) {
                        var log = doc.log;
                        var logLength = log.length;
                        if (logLength > 0 && log[logLength - 1].action.logEnd) {
                            this.enabled = false;
                        }
                    }
                    if (this.enabled && !action.enableAtPlayBack &&
                            logContainer.isPlaybackActive()) {
                        this.enabled = false;
                    }
                }
            }
            $.each(this.stateObservers, function() {
                this();
            });
        },
   
        getExecuteId: function() {
            return ++executeId;
        },
   
        toJson: function(props, comments) {
            var json = this.action.toJson(props);
            if (comments) {
                json.comments = comments;
            }
            return json;
        }
    });
});