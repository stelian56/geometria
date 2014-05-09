/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dijit/Toolbar",
    "dijit/ToolbarSeparator",
    "dijit/Tooltip",
    "dijit/form/Button",
    "geometria/GActions"
], function(Toolbar, ToolbarSeparator, Tooltip, Button, allActions) {

    var toolBarActions = [
        "navigatorAction",
        "|",
        "newProblemAction",
        "solveProblemAction",
        "lockProblemAction",
        "saveDocumentAction",
        "propertiesAction",
        "|",
        "undoAction",
        "redoAction",
        "eraseSelectionAction",
        "removeFigureAction",
        "|",
        "cloneAction",
        "saveFigureAction",
        "printFigureAction",
        "|",
        "measureDistanceAction",
        "measureAngleAction",
        "volumeAction",
        "areaAction",
        "|",
        "joinPointsAction",
        "perpendicularAction",
        "intersectionAction",
        "divideSegmentAction",
        "divideAngleAction",
        "layDistanceAction",
        "layAngleAction",
        "|",
        "scaleAction",
        "shearAction",
        "cutAction",
        "joinFiguresAction",
        "|",
        "selectorAction",
        "zoomInAction",
        "zoomOutAction",
        "fitToViewAction",
        "wireframeAction",
        "labelsAction",
        "colorAction",
        "|",
        "helpAction"
    ];
    var buttonCount = 0;
    var separatorCount = 0;
    
    return {
        startUp: function() {

            var dojoToolBar = new Toolbar(arguments[0]);

            var make = function() {
            
                var tooltip = function(action, button) {
                    var tooltipLabel;
                    if (action.tooltip) {
                        tooltipLabel = action.tooltip();
                    }
                    if (tooltipLabel && tooltipLabel.length) {
                        Tooltip.show(tooltipLabel, button.domNode);
                    }
                    else {
                        Tooltip.hide(button.domNode);
                    }
                };
            
                $.each(toolBarActions, function() {
                    if (this == "|") {
                        dojoToolBar.addChild(new ToolbarSeparator());
                        separatorCount++;
                    }
                    else {
                        var action = allActions[this];
                        var button = new Button({
                            iconClass: action.icon,
                            showLabel: false,
                            onClick: function() {
                                action.base.execute();
                                tooltip(action, this);
                            },
                            onMouseOver: function() {
                                tooltip(action, this);
                            },
                            onMouseLeave: function() {
                                Tooltip.hide(this.domNode);
                            }
                        });
                        if (!action.tooltip) {
                            button.set("label", action.label);
                        }
                        action.base.addStateObserver(function() {
                            button.set("disabled", !action.base.enabled);
                            if (action.base.active) {
                                $(button.domNode).addClass("dijitToggleButtonChecked");
                            }
                            else {
                                $(button.domNode).removeClass("dijitToggleButtonChecked");
                            }
                        });
                        dojoToolBar.addChild(button);
                        buttonCount++;
                    }
                });
            };
            
            make();
            dojoToolBar.startup();
            return dojoToolBar;
        },
        
        getPreferredHeight: function(width) {
            return Math.ceil((buttonCount*32 + separatorCount*7)/(width - 4))*32 + 30;
        }
    };
});
