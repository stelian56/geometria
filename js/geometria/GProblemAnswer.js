/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/Deferred",
    "dojo/_base/lang",
    "dojo/data/ItemFileWriteStore",
    "dijit/Dialog",
    "dijit/form/Button",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "dijit/layout/TabContainer",
    "dojox/grid/DataGrid",
    "geometria/GAnswerUtils",
    "geometria/GDictionary",
    "geometria/GFace",
    "geometria/GFiguresContainer",
    "geometria/GMainContainer",
    "geometria/GMath",
    "geometria/GNotepadContainer",
    "geometria/GPlaneConditions",
    "geometria/GPoint",
    "geometria/GSegmentSetConditions",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(Deferred, lang, ItemFileWriteStore, Dialog, Button, ContentPane,
            LayoutContainer, TabContainer, DataGrid,
            answerUtils, dict, GFace, figuresContainer, mainContainer, math, notepadContainer,
            planeConditions, GPoint, segmentSetConditions, utils, widgets) {

    var helpTopic = "EditorsAnswer";
    
    var validateNumber = function(props) {
        if (!("value" in props)) {
            return null;
        }
        var value = utils.eval(props.value);
        if (isNaN(value)) {
            return null;
        }
        var outProps = lang.mixin({}, props);
        outProps.value = value;
        return outProps;
    };
    
    var validateMultipleChoice = function(props) {
        if (!Array.isArray(props.options) || props.options.length < 1) {
            return null;
        }
        var selectedOption, valid = true;
        $.each(props.options, function() {
            if (!this.text) {
                valid = false;
                return false;
            }
            if (this.selected) {
                if (selectedOption) {
                    valid = false;
                    return false;
                }
                selectedOption = this;
            }
        });
        if (valid && selectedOption) {
            return props;
        }
        return null;
    };

    validateSegmentSet = function(props, external) {
        var outProps = null;
        if (props.figureName) {
            $.each(segmentSetConditions, function() {
                if (props.value == this.id) {
                    outProps = props;
                    return false;
                }
            });
        }
        return outProps;
    };
    
    var validateConditionPlane = function(props, external) {
        var outProps = lang.mixin({}, props);
        if (props.condition1) {
            outProps.condition1 = { type: props.condition1.type };
            var condition = planeConditions[props.condition1.type];
            if (condition.validate) {
                var conditionProps1 = {
                    figureName: props.figureName,
                    value: props.condition1.value
                };
                var outProps1 = condition.validate(conditionProps1, external);
                if (outProps1) {
                    outProps.condition1.value = outProps1.value;
                }
            }
        }
        if (props.condition2) {
            outProps.condition2 = { type: props.condition2.type };
            var condition = planeConditions[props.condition2.type];
            var conditionProps2 = {
                figureName: props.figureName,
                value: props.condition2.value
            };
            var outProps2 = condition.validate(conditionProps2, external);
            outProps.condition2.value = outProps2.value;
        }
        if (props.condition3) {
            outProps.condition3 = { type: props.condition3.type };
        }
        return outProps;
    };

    var validateExternal = function(props) {
        if (!props.type) {
            return null;
        }
        switch (props.type) {
        case "Number":
            if (props.locked) {
                try {
                    var decodedValue = window.atob(props.value);
                }
                catch (err) {
                    return null;
                }
                var outProps = lang.mixin({}, props);
                outProps.value = decodedValue;
                return validateNumber(outProps);
            }
            return validateNumber(props);
        case "MultipleChoice":
            return validateMultipleChoice(props);
        case "PointSet":
            return answerUtils.validatePointSet(props, true);
        case "SegmentSet":
            return validateSegmentSet(props, true);
        case "FixedPlane":
            return answerUtils.validatePlane(props, true);
        case "ConditionPlane":
            return validateConditionPlane(props, true);
        default:
            return null;
        }
    };
    
    var numberPane = function(newAnswer, dialog) {
        newAnswer.type = "Number";
        var doc = mainContainer.currentDocument;
        var valueInput = widgets.validationTextBox({
            placeHolder: dict.get("EnterValidExpression"),
            invalidMessage: dict.get("EnterValidExpression"),
            value: doc.answer.type == newAnswer.type ? doc.answer.value : 0
        });
        var pane = new ContentPane({
            content: valueInput
        });
        var validatorDeferred = new Deferred();
		var validator = function() {
			var stringValue = valueInput.get("value").trim();
			var scope = notepadContainer.getScope();
			var value = utils.eval(stringValue, scope);
			var valid = !isNaN(value);
			if (valid) {
				newAnswer.value = value;
			}
			dialog.okButton.set("disabled", !valid);
			return valid || stringValue.length < 1;
		};
        validatorDeferred.promise.then(function() {
            valueInput.set("validator", validator);
        });
        return { pane: pane, validator: validator, validatorDeferred: validatorDeferred };
    };

    var multipleChoicePane = function(newAnswer, dialog) {
        newAnswer.type = "MultipleChoice";
        var doc = mainContainer.currentDocument;
        if (doc.answer.type == newAnswer.type) {
            newAnswer.options = doc.answer.options.slice();
        }
        else {
            newAnswer.options = [{
                text: dict.get("DoubleClickToEditOption"),
                selected: true
            }];
        }
        var grid, id = 0;
        var addButton, deleteButton, selectButton;
        
        var updateButtons = function() {
            var disabled = grid.selection.selectedIndex < 0 || newAnswer.options.length < 2;
            deleteButton.set("disabled", disabled);
            selectButton.set("disabled", disabled);
        };

        var addOption = function() {
            var text = dict.get("DoubleClickToEditOption");
            var item = { id: ++id, col1: text };
            grid.store.newItem(item);
            var option = { text: text, selected: false };
            newAnswer.options.push(option);
            grid.selection.clear();
            updateButtons();
            dialog.okButton.set("disabled", false);
        };

        var deleteOption = function() {
            var optionIndex = grid.selection.selectedIndex;
            var selected = newAnswer.options[optionIndex].selected;
            newAnswer.options.splice(optionIndex, 1);
            grid.removeSelectedRows();
            if (selected) {
                newAnswer.options[0].selected = true;
                grid.update();
            }
            grid.selection.clear();
            updateButtons();
            dialog.okButton.set("disabled", false);
        };

        var selectOption = function() {
            $.each(newAnswer.options, function(index) {
                var option = newAnswer.options[index];
                if (index == grid.selection.selectedIndex) {
                    option.selected = true;
                }
                else {
                    delete option.selected;
                }
            });
            grid.update();
            grid.selection.clear();
            dialog.okButton.set("disabled", false);
        };
        
        var styleRow = function(row) {
            var option = newAnswer.options[row.index];
            if (option) {
                row.customStyles = option.selected ? "font-weight: bold;" : "font-weight: normal;";
            }
        };

        var onGridEdit = function(rowIndex) {
            var item = grid.getItem(rowIndex);
            newAnswer.options[rowIndex].text = item.col1[0];
            dialog.okButton.set("disabled", false);
        };
        
        var items = [];
        $.each(newAnswer.options, function() {
            items.push({ id: ++id, col1: this.text });
        });
        var store = new ItemFileWriteStore({ data: { identifier: "id", items: items } });
        var layout = [
            { field: "col1", width: "100%", editable: true }
        ];
        grid = new DataGrid({
            "class": "geometria_multiplechoicegrid",
            store: store,
            structure: layout,
            selectionMode: "single",
            escapeHTMLInData: false,
            onStyleRow: styleRow,
            onApplyEdit: onGridEdit
        });
        grid.selection.onChanged = function() {
            updateButtons();
        };
        var container = new LayoutContainer({});
        var gridPane = new ContentPane({
            region: "center",
            content: grid
        });
        grid.startup();
        container.addChild(gridPane);
        addButton = new Button({
            label: dict.get("Add"),
            onClick: addOption
        });
        deleteButton = new Button({
            label: dict.get("Delete"),
            onClick: deleteOption
        });
        selectButton = new Button({
            label: dict.get("Select"),
            onClick: selectOption
        });
        updateButtons();
        var buttonPane = new LayoutContainer({
            region: "bottom",
            style: "height:20%"
        });
        buttonPane.addChild(new ContentPane({
            region: "left",
            content: addButton
        }));
        buttonPane.addChild(new ContentPane({
            region: "center",
            content: deleteButton
        }));
        buttonPane.addChild(new ContentPane({
            region: "right",
            content: selectButton
        }));
        container.addChild(buttonPane);
        return { pane: container };
    };

    var pointSetPane = function(newAnswer, dialog) {
        newAnswer.type = "PointSet";
        var doc = mainContainer.currentDocument;
        var value;
        if (doc.answer.type == newAnswer.type) {
            value = doc.answer.value;
        }
        var valueInput = widgets.validationTextBox({
            placeHolder: dict.get("EnterCommaSeparatedPoints"),
            value: value,
            invalidMessage: dict.get("EnterCommaSeparatedPoints")
        });
        var pane = new ContentPane({
            content: valueInput
        });
        if (!value) {
            value = answerUtils.pointSetFromSelection();
        }
        if (value) {
            valueInput.set("value", value);
        }
        var validator = function() {
			var props = { value: valueInput.get("value").trim() };
			var outProps = answerUtils.validatePointSet(props);
			var valid = outProps && true;
			if (valid) {
				newAnswer.value = outProps.value;
			}
			dialog.okButton.set("disabled", !valid);
			return valid || props.value.length < 1;
		};
		var validatorDeferred = new Deferred();
        validatorDeferred.promise.then(function() {
            valueInput.set("validator", validator);
        });
        return { pane: pane, validator: validator, validatorDeferred: validatorDeferred };
    };

    var segmentSetPane = function(newAnswer, dialog) {
        newAnswer.type = "SegmentSet";
        var doc = mainContainer.currentDocument;
        var figureName, value;
        if (doc.answer.type == newAnswer.type) {
            value = doc.answer.value;
            figureName = doc.answer.figureName;
        }
        else {
            figureName = figuresContainer.getSelectedFigure().name;
        }
        var container = new LayoutContainer({
            style: "width:100%;height:100%"
        });
        var figurePane = new LayoutContainer({
            region: "top",
            style: "height:50%;"
        });
        figurePane.addChild(new ContentPane({
            region: "left",
            style: "width:20%;",
            content: dict.get("TargetFigure")
        }));
        var figureOptions = [];
        $.each(doc.figures, function() {
            figureOptions.push({ label: this.name, value: this.name });
        });
        figureInput = widgets.select({
            options: figureOptions,
            onChange: function(newValue) {
                newAnswer.figureName = newValue;
            }
        });
        figureInput.setValue(figureName);
        figurePane.addChild(new ContentPane({
            region: "center",
            content: figureInput
        }));
        container.addChild(figurePane);

        var valueContainer = new LayoutContainer({
            region: "center"
        });
        valueContainer.addChild(new ContentPane({
            region: "left",
            style: "width:30%;",
            content: dict.get("SegmentsForm")
        }));
        
        newAnswer.type = "SegmentSet";
        var options = [];
        $.each(segmentSetConditions, function() {
            options.push({ label: dict.get(this.id), value: this.id });
        });
        valueInput = widgets.select({
            options: options,
            onChange: function(newValue) {
                newAnswer.value = newValue;
            }
        });
        var valuePane = new ContentPane({
            region: "center",
            content: valueInput
        });
        valueContainer.addChild(valuePane);
        container.addChild(valueContainer);
        if (value) {
            valueInput.set("value", value);
        }
        newAnswer.value = valueInput.get("value");
        newAnswer.figureName = figureInput.get("value");
        
        var validator = function() {
            dialog.okButton.set("disabled", false);
        };
        
        return { pane: container, validator: validator };
    };

    var fixedPlanePane = function(newAnswer, dialog) {
        newAnswer.type = "FixedPlane";
        var doc = mainContainer.currentDocument;
        var value;
        if (doc.answer.type == newAnswer.type) {
            value = doc.answer.value;
        }
        var valueInput = widgets.validationTextBox({
            placeHolder: dict.get("Enter3Points"),
            value: value,
            invalidMessage: dict.get("Enter3NonCollinearCommaSeparatedPoints")
        });
        var pane = new ContentPane({
            content: valueInput
        });
        if (!value) {
            value = answerUtils.planeFromSelection();
        }
        if (value) {
            valueInput.set("value", value);
        }
        var validatorDeferred = new Deferred();
		var validator = function() {
            var props = { value: valueInput.get("value").trim() };
			var outProps = answerUtils.validatePlane(props);
			var valid = outProps && true;
			if (valid) {
				newAnswer.value = outProps.value;
			}
			dialog.okButton.set("disabled", !valid);
			return valid || props.value.length < 1;
		};
        validatorDeferred.promise.then(function() {
            valueInput.set("validator", validator);
        });
        valueInput.focus();
        return { pane: pane, validator: validator, validatorDeferred: validatorDeferred };
    };

    var conditionPlanePane = function(newAnswer, dialog) {
        newAnswer.type = "ConditionPlane";
        var doc = mainContainer.currentDocument;
        var container = new LayoutContainer({
            style: "width:100%;height:100%"
        });
        var header = "SelectCondition";
        var figureInput;
        var typeInputs = [];
        var valueInputs = [];
        $.each(["Position", "Volume", "Shape"], function(index) {
            var group = this;
            var options = [{ label: dict.get(header), value: header} ];
            $.each(planeConditions, function() {
                if (this.group == group) {
                    options.push({ label: dict.get(this.id), value: this.id });
                }
            });
            typeInput = widgets.select({
                options: options
            });
            typeInputs.push(typeInput);
            var valueInput = widgets.validationTextBox();
            valueInputs.push(valueInput);
        });

        var getInputProps = function() {
            var props = { figureName: figureInput.get("value") };
            var type1 = typeInputs[0].get("value");
            if (type1 != header) {
                props.condition1 = {
                    type: type1,
                    value: valueInputs[0].get("value").trim().toUpperCase()
                };
            }
            var type2 = typeInputs[1].get("value");
            if (type2 != header) {
                props.condition2 = {
                    type: type2,
                    value: [valueInputs[1].get("value").trim(),
                            valueInputs[2].get("value").trim()]
                };
            }
            var type3 = typeInputs[2].get("value");
            if (type3 != header) {
                props.condition3 = {
                    type: type3
                };
            }
            return props;
        };

        var figurePane = new LayoutContainer({
            region: "top",
            style: "height:25%;"
        });
        figurePane.addChild(new ContentPane({
            region: "left",
            style: "width:25%;",
            content: dict.get("TargetFigure")
        }));
        var figureOptions = [];
        $.each(doc.figures, function() {
            figureOptions.push({ label: this.name, value: this.name });
        });
        figureInput = widgets.select({
            options: figureOptions,
            onChange: function(newValue) {
                var props = getInputProps();
                var outProps = validateConditionPlane(props);
                var valid = (!outProps.condition1 ||
                    !planeConditions[outProps.condition1.type].validate ||
                    outProps.condition1.value) && (!outProps.condition2 ||
                    outProps.condition2.value[0] && outProps.condition2.value[1]);
                if (valid) {
                    newAnswer.figureName = outProps.figureName;
                    newAnswer.condition1 = outProps.condition1;
                    newAnswer.condition2 = outProps.condition2;
                    newAnswer.condition3 = outProps.condition3;
                }
                dialog.okButton.set("disabled", !valid);
            }
        });
        var figureName;
        if (doc.answer.type == newAnswer.type) {
            figureName = doc.answer.figureName;
        }
        else {
            figureName = figuresContainer.getSelectedFigure().name;
        }
        figureInput.setValue(figureName);
        figurePane.addChild(new ContentPane({
            region: "center",
            content: figureInput
        }));
        container.addChild(figurePane);
        
        var conditionsPane = new LayoutContainer({
            region: "center"
        });
        
        typeInputs[0].onChange = function(newValue) {
            var condition = planeConditions[newValue];
            var placeHolder = condition ? condition.placeHolder : null;
            var invalidMessage = condition ? condition.invalidMessage || placeHolder : null;
            if (placeHolder) {
                valueInputs[0].set("disabled", false);
                valueInputs[0].set("placeHolder", placeHolder);
                valueInputs[0].set("invalidMessage", invalidMessage);
                var value = valueInputs[0].get("value");
                if (!value) {
                    value =
                        condition.stringValueFromSelection && condition.stringValueFromSelection();
                }
                if (value) {
                    valueInputs[0].set("value", value);
                }
            }
            else {
                valueInputs[0].set("value", null);
                valueInputs[0].set("disabled", true);
                valueInputs[0].set("placeHolder", null);
            }
            var props = getInputProps();
            var outProps = validateConditionPlane(props);
            var valid = (!outProps.condition1 || !condition.validate ||
                outProps.condition1.value) && (!outProps.condition2 ||
                outProps.condition2.value[0] && outProps.condition2.value[1]);
            if (valid) {
                newAnswer.figureName = outProps.figureName;
                newAnswer.condition1 = outProps.condition1;
                newAnswer.condition2 = outProps.condition2;
                newAnswer.condition3 = outProps.condition3;
            }
            dialog.okButton.set("disabled", !valid);
        };
        if (doc.answer.type == newAnswer.type && doc.answer.condition1) {
            typeInputs[0].set("value", doc.answer.condition1.type);
            valueInputs[0].set("value", doc.answer.condition1.value);
        }
        var conditionPane1 = new LayoutContainer({
            region: "top",
            style: "height:33%"
        });
        conditionPane1.addChild(new ContentPane({
            region: "left",
            style: "width:25%",
            content: dict.get("Plane")
        }));
        conditionPane1.addChild(new ContentPane({
            region: "center",
            content: typeInputs[0]
        }));
        conditionPane1.addChild(new ContentPane({
            region: "right",
            style: "width:25%",
            content: valueInputs[0]
        }));
        conditionsPane.addChild(conditionPane1);

        typeInputs[1].onChange = function(newValue) {
            var condition = planeConditions[newValue];
            var invalidMessage = condition ? condition.invalidMessage : null;
            $.each([0, 1], function(index) {
                var valueInput = valueInputs[index + 1];
                valueInput.set("disabled", !condition);
                valueInput.set("invalidMessage", invalidMessage);
            });
            if (!condition) {
                valueInputs[1].set("value", null);
                valueInputs[2].set("value", null);
            }
            var props = getInputProps();
            var outProps = validateConditionPlane(props);
            var valid = (!outProps.condition1 ||
                !planeConditions[outProps.condition1.type].validate ||
                outProps.condition1.value) && (!outProps.condition2 ||
                outProps.condition2.value[0] && outProps.condition2.value[1]);
            if (valid) {
                newAnswer.figureName = outProps.figureName;
                newAnswer.condition1 = outProps.condition1;
                newAnswer.condition2 = outProps.condition2;
                newAnswer.condition3 = outProps.condition3;
            }
            dialog.okButton.set("disabled", !valid);
        };
        if (doc.answer.type == newAnswer.type && doc.answer.condition2) {
            typeInputs[1].set("value", doc.answer.condition2.type);
            valueInputs[1].set("value", doc.answer.condition2.value[0]);
            valueInputs[2].set("value", doc.answer.condition2.value[1]);
        }
        var conditionPane2 = new LayoutContainer({
            region: "center"
        });
        conditionPane2.addChild(new ContentPane({
            region: "left",
            style: "width:25%",
            content: dict.get("Plane")
        }));
        conditionPane2.addChild(new ContentPane({
            region: "center",
            content: typeInputs[1]
        }));
        var ratioContainer = new LayoutContainer({
            region: "right",
            style: "width:25%;margin-right:10px;"
        });
        ratioContainer.addChild(new ContentPane({
            "class": "geometria_ratiodivider",
            region: "center",
            content: ":"
        }));
        $.each(["left", "right"], function(index, region) {
            var valueInput = valueInputs[index + 1];
            ratioContainer.addChild(new ContentPane({
                "class": "geometria_pointinput geometria_inputpane",
                region: region,
                content: valueInput
            }));
        });
        conditionPane2.addChild(ratioContainer);
        conditionsPane.addChild(conditionPane2);

        typeInputs[2].onChange = function(newValue) {
            var props = getInputProps();
            var outProps = validateConditionPlane(props);
            var valid = (!outProps.condition1 ||
                !planeConditions[outProps.condition1.type].validate ||
                outProps.condition1.value) && (!outProps.condition2 ||
                outProps.condition2.value[0] && outProps.condition2.value[1]);
            if (valid) {
                newAnswer.figureName = outProps.figureName;
                newAnswer.condition1 = outProps.condition1;
                newAnswer.condition2 = outProps.condition2;
                newAnswer.condition3 = outProps.condition3;
            }
            dialog.okButton.set("disabled", !valid);
        };
        if (doc.answer.type == newAnswer.type && doc.answer.condition3) {
            typeInputs[2].set("value", doc.answer.condition3.type);
        }
        var conditionPane3 = new LayoutContainer({
            region: "bottom",
            style: "height:33%"
        });
        conditionPane3.addChild(new ContentPane({
            region: "left",
            style: "width:25%",
            content: dict.get("SectionShapedAs")
        }));
        conditionPane3.addChild(new ContentPane({
            region: "center",
            content: typeInputs[2]
        }));
        conditionPane3.addChild(new ContentPane({
            region: "right",
            style: "width:25%"
        }));
        conditionsPane.addChild(conditionPane3);
        container.addChild(conditionsPane);
        
        var validatorDeferred = new Deferred();
		
		var validator = function() {
			var props = getInputProps();
			var outProps = validateConditionPlane(props);
			var valid =
				[!outProps.condition1 ||
				!planeConditions[outProps.condition1.type].validate ||
				outProps.condition1.value,
				!outProps.condition2 || outProps.condition2.value[0],
				!outProps.condition2 || outProps.condition2.value[1]];
			var allValid = valid[0] && valid[1] && valid[2];
			if (allValid) {
				newAnswer.figureName = outProps.figureName;
				newAnswer.condition1 = outProps.condition1;
				newAnswer.condition2 = outProps.condition2;
				newAnswer.condition3 = outProps.condition3;
			}
			dialog.okButton.set("disabled", !allValid);
			return valid;
		};
		
        validatorDeferred.promise.then(function() {
            $.each([0, 1, 2], function(index) {
                valueInputs[index].set("validator", function() {
					var valid = validator();
					return valid[index] || valueInputs[index].get("value").trim().length < 1;
				});
            });
        });
        return { pane: container, validator: validator, validatorDeferred: validatorDeferred };
    };

    return {

        execute: function() {
            var deferred = new Deferred();
            var doc = mainContainer.currentDocument;
            var container = new LayoutContainer({
                "class": "geometria_problemanswer"
            });
            var tabPane = new TabContainer({
                region: "center"
            });
            var dialog, valuePane, newAnswer, validator;
            var typeInput = widgets.select({
                options: [
                    { label: dict.get("Number"), value: "Number" },
                    { label: dict.get("MultipleChoice"), value: "MultipleChoice" },
                    { label: dict.get("PointSet"), value: "PointSet" },
                    { label: dict.get("SegmentSet"), value: "SegmentSet",
                        disabled: doc.figures.length < 1 },
                    { label: dict.get("FixedPlane"), value: "FixedPlane" },
                    { label: dict.get("ConditionPlane"), value: "ConditionPlane",
                        disabled: doc.figures.length < 1 }
                ],
                onChange: function() {
                    var result;
                    newAnswer = {};
                    switch (this.get("value")) {
                        case "Number":
                            result = numberPane(newAnswer, dialog);
                            break;
                        case "MultipleChoice":
                            result = multipleChoicePane(newAnswer, dialog);
                            break;
                        case "PointSet":
                            result = pointSetPane(newAnswer, dialog);
                            break;
                        case "SegmentSet":
                            result = segmentSetPane(newAnswer, dialog);
                            break;
                        case "FixedPlane":
                            result = fixedPlanePane(newAnswer, dialog);
                            break;
                        case "ConditionPlane":
                            result = conditionPlanePane(newAnswer, dialog);
                            break;
                    }
                    valuePane.set("content", result.pane);
                    if (result.validatorDeferred) {
                        result.validatorDeferred.resolve();
                    }
                    validator = result.validator;
                }
            });
            var typePane = new ContentPane({
                "class": "geometria_answertype",
                title: dict.get("Type"),
                content: typeInput
            });
            tabPane.addChild(typePane);
            valuePane = new ContentPane({
                "class": "geometria_answervalue",
                title: dict.get("Value")
            });
            tabPane.addChild(valuePane);
            container.addChild(tabPane);
            tabPane.watch("selectedChildWidget", function(name, oldValue, newValue) {
				if (newValue.title == dict.get("Type")) {
					dialog.okButton.set("disabled", true);
				}
				else {
					validator && validator();
				}
			});
            dialog = widgets.okCancelHelpDialog(container, helpTopic,
                "geometria_problemanswer", dict.get("Answer"));
            dialog.okButton.set("disabled", true);
            var oldAnswer = doc.answer;
            typeInput.set("value", oldAnswer.type);
            if (oldAnswer.type == "Number") {
                typeInput.onChange();
            }
            dialog.ok.then(function() {
                doc.answer = newAnswer;
                var outProps = {
                    oldAnswer: oldAnswer,
                    newAnswer: newAnswer
                };
                mainContainer.setDocumentModified(true);
                deferred.resolve(outProps);
            });
            return deferred.promise;
        },

        make: function(props) {
            return validateExternal(props) ? props : null;
        },
        
        undo: function(props) {
            mainContainer.currentDocument.answer = props.oldAnswer;
        },
        
        playBack: function(props) {
            mainContainer.currentDocument.answer = props.newAnswer;
        },
        
        toJson: function(props) {
            switch (props.type) {
            case "Number":
                var outProps = lang.mixin({}, props);
                if (props.locked) {
                    outProps.value = window.btoa(props.value);
                }
                else {
                    outProps.value = props.value.toString();
                }
                return outProps;
            default:
                return props;
            }
        }
    };
});
