/**
 * Copyright 2000-2026 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/lang",
    "dojo/aspect",
    "dojo/Deferred",
    "dojo/request",
    "dojo/request/xhr",
    "dojo/store/Memory",
    "dojo/store/Observable",
    "dijit/Tree",
    "dijit/form/ValidationTextBox",
    "dijit/layout/ContentPane",
    "dijit/layout/LayoutContainer",
    "dijit/tree/dndSource",
    "dijit/tree/ObjectStoreModel",
    "geometria/GActions",
    "geometria/GDictionary",
    "geometria/GFiguresContainer",
    "geometria/GLogContainer",
    "geometria/GMainContainer",
    "geometria/GProblem",
    "geometria/GSolid",
    "geometria/GSolution",
    "geometria/GUtils",
    "geometria/GWidgets"
], function(lang, aspect, Deferred, request, xhr, Memory, Observable, Tree, ValidationTextBox,
        ContentPane, LayoutContainer, dndSource, ObjectStoreModel, 
        actions, dict, figuresContainer, logContainer, mainContainer, GProblem,
        GSolid, GSolution, utils, widgets) {

    var itemNameRegExp = "[A-Za-z0-9\\s]+";
    var root;
    var store;
    var model;
    var tree;
    var container;
    var contentPane;
    var itemNameTextBox;
    var itemNamePane;

    var showItemNamePane = function(hint, invalidMessage) {
        var deferred = new Deferred();
        var hide = function() {
            if (container.getIndexOfChild(itemNamePane) > -1) {
                container.removeChild(itemNamePane);
            }
         };
        itemNameTextBox = widgets.validationTextBox({
            placeHolder: hint,
            invalidMessage: invalidMessage,
            regExp: itemNameRegExp,
            onKeyPress: function(event) {
                if (event.keyCode == 13 && this.isValid() && $.trim(this.get("value"))) {
                    hide();
                    deferred.resolve();
                }
            },
            onBlur: function() {
                hide();
            }
        });
        itemNameTextBox.startup();
        itemNamePane.setContent(itemNameTextBox);
        if (container.getIndexOfChild(itemNamePane) < 0) {
            container.addChild(itemNamePane);
        }
        itemNameTextBox.focus();
        return deferred.promise;
    }

    return {

        expandTo: function(item) {
            var path = [ item.id ];
            var parent = this.itemById(item.parent);
            while (parent) {
                path.splice(0, 0, parent.id);
                parent = this.itemById(parent.parent);
            }
            tree.set("path", path);
        },

        getSelectedItem: function() {
            return tree && tree.selectedItem;
        },

        selectItem: function(id) {
            if (id) {
                var item = this.itemById(id);
                if (item) {
                    this.expandTo(item);
                    tree.set("selectedItem", item);
                }
            }
            else {
                tree.set("selectedItem", null);
            }
        },

        isSelectedItemRemovable: function() {
            if (tree) {
                var item = tree.selectedItem;
                return item && item.id != root.id;
            }
            return false;
        },

        populate: function() {
            var deferred = new Deferred();
            var storedata = [];

            var onAllJson = function(allJson) {
                if (allJson) {
                    var makeItem = function(itemJson, parentId) {
                        var id = itemJson.id;
                        if (id) {
                            var type = itemJson.type;
                            if (type) {
                                var name = itemJson.name;
                                if (name) {
                                    var item = { id: id, parent: parentId, type: type, name: name };
                                    storedata.push(item);
                                    if (!parentId) {
                                        root = item;
                                    }
                                    var childrenJson = itemJson.items;
                                    if (Array.isArray(childrenJson)) {
                                        childrenJson.forEach((childJson) => {
                                            makeItem(childJson, id);
                                        });
                                    }
                                }
                            }
                        }
                    }
                    makeItem(allJson);
                }
                
                if (root) {
                    var memoryStore = new Memory({
                        data: storedata,
                        getChildren: function(parent) {
                            return this.query({ parent: parent.id });
                        }
                    });
                    store = new Observable(memoryStore);
                    model = new ObjectStoreModel({
                        store: store,
                        query: {"id": root.id}
                    });
                    tree = new Tree({
                        model: model,
                        dndController: dndSource,
                        getIconClass: function(item, opened) {
                            switch (item.type) {
                            case 'd':
                                return opened ? "dijitFolderOpened" : "dijitFolderClosed";
                            case 'f':
                                return "geometriaNavigatorIcon geometriaIcon24FigureFile";
                            case 'p':
                                return "geometriaNavigatorIcon geometriaIcon24ProblemFile";
                            case 's':
                                return "geometriaNavigatorIcon geometriaIcon24SolutionFile";
                            }
                        },
                        checkItemAcceptance: function(target, source, position) {
                            if (logContainer.isPlaybackActive()) {
                                return false;
                            }
                            var targetItem = dijit.getEnclosingWidget(target).item;
                            var sourceItem = source.anchor.item;
                            if (targetItem.type == 'd') {
                                var duplicate;
                                model.getChildren(targetItem, function(children) {
                                    $.each(children, function() {
                                        if (this.name == sourceItem.name) {
                                            duplicate = true;
                                            return false;
                                        }
                                    });
                                });
                                if (!duplicate) {
                                    return true;
                                }
                            }
                            return position != "over";
                        },
                        onOpen: function(item, node) {
                            $.each(node.getChildren(), function() {
                                if (this.item.type != 'd') {
                                    this.isExpandable = false;
                                    this._setExpando();
                                }
                            });
                        },
                        onClick: function(item, node, event) {
                            this._onExpandoClick({ node: node });
                        },
                        onDblClick: function() {
                            if (tree.selectedItem.type != 'd') {
                                var action = actions["openAction"];
                                if (action.base.enabled) {
                                    action.base.execute();
                                }
                            }
                        }
                    });
                    tree.watch("selectedItem", function() {
                        actions.updateStates();
                    });
                    tree.startup();
                    contentPane.set("content", tree);
                    
                    aspect.around(store, "put", function(originalPut){
                        return function(obj, options) {
                            var result = false;

                            if(options && options.parent){
                                obj.parent = options.parent.id;
                            }
                            if (options.noDnd) {
                                result = true;
                            }
                            else {
                                var allJson = utils.jsonFromStorage();
                                var itemJson = utils.itemJsonById(allJson, obj.id);
                                var oldParentJson = utils.parentJsonByItemId(allJson, obj.id);
                                var newParentJson = utils.itemJsonById(allJson, options.parent.id);
                                if (oldParentJson && newParentJson) {
                                    var oldItems = oldParentJson.items;
                                    var newItems = newParentJson.items;
                                    if (oldItems && newItems) {
                                        for (var itemIndex = 0; itemIndex < oldItems.length; itemIndex++) {
                                            if (oldItems[itemIndex].id == obj.id) {
                                                oldItems.splice(itemIndex, 1);
                                                break;
                                            }
                                        }
                                        newItems.push(itemJson);
                                        utils.jsonToStorage(allJson);
                                        return originalPut.call(store, obj, options);
                                    }
                                }
                            }
                            if (result) {
                                return originalPut.call(store, obj, options);
                            }
                            else {
                                return false;
                            }
                        }
                    });

                    deferred.resolve();
                }
                else {
                    deferred.reject(dict.get("navigator.CannotStartNavigator"));
                }
            };
           
            var allJson = utils.jsonFromStorage();
            if (allJson) {
                onAllJson(allJson);
            }
            else {
                var allName = utils.getAllName();
                var url = "/json/" + allName + ".json";
                utils.showStandby();
                request(url).then(function(allString) {
                    utils.hideStandby();
                    allJson = JSON.parse(allString);
                    utils.jsonToStorage(allJson);
                    onAllJson(allJson);
                });
            }

            return deferred.promise;
        },

        newFolder: function() {
            var deferred = new Deferred();
            var navigator = this;

            var addFolder = function(parentId, name) {
                var allJson = utils.jsonFromStorage();
                var parentJson = utils.itemJsonById(allJson, parentId);
                if (parentJson) {
                    var maxId = utils.getMaxId(allJson, 0);
                    var itemId = (parseInt(maxId) + 1).toString();
                    var childrenJson = parentJson.items;
                    if (Array.isArray(childrenJson)) {
                        var itemJson = { id: itemId, type: 'd', name: name, items: [] };
                        childrenJson.push(itemJson);
                        utils.jsonToStorage(allJson);
                        return itemId;
                    }
                }
                return null;
            }
            
            showItemNamePane(dict.get("navigator.EnterFolderName"),
                    dict.get("navigator.InvalidName")).then(function() {
                var parent;

                var onFolderAdded = function(id) {
                    if (!isNaN(parseInt(id))) {
                        var item = { id: id, parent: parent.id, type: 'd', name: name};
                        store.put(item, {
                            overwrite: true,
                            parent: parent,
                            noDnd: true
                        });
                        deferred.resolve();
                    }
                    else {
                        deferred.reject(dict.get("navigator.CannotCreateFolder"));
                    }
                };

                
                var name = $.trim(itemNameTextBox.get("value"));
                if (tree.selectedItem) {
                    if (tree.selectedItem.type == 'd') {
                        parent = tree.selectedItem;
                    }
                    else {
                        parent = navigator.itemById(tree.selectedItem.parent);
                    }
                }  
                else {
                    parent = root;
                }
                model.getChildren(parent, function(children) {
                    var duplicate;
                    $.each(children, function() {
                        if (this.name == name) {
                            duplicate = true;
                            return false;
                        }
                    });
                    if (!duplicate) {
                        var id = addFolder(parent.id, name);
                        onFolderAdded(id);
                    }
                    else {
                        deferred.reject(dict.get("navigator.CannotCreateFolderExists", name));
                    }
                });
            });
            return deferred.promise;
        },

        open: function(id) {
            var deferred = new Deferred();
            var navigator = this;
            if (!id) {
                id = navigator.getSelectedItem().id;
            }
            var item = navigator.itemById(id);
            var allJson = utils.jsonFromStorage();
            var itemJson = utils.itemJsonById(allJson, id);
            if (itemJson) {
                tree.set("selectedItem", item);
                var results = { content: itemJson.content, id: id };
                deferred.resolve(results);
            }
            else {
                deferred.reject(dict.get("navigator.CannotOpenFile"));
            }

            return deferred.promise;
        },

        save: function(doc) {
            var deferred = new Deferred();
            if (!mainContainer.isNavigatorVisible()) {
                mainContainer.toggleNavigator();
            }
            var id = doc.navigatorItemId;
            if (id) {
                var allJson = utils.jsonFromStorage();
                var itemJson = utils.itemJsonById(allJson, id);
                itemJson.content = doc.toJson();
                utils.jsonToStorage(allJson);
                deferred.resolve();
                return deferred.promise;
            }
            else {
                return this.saveAs(doc);
            }
        },

        saveAs: function(entity) {
            var deferred = new Deferred();
            var navigator = this;
            var name;
            var entityType = entity instanceof GProblem ? 'p' :
                (entity instanceof GSolution ? 's': 'f');

            var addEntity = function(parentId, entityType, name, content) {
                var allJson = utils.jsonFromStorage();
                var parentJson = utils.itemJsonById(allJson, parentId);
                if (parentJson) {
                    var maxId = utils.getMaxId(allJson, 0);
                    var itemId = (parseInt(maxId) + 1).toString();
                    var childrenJson = parentJson.items;
                    if (Array.isArray(childrenJson)) {
                        var itemJson = { id: itemId, type: entityType, name: name, content: content };
                        childrenJson.push(itemJson);
                        utils.jsonToStorage(allJson);
                        return itemId;
                    }
                }
                return null;
            };

            showItemNamePane(dict.get("navigator.EnterFileName"),
                    dict.get("navigator.InvalidName")).then(function() {

                var onSuccess = function(id) {
                    if (!isNaN(parseInt(id))) {
                        var item = { id: id, parent: parent.id, type: entityType, name: name};
                        store.put(item, {
                            overwrite: true,
                            parent: parent,
                            noDnd: true
                        });
                        navigator.expandTo(item);
                        var node = tree.getNodesByItem(item)[0];
                        node.isExpandable = false;
                        node._setExpando();
                        entity.navigatorItemId = item.id;
                        deferred.resolve();
                    }
                    else {
                        onError();
                    }
                };

                var onError = function(err) {
                    deferred.reject(err || dict.get("navigator.CannotSaveFile"));
                };

                name = $.trim(itemNameTextBox.get("value"));
                var parent;
                if (tree.selectedItem) {
                    if (tree.selectedItem.type == 'd') {
                        parent = tree.selectedItem;
                    }
                    else {
                        parent = store.query({id: tree.selectedItem.parent})[0];
                    }
                }  
                else {
                    parent = root;
                }
                model.getChildren(parent, function(children) {
                    var duplicate;
                    $.each(children, function() {
                        var child = this;
                        if (child.name == name) {
                            duplicate = this.type == 'd' ? "folder" : child;
                            return false;
                        }
                    });
                    if (!duplicate) {
                        var json = entity.toJson();
                        var id = addEntity(parent.id, entityType, name, json);
                        onSuccess(id);
                    }
                    else if (duplicate == "folder") {
                        onError(dict.get("navigator.CannotSaveFolderExists", name));
                    }
                    else {
                        widgets.yesNoDialog(
                                dict.get("navigator.FileExists", name)).yes.then(function() {
                            entity.navigatorItemId = duplicate.id;
                            navigator.save(entity).then(function() {
                                deferred.resolve();
                            });
                        });
                    }
                });
            });
            return deferred.promise;
        },

        rename: function() {
            var deferred = new Deferred();
            var item = tree.selectedItem;
            if (item) {
                showItemNamePane(dict.get("navigator.EnterNewName"), dict.get("navigator.InvalidName")).then(function() {

                    var onSuccess = function() {
                        item.name = name;
                        store.put(item, {
                            overwrite: true,
                            parent: parent,
                            noDnd: true
                        });
                        deferred.resolve(item);
                    };

                    var onError = function() {
                        var message = item.type == 'd' ?
                                dict.get("navigator.CannotRenameFolder") :
                                dict.get("navigator.CannotRenameFile");
                        deferred.reject(message);
                    };

                    var renameItem = function(id, name) {
                        var allJson = utils.jsonFromStorage();
                        var itemJson = utils.itemJsonById(allJson, id);
                        if (itemJson) {
                            itemJson.name = name;
                            utils.jsonToStorage(allJson);
                            onSuccess();
                        }
                        else {
                            onError();
                        }
                    };

                    parent = store.query({id: item.parent})[0];
                    var name = itemNameTextBox.get("value");
                    if (parent) {
                        model.getChildren(parent, function(children) {
                        var duplicate;
                            $.each(children, function() {
                                if (this.name == name) {
                                    duplicate = true;
                                    return false;
                                }
                            });
                            if (!duplicate) {
                                renameItem(item.id, name);
                            }
                            else {
                                var message = item.type == 'd' ?
                                    dict.get("navigator.CannotRenameFolderExists", name) :
                                    dict.get("navigator.CannotRenameFileExists", name);
                                deferred.reject(message);
                            }
                        });
                    }
                    else {
                        renameItem(item.id, name);
                    }
                });
            }
            return deferred.promise;
        },
        
        remove: function() {
            var deferred = new Deferred();
            var item = tree.selectedItem;

            var removeItem = function(id) {
                var allJson = utils.jsonFromStorage();
                var parentJson = utils.parentJsonByItemId(allJson, id);
                if (parentJson) {
                    var itemsJson = parentJson.items;
                    for (var itemIndex = 0; itemIndex < itemsJson.length; itemIndex++) {
                        if (itemsJson[itemIndex].id == id) {
                            itemsJson.splice(itemIndex, 1);
                            break;
                        }
                    }
                    utils.jsonToStorage(allJson);
                    return true;
                }
                return false;
            };
            
            if (removeItem(item.id)) {
                store.remove(item.id);
                deferred.resolve();
            }
            else {
                var message = item.type == 'd' ? dict.get("navigator.CannotDeleteFolder") :
                        dict.get("navigator.CannotDeleteFile");
                deferred.reject(message);
            }
            
            return deferred.promise;
        },

        itemById: function(id) {
            return store.query({id: id})[0];
        },

        startUp: function() {
            container = new LayoutContainer(arguments[0]);
            
            contentPane = new ContentPane({
                "class": "geometria_navigatorcontent",
                region: "center"
            });
            container.addChild(contentPane);
            itemNamePane = new ContentPane({
                "class": "geometria_navigatorbottom",
                region: "bottom"
            });
            container.startup();
            return container;
        },
    
        destroy: function() {
            tree.destroyRecursive();
        }
    };
});
