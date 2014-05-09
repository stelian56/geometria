/**
 * Copyright (C) 2000-2014 Geometria Contributors
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

    var getUrl = function(parameters) {
        var params = lang.mixin({
            lang: dict.language
        }, parameters);
        var queryString = "";
        $.each(params, function(key, value) {
            if (queryString.length) {
                queryString += "&";
            }
            queryString += key + "=" + value;
        });
        return mainContainer.baseUrl + "?" + queryString;
    };

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

        populate: function() {
            var deferred = new Deferred();
            var storedata = [];
            var sort = function(item1, item2) {
                if (item1.type == item2.type) {
                    return item1.name > item2.name;
                }
                return item1.type > item2.type;
            };
            
            var memoryStore = new Memory({
                data: storedata,
                getChildren: function(parent) {
                    var rs = this.query({ parent: parent.id }, {sort: sort});
                    return rs;
                }
            });
            store = new Observable(memoryStore);

            aspect.around(store, "put", function(originalPut){
                return function(obj, options) {
                    var result = false;

                    var onSuccess = function(webdata) {
                        if ($.trim(webdata) != "OK") {
                            onError();
                        }
                        else {
                            result = true;
                        }
                    };
                    
                    var onError = function(server) {
                        var message = server ? dict.get("navigator.CannotDragAndDropServer") :
                            dict.get("navigator.CannotDragAndDropDb");
                        widgets.errorDialog(message);
                        result = false;
                    };
                
                    if(options && options.parent){
                        obj.parent = options.parent.id;
                    }
                    if (options.noDnd) {
                        result = true;
                    }
                    else {
                        var url = getUrl({command: "setparent",
                            id: obj.id, parent_id: options.parent.id});
                        xhr(url, {sync: true}).then(onSuccess, onError);
                    }
                    if (result) {
                        return originalPut.call(store, obj, options);
                    }
                    else {
                        return false;
                    }
                }
            });
            
            var onError = function() {
                deferred.reject(dict.get("navigator.CannotStartNavigator"));
            };

            var onSuccess = function(webdata) {
                $.each(webdata.split("\n"), function() {
                    if (this.length) {
                        var tokens = this.split("|");
                        var id = tokens[0];
                        var parentId;
                        if (tokens[1]) {
                            parentId = tokens[1];
                        }
                        var type = tokens[2];
                        var name = parentId ? tokens[3] : dict.get("RootFolder");
                        var child = { id: id, parent: parentId, type: type, name: name };
                        storedata.push(child);
                        if (!parentId) {
                            root = child;
                        }
                    }
                });
                if (root) {
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
                            if (mainContainer.readOnly || logContainer.isPlaybackActive()) {
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
                    deferred.resolve();
                }
                else {
                    onError();
                }
            };
            
            var url = getUrl({command: "getall"});
            request(url).then(onSuccess, onError);
            return deferred.promise;
        },

        expandTo: function(item) {
            var path = [ item.id ];
            var parent = this.itemById(item.parent);
            while (parent) {
                path.splice(0, 0, parent.id);
                parent = this.itemById(parent.parent);
            }
            tree.set("path", path);
        },
        
        newFolder: function() {
            var navigator = this;
            var deferred = new Deferred();
            
            showItemNamePane(dict.get("navigator.EnterFolderName"),
                    dict.get("navigator.InvalidName")).then(function() {
                var parent;
                var onError = function(server) {
                    utils.hideStandby();
                    var message = server ? dict.get("navigator.CannotCreateFolderServer") :
                        dict.get("navigator.CannotCreateFolderDb");
                    deferred.reject(message);
                };

                var onSuccess = function(webdata) {
                    utils.hideStandby();
                    var id = $.trim(webdata);
                    if (!isNaN(parseInt(id))) {
                        var item = { id: id, parent: parent.id, type: 'd', name: name };
                        store.put(item, {
                            overwrite: true,
                            parent: parent,
                            noDnd: true
                        });
                        navigator.expandTo(item);
                        deferred.resolve();
                    }
                    else {
                        onError();
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
                        var url = getUrl({command: "add", parent_id: parent.id, type: 'd',
                            name: name});
                        utils.showStandby();
                        request(url).then(onSuccess, onError);
                    }
                    else {
                        deferred.reject(dict.get("navigator.CannotCreateFolderExists", name));
                    }
                });
            });
            return deferred.promise;
        },

        getSelectedItem: function() {
            return tree && tree.selectedItem;
        },

        selectItem: function(id) {
            var item = this.itemById(id);
            if (item) {
                this.expandTo(item);
                tree.set("selectedItem", item);
            }
        },
        
        open: function(id) {
            var deferred = new Deferred();
            var navigator = this;
            if (!id) {
                id = navigator.getSelectedItem().id;
            }
            var url = getUrl({command: "getcontent", table: "samples", id: id});
            var item = navigator.itemById(id);

            var onSuccess = function(webdata) {
                utils.hideStandby();
                var results = { content: webdata, id: id };
                tree.set("selectedItem", item);
                deferred.resolve(results);
            };

            var onError = function() {
                utils.hideStandby();
                deferred.reject(dict.get("navigator.CannotOpenFile"));
            };
            
            utils.showStandby();
            request(url).then(onSuccess, onError);
            return deferred.promise;
        },

        save: function(entity) {
            if (!mainContainer.isNavigatorVisible()) {
                mainContainer.toggleNavigator();
            }
            var deferred = new Deferred();

            var onSuccess = function(webdata) {
                utils.hideStandby();
                var result = $.trim(webdata);
                if (result == "OK") {
                    deferred.resolve();
                }
                else {
                    onError();
                }
            };
            
            onError = function() {
                utils.hideStandby();
                deferred.reject(dict.get("navigator.CannotSaveFile"));
            };

            var id = entity.navigatorItemId;
            if (id) {
                var json = entity.toJson();
                var content = JSON.stringify(json);
                var url = getUrl({command: "setcontent", id: id});
                
                utils.showStandby();
                request(url, { method: "POST", data: content }).then(onSuccess, onError);
                return deferred.promise;
            }
            else {
                return this.saveAs(entity);
            }
        },

        saveAs: function(entity) {
            var navigator = this;
            var deferred = new Deferred();
            var name;
            var entityType = entity instanceof GProblem ? 'p' :
                (entity instanceof GSolution ? 's': 'f');
            
            showItemNamePane(dict.get("navigator.EnterFileName"),
                    dict.get("navigator.InvalidName")).then(function() {
                var onSuccess = function(webdata) {
                    utils.hideStandby();
                    var id = $.trim(webdata);
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
                    utils.hideStandby();
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
                        var content = JSON.stringify(json);
                        var url = getUrl({command: "add", parent_id: parent.id, type: entityType,
                            name: name});
                        utils.showStandby();
                        request(url, { method: "POST", data: content }).then(onSuccess, onError);
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
                    parent = store.query({id: item.parent})[0];
                    var name = itemNameTextBox.get("value");

                    var doRename = function() {
                        var url = getUrl({command: "rename", id: item.id, name: name});

                        var onError = function(server) {
                            utils.hideStandby();
                            var message;
                            if (server) {
                                message = item.type == 'd' ?
                                    dict.get("navigator.CannotRenameFolderServer") :
                                    dict.get("navigator.CannotRenameFileServer");
                            }
                            else {
                                message = item.type == 'd' ?
                                    dict.get("navigator.CannotRenameFolderDb") :
                                    dict.get("navigator.CannotRenameFileDb");
                            }
                            deferred.reject(message);
                        };
                        
                        var onSuccess = function(webdata) {
                            utils.hideStandby();
                            result = $.trim(webdata);
                            if (result == "OK") {
                                item.name = name;
                                store.put(item, {
                                    overwrite: true,
                                    parent: parent,
                                    noDnd: true
                                });
                                deferred.resolve(item);
                            }
                            else {
                                onError();
                            }
                        };
                        
                        utils.showStandby();
                        request(url).then(onSuccess, onError);
                    };

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
                                doRename();
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
                        doRename();
                    }
                });
            }
            return deferred.promise;
        },
        
        isSelectedItemRemovable: function() {
            if (tree) {
                var item = tree.selectedItem;
                return item && item.id != root.id;
            }
            return false;
        },
        
        remove: function() {
            var deferred = new Deferred();
            var item = tree.selectedItem;
            var url = getUrl({command: "delete", id: item.id});
            
            var onError = function(server) {
                utils.hideStandby();
                var message;
                if (server) {
                    message = item.type == 'd' ? dict.get("navigator.CannotDeleteFolderServer") :
                        dict.get("navigator.CannotDeleteFileServer");
                }
                else {
                    message = item.type == 'd' ? dict.get("navigator.CannotDeleteFolderDb") :
                        dict.get("navigator.CannotDeleteFileDb");
                }
                deferred.reject(message);
            };

            var onSuccess = function(webdata) {
                utils.hideStandby();
                result = $.trim(webdata);
                if (result == "OK") {
                    store.remove(item.id);
                    deferred.resolve();
                }
                else {
                    onError();
                }
            };
            
            utils.showStandby();
            request(url).then(onSuccess, onError);
            return deferred.promise;
        },

        itemById: function(id) {
            return store.query({id: id})[0];
        },
        
        isReadOnly: function() {
            var deferred = new Deferred();
            var navigator = this;
            var url = getUrl({command: "isreadonly"});

            var onSuccess = function(webdata) {
                var readOnly = webdata.trim().toLowerCase() == "true";
                deferred.resolve(readOnly);
            };

            request(url).then(onSuccess);
            return deferred.promise;
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
