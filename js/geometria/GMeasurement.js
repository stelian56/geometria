/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "geometria/GActions",
    "geometria/GDictionary"
], function(declare, actions, dict) {

    return declare(null, {

        constructor: function(type, props) {
            this.deprecated = false;
            this._deprecatingExecuteId = null;
            switch(type) {
            case actions.measureDistanceAction.measurementType:
                this.figureName = props.figureName;
                this.expression = actions.measureDistanceAction.getExpression(props);
                break;
            case actions.measureAngleAction.measurementType:
                this.figureName = props.figureName;
                this.expression = actions.measureAngleAction.getExpression(props);
                break;
            case actions.volumeAction.measurementType:
                this.figureName = props.figureName;
                this.expression = actions.volumeAction.getExpression();
                break;
            case actions.areaAction.measurementType:
                this.figureName = props.figureName;
                this.expression = actions.areaAction.getExpression(props);
                break;
            case actions.totalAreaAction.measurementType:
                this.figureName = props.figureName;
                this.expression = actions.totalAreaAction.getExpression();
                break;
            }
        },
        
        make: function(props) {
            this.expression = props.measurement.expression;
            this.figureName = props.measurement.figureName;
            var outProps = {
                variableName: props.variableName,
                expression: this.expression,
                figureName: this.figureName
            };
            if (!outProps.expression) {
                throw "Missing expression in measurement";
            }
            var action;
            if (actions.measureDistanceAction.expressionRegex.test(outProps.expression)) {
                action = actions.measureDistanceAction;
            }
            else if (actions.measureAngleAction.expressionRegex.test(outProps.expression)) {
                action = actions.measureAngleAction;
            }
            else if (actions.volumeAction.expressionRegex.test(outProps.expression)) {
                action = actions.volumeAction;
            }
            else if (actions.areaAction.expressionRegex.test(outProps.expression)) {
                action = actions.areaAction;
            }
            else if (actions.totalAreaAction.expressionRegex.test(outProps.expression)) {
                action = actions.totalAreaAction;
            }
            else {
                throw "Bad measurement expression " + this.expression;
            }
            action.evaluate(outProps);
        },

        figureRenamed: function(oldName, newName) {
            if (!this.deprecated && this.figureName == oldName) {
                this.figureName = newName;
            }
        },
        
        figureRemoved: function(figureName, executeId) {
            if (!this.deprecated) {
                if (this.figureName == figureName) {
                    this.deprecated = true;
                    this._deprecatingExecuteId = executeId;
                }
            }
        },
        
        removeFigureUndone: function(executeId) {
            if (this.deprecated && this._deprecatingExecuteId == executeId) {
                this.deprecated = false;
            }
        },
        
        figureTransformed: function(figureName, executeId) {
            if (!this.deprecated) {
                if (this.figureName == figureName) {
                    this.deprecated = true;
                    this._deprecatingExecuteId = executeId;
                }
            }
        },
        
        transformFigureUndone: function(executeId) {
            if (this.deprecated && this._deprecatingExecuteId == executeId) {
                this.deprecated = false;
            }
        },

        pointRenamed: function(oldLabel, newLabel, figureName) {
            if (!this.deprecated && this.figureName == figureName) {
                var expression = " " + this.expression + " ";
                var regex = new RegExp("(.*)" + oldLabel + "([^0-9].*)");
                while (regex.test(expression)) {
                    expression = expression.replace(regex, "$1" + newLabel + "$2");
                }
                this.expression = expression.substring(1, expression.length -1);
            }
        },
        
        pointRemoved: function(label, figureName, executeId) {
            if (!this.deprecated) {
                if (this.figureName == figureName) {
                    this.deprecated = true;
                    this._deprecatingExecuteId = executeId;
                }
            }
        },

        removePointUndone: function(executeId) {
            if (this.deprecated && this._deprecatingExecuteId == executeId) {
                this.deprecated = false;
            }
        },

        toJson: function() {
            var json = {
                "measurement": {
                    "expression": this.expression,
                    "figureName": this.figureName
                }
            };
            return json;
        },
        
        toString: function() {
            if (actions.measureAngleAction.expressionRegex.test(this.expression)) {
                return this.expression.replace("<", "&lt;");
            }
            if (actions.volumeAction.expressionRegex.test(this.expression)) {
                return dict.get("volume");
            }
            if (actions.areaAction.expressionRegex.test(this.expression)) {
                return this.expression.replace("area", dict.get("area"));
            }
            if (actions.totalAreaAction.expressionRegex.test(this.expression)) {
                return this.expression.replace("totalArea", dict.get("totalArea"));
            }
            return this.expression;
        }
    });
});