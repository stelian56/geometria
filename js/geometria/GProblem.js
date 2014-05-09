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
    "geometria/GFigure",
    "geometria/GNotepadRecord",
    "geometria/GProblemAnswer"
], function(declare, lang, GFigure, GNotepadRecord, problemAnswer) {
    return declare(null, {

        constructor: function(navigatorItemId) {
            this.navigatorItemId = navigatorItemId;
            this.text = "";
            this.figures = [];
            this.notepad = [];
            this.answer = {
                type: "Number",
                value: 0
            };
            this.properties = "";
            this.modified = false;
        },
        
        make: function(props) {
            var problem = this;
            this.text = props.text
            $.each(props.figures, function(index, figureProps) {
                var figure = new GFigure();
                figure.make(figureProps);
                figure.selected = index == 0;
                problem.figures.push(figure);
            });
            $.each(props.notepad, function() {
                new GNotepadRecord().make(this);
            });
            if (!props.answer) {
                throw "Problem has no answer";
            }
            this.properties = props.properties;
        },
        
        makeAnswer: function(props) {
            this.answer = problemAnswer.make(props.answer, this);
        },
		
        makeAll: function(props) {
            this.make(props);
            this.makeAnswer(props);
        },
        
        containsFigure: function(figureName) {
            var contains = false;
            $.each(this.figures, function() {
                if (this.name == figureName) {
                    contains = true;
                    return false;
                }
            });
            return contains;
        },
        
        toJson: function() {
            var figureProps = [];
            $.each(this.figures, function() {
                figureProps.push(this.toJson());
            });
            var notepadProps = [];
            $.each(this.notepad, function() {
                notepadProps.push(this.toJson());
            });
            var answerProps = problemAnswer.toJson(this.answer);
            var json = {
                "problem": {
                    "text": this.text,
                    "figures": figureProps,
                    "notepad": notepadProps,
                    "answer": answerProps,
                    "properties": this.properties
                }
            };
            return json;
        }
    });
});
