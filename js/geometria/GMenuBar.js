/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dijit/DropDownMenu",
    "dijit/MenuBar",
    "dijit/MenuItem",
    "dijit/MenuSeparator",
    "dijit/PopupMenuBarItem",
    "dijit/PopupMenuItem",
    "dijit/Tooltip",
    "geometria/GActions",
    "geometria/GDictionary"
], function(DropDownMenu, MenuBar, MenuItem, MenuSeparator, PopupMenuBarItem, PopupMenuItem,
        Tooltip, allActions, dict) {

    var actionGroups = {};
    actionGroups["Document"] = [
        "newProblemAction",
        "solveProblemAction",
        "lockProblemAction",
        "|",
        "saveDocumentAction",
        "saveDocumentAsAction",
        "|",
        "answerAction",
        "propertiesAction",
        "|",
        "exportDocumentAction",
        "importDocumentAction",
        "|",
        "languageAction",
        "exitAction"
    ];
    actionGroups["Edit"] = [
        "undoAction",
        "redoAction",
        "|",
        "renamePointAction",
        "renameVariableAction",
        "renameFigureAction",
        "|",
        "eraseSegmentAction",
        "eraseSelectionAction",
        "removeFigureAction",
        "clearNotepadAction",
        "|",
        "selectAllAction",
        "deselectAllAction"
    ];
    var platonicSolidActions = {};
    platonicSolidActions["PlatonicSolids"] = [
        "tetrahedronAction",
        "cubeAction",
        "octahedronAction",
        "dodecahedronAction",
        "icosahedronAction"
    ];
    var galleryActions = {};
    galleryActions["Gallery"] = [
        "prismAction",
        "pyramidAction",
        platonicSolidActions
    ];
    actionGroups["Figure"] = [
        galleryActions,
        "|",
        "cloneAction",
        "saveFigureAction",
        "|",
        "printFigureAction"
    ];
    actionGroups["Measure"] = [
        "measureDistanceAction",
        "measureAngleAction",
        "|",
        "volumeAction",
        "areaAction",
        "totalAreaAction"
    ];
    actionGroups["Draw"] = [
        "joinPointsAction",
        "perpendicularAction",
        "intersectionAction",
        "|",
        "midpointAction",
        "divideSegmentAction",
        "|",
        "bisectorAction",
        "divideAngleAction",
        "|",
        "layDistanceAction",
        "layAngleAction"
    ];
    actionGroups["Transform"] = [
        "scaleAction",
        "shearAction",
        "|",
        "cutAction",
        "joinFiguresAction"
    ];
    actionGroups["View"] = [
        "navigatorAction",
        "selectorAction",
        "|",
        "zoomInAction",
        "zoomOutAction",
        "fitToViewAction",
        "|",
        "initialAttitudeAction",
        "defaultAttitudeAction",
        "|",
        "wireframeAction",
        "labelsAction",
        "colorAction"
    ];
    actionGroups["Help"] = [
        "helpAction",
        "homePageAction"
    ];

    return {
        actionGroups: actionGroups,
    
        startUp: function() {
        
            var make = function(menu, menuActions) {
                $.each(menuActions, function(label, actions) {
                    var childMenu = new DropDownMenu();
                    var PopupMenuConstructor =
                        menu instanceof MenuBar ? PopupMenuBarItem : PopupMenuItem;
                    menu.addChild(new PopupMenuConstructor({
                        label: dict.get("menu." + label),
                        popup: childMenu
                    }));
                    $.each(actions, function() {
                        var action = allActions[this];
                        if (action) {
                            var menuItem;
                            if (action.popup)  {
                                menuItem = new PopupMenuItem({
                                    label: action.label,
                                    iconClass: action.icon
                                });
                                var popup = action.popup(menuItem);
                                menuItem.popup = popup;
                                childMenu.addChild(menuItem);
                            }
                            else {
                                menuItem = new MenuItem({
                                    label: action.label,
                                    iconClass: action.icon,
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
                            }
                            childMenu.addChild(menuItem);
                            action.base.addStateObserver(function() {
                                menuItem.set("disabled", !action.base.enabled);
                            });
                        }
                        else if (this == "|") {
                            childMenu.addChild(new MenuSeparator());
                        }
                        else {
                            make(childMenu, this);
                        }
                    });
                });
            };
            
            var dojoMenuBar = new MenuBar(arguments[0]);
            make(dojoMenuBar, actionGroups);
            dojoMenuBar.startup();
            return dojoMenuBar;
        }
    };
});
