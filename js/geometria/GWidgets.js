/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "dojo/Deferred",
    "dijit/Dialog",
    "dijit/Tooltip",
    "dijit/form/Button",
    "dijit/form/RadioButton",
    "dijit/form/Select",
    "dijit/form/SimpleTextarea",
    "dijit/form/TextBox",
    "dijit/form/ValidationTextBox",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GDictionary",
    "geometria/GHelp"
], function(lang, Deferred, Dialog, Tooltip, Button, RadioButton, Select, SimpleTextarea, TextBox,
            ValidationTextBox, ContentPane, LayoutContainer,
            dict, help) {

    return {

        okDialog: function(message, title, iconClass) {
            var dialog;
            var deferredOk = new Deferred();
            var container = new LayoutContainer({
                "class": "geometria_okdialog"
            });
            if (iconClass) {
                var iconPane = new ContentPane({
                    "class": iconClass,
                    region: "left"
                });
                container.addChild(iconPane);
            }
            var messagePane = new ContentPane({
                region: "center",
                content: message
            });
            container.addChild(messagePane);
            var buttonPane = new LayoutContainer({
                "class": "geometria_buttonspane",
                region: "bottom",
                style: "text-align:center;"
            });
            var okButton = new Button({
                "class": "geometria_button",
                region: "center",
                label: dict.get("OK"),
                tabIndex: 1001,
                onClick: function() {
                    this.set("disabled", true);
                    dialog.hide().then(function() {
                        deferredOk.resolve();
                    });
                }
            });
            buttonPane.addChild(okButton);
            container.addChild(buttonPane);
            dialog = new Dialog({
                title: title || "Geometria",
                content: container,
                onHide: function() {
                    this.destroyRecursive();
                }
            });
            dialog.show();
            return { ok: deferredOk.promise };
        },

        errorDialog: function(message) {
            return this.okDialog(message, dict.get("Error"), "geometriaIconError");
        },

        okHelpDialog: function(content, helpTopic, contentClass, okButtonLabel, title,
                inputEnters) {
            var dialog;
            var deferredOk = new Deferred();
            content.set("region", "center");
            var container = new LayoutContainer({
                "class": contentClass
            });
            container.addChild(content);
            var buttonsPane = new LayoutContainer({
                "class": "geometria_buttonspane",
                region: "bottom"
            });
            leftPane = new LayoutContainer({
                region: "center"
            });
            var helpButton = new Button({
                "class": "geometria_button",
                region: "left",
                label: dict.get("Help"),
                tabIndex: 1002,
                onClick: function() {
                    this.set("disabled", true);
                    dialog.hide().then(function() {
                        help.newTopic(helpTopic);
                    });
                }
            });
            leftPane.addChild(helpButton);
            var okButton = new Button({
                "class": "geometria_button",
                region: "right",
                label: okButtonLabel || dict.get("OK"),
                tabIndex: 1001,
                onClick: function() {
                    this.set("disabled", true);
                    deferredOk.resolve();
                    dialog.hide();
                }
            });
            leftPane.addChild(okButton);
            buttonsPane.addChild(leftPane);
            container.addChild(buttonsPane);
            if (inputEnters) {
                $.each(inputEnters, function() {
                    this.then(function() {
                        deferredOk.resolve();
                        dialog.hide();
                    });
                });
            }
            dialog = new Dialog({
                title: title || "Geometria",
                content: container,
                onHide: function() {
                    this.destroyRecursive();
                }
            });
            dialog.show();
            return { ok: deferredOk.promise, okButton: okButton };
        },

        okCancelHelpDialog: function(content, helpTopic, contentClass, title, inputEnters) {
            var dialog;
            var deferredOk = new Deferred();
            var deferredCancel = new Deferred();
            var deferredHelp = new Deferred();
            content.set("region", "center");
            var container = new LayoutContainer({
                "class": contentClass
            });
            container.addChild(content);
            var buttonsPane = new LayoutContainer({
                "class": "geometria_buttonspane",
                region: "bottom"
            });
            leftPane = new LayoutContainer({
                region: "center"
            });
            var helpButton = new Button({
                "class": "geometria_button",
                region: "left",
                label: dict.get("Help"),
                tabIndex: 1002,
                onClick: function() {
                    this.set("disabled", true);
                    deferredHelp.resolve();
                    dialog.hide().then(function() {
                        help.newTopic(helpTopic);
                    });
                }
            });
            leftPane.addChild(helpButton);
            var okButton = new Button({
                "class": "geometria_button",
                region: "right",
                label: dict.get("OK"),
                tabIndex: 1001,
                onClick: function() {
                    this.set("disabled", true);
                    deferredOk.resolve();
                    dialog.hide();
                }
            });
            leftPane.addChild(okButton);
            buttonsPane.addChild(leftPane);
            var cancelButton = new Button({
                "class": "geometria_button",
                region: "right",
                label: dict.get("Cancel"),
                tabIndex: 1003,
                onClick: function() {
                    this.set("disabled", true);
                    deferredCancel.resolve();
                    dialog.hide();
                }
            });
            buttonsPane.addChild(cancelButton);
            container.addChild(buttonsPane);
            if (inputEnters) {
                $.each(inputEnters, function() {
                    this.then(function() {
                        deferredOk.resolve();
                        dialog.hide();
                    });
                });
            }
            dialog = new Dialog({
                title: title || "Geometria",
                content: container,
                onHide: function() {
                    this.destroyRecursive();
                }
            });
            dialog.show();
            return { ok: deferredOk.promise, cancel: deferredCancel.promise,
                help: deferredHelp, okButton: okButton };
        },

        yesNoDialog: function(message, title) {
            var dialog;
            var deferredYes = new Deferred();
            var deferredNo = new Deferred();
            var container = new LayoutContainer({
                "class": "geometria_okdialog"
            });
            var iconPane = new ContentPane({
                "class": "geometriaIconQuestion",
                region: "left"
            });
            container.addChild(iconPane);
            var messagePane = new ContentPane({
                region: "center",
                content: message
            });
            container.addChild(messagePane);
            buttonsPane = new LayoutContainer({
                "class": "geometria_buttonspane",
                region: "bottom"
            });
            leftPane = new LayoutContainer({
                region: "center"
            });
            var yesButton = new Button({
                "class": "geometria_button",
                region: "right",
                label: dict.get("Yes"),
                tabIndex: 1001,
                onClick: function() {
                    this.set("disabled", true);
                    dialog.hide().then(function() {
                        deferredYes.resolve();
                    });
                }
            });
            leftPane.addChild(yesButton);
            buttonsPane.addChild(leftPane);
            var noButton = new Button({
                "class": "geometria_button",
                region: "right",
                label: dict.get("No"),
                tabIndex: 1002,
                onClick: function() {
                    this.set("disabled", true);
                    dialog.hide().then(function() {
                        deferredNo.resolve();
                    });
                }
            });
            buttonsPane.addChild(noButton);
            container.addChild(buttonsPane);
            dialog = new Dialog({
                title: title || "Geometria",
                content: container,
                onHide: function() {
                    this.destroyRecursive();
                }
            });
            dialog.show();
            return { yes: deferredYes.promise, no: deferredNo.promise };
        },
        
        yesNoCancelDialog: function(message, title) {
            var dialog;
            var deferredYes = new Deferred();
            var deferredNo = new Deferred();
            var deferredCancel = new Deferred();
            var container = new LayoutContainer({
                "class": "geometria_okdialog"
            });
            var iconPane = new ContentPane({
                "class": "geometriaIconQuestion",
                region: "left"
            });
            container.addChild(iconPane);
            var messagePane = new ContentPane({
                region: "center",
                content: message
            });
            container.addChild(messagePane);
            var buttonsPane = new LayoutContainer({
                "class": "geometria_buttonspane",
                region: "bottom"
            });
            leftPane = new LayoutContainer({
                region: "center"
            });
            var cancelButton = new Button({
                "class": "geometria_button",
                region: "left",
                label: dict.get("Cancel"),
                tabIndex: 1003,
                onClick: function() {
                    this.set("disabled", true);
                    dialog.hide().then(function() {
                        deferredCancel.resolve();
                    });
                }
            });
            leftPane.addChild(cancelButton);
            var yesButton = new Button({
                "class": "geometria_button",
                region: "right",
                label: dict.get("Yes"),
                tabIndex: 1001,
                onClick: function() {
                    this.set("disabled", true);
                    dialog.hide().then(function() {
                        deferredYes.resolve();
                    });
                }
            });
            leftPane.addChild(yesButton);
            buttonsPane.addChild(leftPane);
            var noButton = new Button({
                "class": "geometria_button",
                region: "right",
                label: dict.get("No"),
                tabIndex: 1002,
                onClick: function() {
                    this.set("disabled", true);
                    dialog.hide().then(function() {
                        deferredNo.resolve();
                    });
                }
            });
            buttonsPane.addChild(noButton);
            container.addChild(buttonsPane);
            dialog = new Dialog({
                title: title || "Geometria",
                content: container,
                onHide: function() {
                    this.destroyRecursive();
                }
            });
            dialog.show();
            return { yes: deferredYes.promise, no: deferredNo.promise,
                cancel: deferredCancel.promise };
        },
        
        textBox: function() {
            var args = arguments[0] || {};
            args["class"] = "geometria_textbox" + (args["class"] && (" " + args["class"]) || "");
            var widget = new TextBox(args);
            if (args.tooltip) {
                new Tooltip({
                    connectId: widget.domNode,
                    label: args.tooltip
                });
            }
            return widget;
        },

        validationTextBox: function() {
            var args = arguments[0] || {};
            args["class"] = "geometria_textbox" + (args["class"] && (" " + args["class"]) || "");
            var widget = new ValidationTextBox(args);
            if (args.tooltip) {
                new Tooltip({
                    connectId: widget.domNode,
                    label: args.tooltip
                });
            }
            return widget;
        },
        
        textArea: function() {
            var args = arguments[0] || {};
            args["class"] = "geometria_textarea" + (args["class"] && (" " + args["class"]) || "");
            var widget = new SimpleTextarea(args);
            new Tooltip({
                connectId: widget.domNode,
                label: args.tooltip
            });
            return widget;
        },
        
        select: function() {
            var args = arguments[0] || {};
            var widget = new Select(args);
            return widget;
        },
        
        radioButton: function() {
            var args = arguments[0] || {};
            var widget = new RadioButton(args);
            return widget;
        }
    };
});
