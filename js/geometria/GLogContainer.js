/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/data/ObjectStore",
    "dojo/store/Memory",
    "dijit/Tooltip",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "dojox/grid/DataGrid",
    "geometria/GActions",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GLogToolBar",
    "geometria/GMainContainer",
    "geometria/GNotepadContainer",
    "geometria/GProblemText",
    "geometria/GSolution",
    "geometria/GWidgets"
], function(ObjectStore, Memory, Tooltip, ContentPane, LayoutContainer, DataGrid,
        actions, dict, figuresContainer, logToolBar, mainContainer, notepadContainer, problemText,
        GSolution, widgets) {

    var grid;
    var changingSelectionIndex;
    var playbackActive;
    
    var commentsHelpTopic = "";
    
    var watchSelection = function() {
        grid.selection.onChanged = function() {
            selectionChanged(changingSelectionIndex, this.selectedIndex);
        };
        grid.selection.onChanging = function() {
            changingSelectionIndex = this.selectedIndex;
        };
    };
    
    var unwatchSelection = function() {
        grid.selection.onChanged = function() {}
        grid.selection.onChanging = function() {}
    };
    
    var selectionChanged = function(fromIndex, toIndex) {
        if (playbackActive) {
            var doc = mainContainer.masterSolution;
            var log = doc.log;
            var recordIndex;
            if (fromIndex < 0 || fromIndex > toIndex) {
                var solution = new GSolution(doc.navigatorItemId, doc.problem);
                mainContainer.setCurrentDocument(solution);
                figuresContainer.documentChanged();
                notepadContainer.clear();
                for (recordIndex = 0; recordIndex <= toIndex; recordIndex++) {
                    var record = log[recordIndex];
                    record.action.base.playBack(record.props);
                }
            }
            else {
                for (recordIndex = fromIndex + 1; recordIndex <= toIndex; recordIndex++) {
                    var record = log[recordIndex];
                    record.action.base.playBack(record.props);
                }
            }
            actions.updateStates();
        }
    };

    var getCommentsPlaceholder = function(record) {
        return record.comments ? "<div class='geometriaNavigatorIcon geometriaIcon24ProblemFile'></div>" : "";
    };
    
    
    var editComments = function(recordIndex) {
        grid.selection.setSelected(recordIndex, false);
        var record = mainContainer.masterSolution.log[recordIndex];
        var container = new LayoutContainer();
        var textArea = widgets.textArea({
            region: "center",
            placeholder: dict.get("TypeComments")
        });
        textArea.set("value", record.comments);
        container.addChild(textArea);
        var dialog = widgets.okCancelHelpDialog(container, commentsHelpTopic, "geometria_logcomments",
            dict.get("Comments"));
        dialog.ok.then(function() {
            comments = textArea.get("value").trim();
            record.comments = comments.length ? comments : null;
            var item = grid.getItem(recordIndex);
            var commentsPlaceholder = getCommentsPlaceholder(record);
            grid.store.setValue(item, "col2", commentsPlaceholder);
            grid.store.save();
            mainContainer.setDocumentModified(true);
        });
    };

    var showComments = function(recordIndex, cellNode) {
        var comments = mainContainer.masterSolution.log[recordIndex].comments;
        if (comments) {
            Tooltip.show(comments, cellNode);
        }
    };
    
    var hideComments = function(cellNode) {
        Tooltip.hide(cellNode);
    };
    
    return {
    
        masterSolution: null,
        
        startUp: function() {
            var logContainer = this;
            var container = new LayoutContainer(arguments[0]);
            var dojoToolBar = logToolBar.startUp({
                "class": "geometria_logtop",
                region: "top"
            });
            container.addChild(dojoToolBar);
            var recordPane = new ContentPane({
                "class": "geometria_logcontent",
                region: "center"
            });
            var store = new ObjectStore({ objectStore: new Memory({ data: { items: [] } }) });
            var layout = [
                { field: "col1", width: "95%" },
                { field: "col2", width: "16px" }
            ];
            grid = new DataGrid({
                "class": "geometria_loggrid",
                store: store,
                structure: layout,
                selectionMode: "single",
                escapeHTMLInData: false,
                onCellDblClick: function(event) {
                    if (!playbackActive && event.cellIndex == 1) {
                        editComments(event.rowIndex);
                    }
                },
                onCellMouseOver: function(event) {
                    showComments(event.rowIndex, event.cellNode);
                },
                onCellMouseOut: function(event) {
                    hideComments(event.cellNode)
                }
            });
            recordPane.set("content", grid);
            grid.startup();
            container.addChild(recordPane);
            return container;
        },
        
        clear: function() {
            unwatchSelection();
            var selectedIndex = grid.selection.selectedIndex;
            grid.selection.setSelected(selectedIndex, false);
            var store = new ObjectStore({ objectStore: new Memory({ data: { items: [] } }) });
            grid.setStore(store);
            watchSelection();
        },
        
        add: function(record) {
            mainContainer.currentDocument.log.push(record);
            var store = grid.store;
            var entry = record.action.toLog(record.props);
            var commentsPlaceholder = getCommentsPlaceholder(record);
            var record = { id: grid.get("rowCount"), col1: entry, col2: commentsPlaceholder };
            store.newItem(record);
        },
        
        removeLastRecord: function() {
            mainContainer.currentDocument.log.pop();
            var index = grid.get("rowCount") - 1;
            var item = grid.getItem(index);
            grid.store.deleteItem(item);
        },

        isPlaybackActive: function() {
            return playbackActive;
        },
        
        getSelectedIndex: function() {
            return grid.selection.selectedIndex;
        },

        playNext: function() {
            unwatchSelection();
            var selectedIndex = grid.selection.selectedIndex;
            if (!playbackActive) {
                grid.selection.setSelected(selectedIndex, false);
                grid.selection.setSelected(-1, true);
                playbackActive = true;
            }
            selectedIndex = grid.selection.selectedIndex;
            grid.selection.setSelected(selectedIndex, false);
            grid.selection.setSelected(selectedIndex + 1, true);
            selectionChanged(selectedIndex, selectedIndex + 1);
            grid.scrollToRow(selectedIndex + 1);
            watchSelection();
        },

        rewind: function() {
            unwatchSelection();
            var selectedIndex = grid.selection.selectedIndex;
            grid.selection.setSelected(selectedIndex, false);
            selectionChanged(selectedIndex, -1);
            watchSelection();
        },
        
        stop: function() {
            playbackActive = false;
            unwatchSelection();
            var selectedIndex = grid.selection.selectedIndex;
            grid.selection.setSelected(selectedIndex, false);
            mainContainer.setCurrentDocument(mainContainer.masterSolution);
            figuresContainer.documentChanged();
            notepadContainer.documentChanged();
            watchSelection();
        }
    };
});
