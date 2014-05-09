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
    "geometria/GActions",
    "geometria/GProblem"
], function(declare, lang, actions, GProblem) {

    var importFigures = function(problem) {
        var figures = [];
        $.each(problem.figures, function(index, f) {
            var figure = f.clone();
            figures.push(figure);
            figure.selected = index == 0;
        });
        return figures;
    };

    return declare(null, {
    
        constructor: function(navigatorItemId, problem) {
            this.navigatorItemId = navigatorItemId;
            this.problem = problem;
            if (problem) {
                this.figures = importFigures(problem);
            }
            this.notepad = [];
            this.log = [];
            this.properties = "";
            this.modified = false;
        },

        make: function(props) {
            this.problem = new GProblem();
            this.problem.make(props.problem);
            this.figures = importFigures(this.problem);
            this.problem.makeAnswer(props.problem);
            this.properties = props.properties;
        },

        toJson: function() {
            var logProps = [];
            $.each(this.log, function() {
                var actionJson = this.action.base.toJson(this.props, this.comments);
                logProps.push(actionJson);
            });
            var solutionProps = {
                "log": logProps,
                "properties": this.properties
            };
            lang.mixin(solutionProps, this.problem.toJson());
            var json = {
                "solution": solutionProps
            };
            return json;
        }
    });
});
