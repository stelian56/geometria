/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "geometria/GActions",
    "geometria/GDictionary"
], function(lang, actions, dict) {

    return {

        loggable: true,
    
        figureSpecific: true,

        label: dict.get("action.Bisector"),
        
        execute: function(contextMenuTriggered) {
            return actions.divideAngleAction.execute(contextMenuTriggered, true);
        },
        
        validateSelection: function() {
            var props = actions.divideAngleAction.validateSelection();
            if (props) {
                var outProps = lang.mixin({ ratio: ["1", "1"] }, props);
                if (actions.divideAngleAction.validate(outProps)) {
                    return props;
                }
            }
            return null;
        },
        
        undo: function(props) {
            var outProps = lang.mixin({ ratio: ["1", "1"] }, props);
            actions.divideAngleAction.undo(outProps);
        },
        
        playBack: function(props) {
            var outProps = lang.mixin({ ratio: ["1", "1"] }, props);
            return actions.divideAngleAction.playBack(outProps);
        },
        
        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            return dict.get("BisectorOfAngle", props.points[0], props.points[1], props.points[2],
                props.figureName);
        },
        
        toJson: function(props) {
            return {
                "action": "bisectorAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points
                }
            };
        }
        
    };
});
