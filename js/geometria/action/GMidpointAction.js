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

        label: dict.get("action.Midpoint"),
        
        execute: function(contextMenuTriggered) {
            return actions.divideSegmentAction.execute(contextMenuTriggered, true);
        },
        
        validateSelection: function() {
            var props = actions.divideSegmentAction.validateSelection();
            if (props) {
                var outProps = lang.mixin({ ratio: ["1", "1"] }, props);
                if (actions.divideSegmentAction.validate(outProps)) {
                    return props;
                }
            }
            return null;
        },
        
        undo: function(props) {
            var outProps = lang.mixin({ ratio: ["1", "1"] }, props);
            actions.divideSegmentAction.undo(outProps);
        },
        
        playBack: function(props) {
            var outProps = lang.mixin({ ratio: ["1", "1"] }, props);
            return actions.divideSegmentAction.playBack(outProps);
        },
        
        toTooltip: function(props) {
            return this.toLog(props);
        },

        toLog: function(props) {
            return dict.get("MidpointOfSegment", props.points[0], props.points[1],
                props.figureName);
        },
        
        toJson: function(props) {
            return {
                "action": "midpointAction",
                "props": {
                    "figureName": props.figureName,
                    "points": props.points
                }
            };
        }
        
    };
});
