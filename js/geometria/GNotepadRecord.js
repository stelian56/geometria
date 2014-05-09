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
    "geometria/GCalculation",
    "geometria/GMainContainer",
    "geometria/GMeasurement",
    "geometria/GUtils",
    "geometria/GVariable"
], function(declare, lang, GCalculation, mainContainer, GMeasurement, utils, GVariable) {

    return declare(null, {

        constructor: function(variable, recordable) {
            this.variable = variable;
            this.recordable = recordable;
        },
        
        make: function(props) {
            if (props.measurement) {
                new GMeasurement().make(props);
            }
            else if (props.calculation) {
                new GCalculation().make(props);
            }
            else {
                throw "Notepad record is neither measurement, nor calculation";
            }
        },
  
        figureRenamed: function(oldName, newName) {
            if (this.recordable instanceof GMeasurement) {
                this.recordable.figureRenamed(oldName, newName);
            }
        },

        pointRenamed: function(oldLabel, newLabel, figureName) {
            if (this.recordable instanceof GMeasurement) {
                this.recordable.pointRenamed(oldLabel, newLabel, figureName);
            }
        },
        
        variableRenamed: function(oldName, newName) {
            if (this.variable.name == oldName) {
                this.variable.name = newName;
            }
            else if (this.recordable instanceof GCalculation) {
                this.recordable.variableRenamed(oldName, newName);
            }
        },
        
        figureRemoved: function(figureName, executeId) {
            if (this.recordable instanceof GMeasurement) {
                this.recordable.figureRemoved(figureName, executeId);
            }
        },
            
        removeFigureUndone: function(executeId) {
            if (this.recordable instanceof GMeasurement) {
                this.recordable.removeFigureUndone(executeId);
            }
        },

        pointRemoved: function(label, figureName, executeId) {
            if (this.recordable instanceof GMeasurement) {
                this.recordable.pointRemoved(label, figureName, executeId);
            }
        },

        removePointUndone: function(executeId) {
            if (this.recordable instanceof GMeasurement) {
                this.recordable.removePointUndone(executeId);
            }
        },

        figureTransformed: function(figureName, executeId) {
            if (this.recordable instanceof GMeasurement) {
                this.recordable.figureTransformed(figureName, executeId);
            }
        },
            
        transformFigureUndone: function(executeId) {
            if (this.recordable instanceof GMeasurement) {
                this.recordable.transformFigureUndone(executeId);
            }
        },
        
        toJson: function() {
            var json = {
                "variableName": this.variable.name
            };
            if (!this.recordable.deprecated) {
                var recordableJson = this.recordable.toJson();
                lang.mixin(json, recordableJson);
            }
            return json;
        },
        
        toString: function() {
            var stringValue = "<span class='geometria_variablename'>" + this.variable.name +
                "</span>";
            if (this.recordable instanceof GCalculation) {
                stringValue += " = " + this.recordable.expression;
            }
            else if (!this.recordable.deprecated) {
                stringValue += " = " + this.recordable + " : <span class='geometria_figurename'>" +
                    this.recordable.figureName + "</span>";
            }
            return stringValue;
        }
    });
});