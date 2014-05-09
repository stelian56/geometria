/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "dojo/data/ObjectStore",
    "dojo/store/Memory",
    "dijit/layout/ContentPane",
    "dojox/grid/DataGrid",
    "geometria/GActions",
    "geometria/GMainContainer",
    "geometria/GUtils"
], function(lang, ObjectStore, Memory, ContentPane, DataGrid, actions, mainContainer, utils) {

    var grid;

    return {
    
        startUp: function() {
            var args = arguments[0];
            lang.mixin(args, {
                "class": "geometria_notepadgrid"
            });
            var pane = new ContentPane(args);
            var store = new ObjectStore({ objectStore: new Memory({ data: { items: [] } }) });
            var layout = [{ field: "col1", width: "100%" }];
            grid = new DataGrid({
                store: store,
                structure: layout,
                escapeHTMLInData: false
            });
            pane.addChild(grid);
            grid.startup();
            return pane;
        },
        
        clear: function() {
            var store = new ObjectStore({ objectStore: new Memory({ data: { items: [] } }) });
            grid.setStore(store);
        },
        
        update: function() {
            grid.store.save();
            grid.update();
        },
        
        documentChanged: function() {
            this.clear();
            $.each(mainContainer.currentDocument.notepad, function() {
                var item = { id: grid.get("rowCount"), col1: this };
                grid.store.newItem(item);
            });
        },

        add: function(record) {
            mainContainer.currentDocument.notepad.push(record);
            var item = { id: grid.get("rowCount"), col1: record };
            grid.store.newItem(item);
            var a = 1;
        },

        removeLastRecord: function() {
            mainContainer.currentDocument.notepad.pop();
            var index = grid.get("rowCount") - 1;
            var item = grid.getItem(index);
            grid.store.deleteItem(item);
        },

        getSelectedIndex: function() {
            return grid.selection.selectedIndex;
        },

        getRecord: function(variableName) {
            var record;
            $.each(mainContainer.currentDocument.notepad, function() {
                if (this.variable.name == variableName) {
                    record = this;
                    return false;
                }
            });
            return record;
        },
        
        getScope: function() {
            var scope = {};
            $.each(mainContainer.currentDocument.notepad, function() {
                scope[this.variable.name] = this.variable.value;
            });
            return scope;
        },
        
        figureRenamed: function(oldName, newName) {
            $.each(mainContainer.currentDocument.notepad, function() {
                this.figureRenamed(oldName, newName);
            });
        },

        pointRenamed: function(oldLabel, newLabel, figureName) {
            $.each(mainContainer.currentDocument.notepad, function() {
                this.pointRenamed(oldLabel, newLabel, figureName);
            });
        },
        
        variableRenamed: function(oldName, newName) {
            var definitionRecord = this.getRecord(oldName);
            $.each(mainContainer.currentDocument.notepad, function() {
                if (!(this === definitionRecord)) {
                    this.variableRenamed(oldName, newName);
                }
            });
            definitionRecord.variableRenamed(oldName, newName);
        },
        
        figureRemoved: function(figureName, executeId) {
            $.each(mainContainer.currentDocument.notepad, function() {
                this.figureRemoved(figureName, executeId);
            });
        },
        
        removeFigureUndone: function(executeId) {
            $.each(mainContainer.currentDocument.notepad, function() {
                this.removeFigureUndone(executeId);
            });
        },
        
        pointRemoved: function(label, figureName, executeId) {
            $.each(mainContainer.currentDocument.notepad, function() {
                this.pointRemoved(label, figureName, executeId);
            });
        },
        
        removePointUndone: function(executeId) {
            $.each(mainContainer.currentDocument.notepad, function() {
                this.removePointUndone(executeId);
            });
        },
        
        figureTransformed: function(figureName, executeId) {
            $.each(mainContainer.currentDocument.notepad, function() {
                this.figureTransformed(figureName, executeId);
            });
        },
        
        transformFigureUndone: function(executeId) {
            $.each(mainContainer.currentDocument.notepad, function() {
                this.transformFigureUndone(executeId);
            });
        }
    };
});
