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
], function(Toolbar, ToolbarSeparator, Tooltip, Button, actions) {

    var toolBarActions = [
        "navigatorAction",
        "newFolderAction",
        "openAction",
        "renameFileAction",
        "removeFileAction"
    ];
    
    return {
        startUp: function() {

            var dojoToolBar = new Toolbar(arguments[0]);

            var make = function() {
                $.each(toolBarActions, function() {
                    if (this == "|") {
                        dojoToolBar.addChild(new ToolbarSeparator());
                    }
                    else {
                        var action = actions[this];
                        var button = new Button({
                            iconClass: action.icon,
                            showLabel: false,
                            onClick: function() {
                                action.base.execute();
                            },
                            onMouseOver: function() {
                                if (action.tooltip) {
                                    var tooltipLabel = action.tooltip();
                                    if (tooltipLabel.length) {
                                        Tooltip.show(tooltipLabel, this.domNode);
                                    }
                                }
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
                    }
                });
            };
            
            make();
            dojoToolBar.startup();
            return dojoToolBar;
        }
    };
});
