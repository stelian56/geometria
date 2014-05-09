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
    "dijit/Dialog",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "geometria/GAnswerUtils",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GMath",
    "geometria/GNotepadContainer",
    "geometria/GPlaneConditions",
    "geometria/GSegmentSetConditions",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(Deferred, lang, Dialog, ContentPane, LayoutContainer,
        answerUtils, dict, figuresContainer, logContainer, mainContainer, math, notepadContainer,
        planeConditions, segmentSetConditions, utils, widgets) {

    var helpTopic = "SolversAnswer";
    
    var validateNumber = function(props) {
        var scope = notepadContainer.getScope();
        var value = utils.eval(props.value, scope);
        return !isNaN(value);
    };

    var validateExternal = function(props) {
        switch (mainContainer.currentDocument.problem.answer.type) {
        case "Number":
            if (!("value" in props)) {
                return false;
            }
            return validateNumber(props);
        case "MultipleChoice":
            return true;
        case "PointSet":
            if (!("value" in props)) {
                return false;
            }
            return answerUtils.validatePointSet(props, true);
        case "SegmentSet":
            if (!("value" in props)) {
                return false;
            }
            return answerUtils.validateSegmentSet(props, true);
        case "FixedPlane":
        case "ConditionPlane":
            if (!("value" in props)) {
                return false;
            }
            return answerUtils.validatePlane(props, true);
        }
        return false;
    };
    
    var apply = function(props, external) {
        var doc = mainContainer.currentDocument;
        var answer = doc.problem.answer;
        var correct = false;
        var outProps = lang.mixin({}, props);
        var logValue = props.value;
        switch (answer.type) {
        case "Number":
            var scope = notepadContainer.getScope();
            var value = utils.eval(props.value, scope);
            correct = Math.abs(value - answer.value) < math.EPSILON;
            break;
        case "MultipleChoice":
            var correctValue;
            $.each(answer.options, function() {
                if (this.selected) {
                    correctValue = this.text;
                    return false;
                }
            });
            correct = props.value == correctValue;
            break;
        case "PointSet":
            var solutionAnswerProps = answerUtils.validatePointSet(props, external);
            if (solutionAnswerProps) {
                var problemAnswerProps = answerUtils.validatePointSet(answer, external);
                if (answerUtils.arePointSetsEqual(solutionAnswerProps.points,
                        problemAnswerProps.points, 1)) {
                    correct = true;
                }
            }
            outProps.figureName = solutionAnswerProps.figureName;
            break;
        case "SegmentSet":
            var solutionAnswerProps = answerUtils.validateSegmentSet(props, external);
            if (solutionAnswerProps) {
                var targetFigure = figuresContainer.getFigure(answer.figureName);
                var segments = solutionAnswerProps.segments;
                var condition = segmentSetConditions[answer.value];
                correct =  condition.verify(segments, targetFigure);
            }
            outProps.figureName = solutionAnswerProps.figureName;
            break;
        case "FixedPlane":
            var solutionAnswerProps = answerUtils.validatePlane(props, external);
            if (solutionAnswerProps) {
                var problemAnswerProps = answerUtils.validatePlane(answer, external);
                if (answerUtils.arePlanesEqual(solutionAnswerProps.points,
                        problemAnswerProps.points, 1)) {
                    correct = true;
                    logValue = logValue.replace(/,/g, "");
                    logValue = "[" + logValue + "]";
                }
            }
            outProps.figureName = solutionAnswerProps.figureName;
            break;
        case "ConditionPlane":
            var solutionAnswerProps = answerUtils.validatePlane(props, external);
            if (solutionAnswerProps) {
                var targetSolid = figuresContainer.getFigure(answer.figureName).solid;
                var ps = solutionAnswerProps.points;
                correct = true;
                $.each([answer.condition1, answer.condition2, answer.condition3],
                        function(index, conditionProps) {
                    if (conditionProps) {
                        var condition = planeConditions[conditionProps.type];
                        var value = conditionProps.value;
                        if (!condition.verify(ps, targetSolid, value)) {
                            correct = false;
                            return false;
                        }
                    }
                });
                if (correct) {
                    logValue = logValue.replace(/,/g, "");
                    logValue = "[" + logValue + "]";
                }
            }
            outProps.figureName = solutionAnswerProps.figureName;
        }
        outProps.logValue = logValue;
        return correct ? outProps : null;
    };

    var numberInput = function(dialog, valueInputDeferred) {
        var valueInput = widgets.validationTextBox({
            placeHolder: dict.get("EnterValidExpression"),
            invalidMessage: dict.get("EnterValidExpression"),
            onKeyUp: function(event) {
                if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                    valueInputDeferred.resolve();
                }
            }
        });
        return valueInput;
    };
    
    var numberValidator = function(dialog) {
        var validator = function() {
            var props = { value: this.get("value").trim() };
            var valid = validateNumber(props);
            dialog.okButton.set("disabled", !valid);
            return valid || props.value.length < 1;
        };
        return validator;
    };
    
    var pointSetInput = function(dialog, valueInputDeferred) {
        var valueInput = widgets.validationTextBox({
            placeHolder: dict.get("EnterCommaSeparatedPoints"),
            invalidMessage: dict.get("EnterCommaSeparatedPoints"),
            onKeyUp: function(event) {
                if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                    valueInputDeferred.resolve();
                }
            }
        });
        var value = answerUtils.pointSetFromSelection();
        if (value) {
            valueInput.set("value", value);
        }
        return valueInput;
    };

    var pointSetValidator = function(dialog) {
        var validator = function() {
            var props = { value: this.get("value").trim() };
            var valid = answerUtils.validatePointSet(props) && true;
            dialog.okButton.set("disabled", !valid);
            return valid || props.value.length < 1;
        };
        return validator;
    };

    var segmentSetInput = function(dialog, valueInputDeferred) {
        var valueInput = widgets.validationTextBox({
            placeHolder: dict.get("EnterCommaSeparatedSegments"),
            invalidMessage: dict.get("EnterCommaSeparatedSegmentsNoBrackets"),
            onKeyUp: function(event) {
                if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                    valueInputDeferred.resolve();
                }
            }
        });
        var value = answerUtils.segmentSetFromSelection();
        if (value) {
            valueInput.set("value", value);
        }
        return valueInput;
    };

    var segmentSetValidator = function(dialog) {
        var validator = function() {
            var props = { value: this.get("value").trim() };
            var valid = answerUtils.validateSegmentSet(props) && true;
            dialog.okButton.set("disabled", !valid);
            return valid || props.value.length < 1;
        };
        return validator;
    };

    var planeInput = function(dialog, valueInputDeferred) {
        var valueInput = widgets.validationTextBox({
            placeHolder: dict.get("Enter3Points"),
            invalidMessage: dict.get("Enter3NonCollinearCommaSeparatedPoints"),
            onKeyUp: function(event) {
                if (event.keyCode == 13 && !dialog.okButton.get("disabled")) {
                    valueInputDeferred.resolve();
                }
            }
        });
        var value = answerUtils.planeFromSelection();
        if (value) {
            valueInput.set("value", value);
        }
        return valueInput;
    };

    var planeValidator = function(dialog) {
        var validator = function() {
            var props = { value: this.get("value").trim() };
            var valid = answerUtils.validatePlane(props) && true;
            dialog.okButton.set("disabled", !valid);
            return valid || props.value.length < 1;
        };
        return validator;
    };

    var multipleChoiceInput = function(dialog) {
        var options = [];
        $.each(mainContainer.currentDocument.problem.answer.options, function() {
            var option = { label: this.text, value: this.text };
            options.push(option);
        });
        var valueInput = widgets.select({
            options: options
        });
        return valueInput;
    };
    
    return {

        execute: function() {
            var deferred = new Deferred();
            var container = new LayoutContainer();
            var valueInputDeferred = new Deferred();
            var dialog = widgets.okCancelHelpDialog(container, helpTopic,
                "geometria_solutionanswer", dict.get("Answer"), [valueInputDeferred.promise]);
            var valueInput;
            var answerType = mainContainer.currentDocument.problem.answer.type;
            switch (answerType) {
            case "Number":
                valueInput = numberInput(dialog, valueInputDeferred);
                break;
            case "MultipleChoice":
                valueInput = multipleChoiceInput(dialog);
                break;
            case "PointSet":
                valueInput = pointSetInput(dialog, valueInputDeferred);
                break;
            case "SegmentSet":
                valueInput = segmentSetInput(dialog, valueInputDeferred);
                break;
            case "FixedPlane":
            case "ConditionPlane":
                valueInput = planeInput(dialog, valueInputDeferred);
            }
            var valuePane = new ContentPane({
                "class": "geometria_answervalue",
                region: "center",
                content: valueInput
            });
            container.addChild(valuePane);
            var validator;
            switch(answerType) {
            case "Number":
                validator = numberValidator(dialog);
                dialog.okButton.set("disabled", true);
                break;
            case "PointSet":
                validator = pointSetValidator(dialog);
                dialog.okButton.set("disabled", true);
                break;
            case "SegmentSet":
                validator = segmentSetValidator(dialog);
                dialog.okButton.set("disabled", true);
                break;
            case "FixedPlane":
            case "ConditionPlane":
                validator = planeValidator(dialog);
                dialog.okButton.set("disabled", true);
            }
            if (valueInput) {
                valueInput.set("validator", validator);
                valueInput.validate();
            }
            dialog.ok.then(function() {
                var inputProps = {
                    value: valueInput.get("value").trim()
                };
                var figure = figuresContainer.getSelectedFigure();
                if (figure) {
                    inputProps.figureName = figure.name;
                }
                outProps = apply(inputProps);
                var title = outProps ? dict.get("CorrectAnswer") : dict.get("IncorrectAnswer");
                var iconClass = outProps ? "geometriaIconCorrect" : "geometriaIconIncorrect";
                var message = outProps ? dict.get("AnswerIsCorrect") : dict.get("AnswerIsIncorrect");
                widgets.okDialog(message, title, iconClass).ok.then(function() {
                    if (outProps) {
                        mainContainer.setDocumentModified(true);
                        deferred.resolve(outProps);
                    }
                });
            });
            return deferred.promise;
        },

        playBack: function(props, external) {
            if (external && !validateExternal(props)) {
                return null;
            }
            return apply(props, external);
        },

        toLog: function(props) {
            if (mainContainer.currentDocument.problem.answer.type == "Number") {
                return dict.get("CorrectAnswerNumber", props.logValue);
            }
            else if (props.figureName) {
                return dict.get("CorrectAnswerInFigure", props.logValue, props.figureName);
            }
            return dict.get("CorrectAnswerOther", props.logValue);
        },

        toJson: function(props) {
            return {
                "action": "answerAction",
                "props": {
                    "value": props.value,
                    "figureName": props.figureName
                }
            };
        }
    };
});
