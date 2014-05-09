/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dijit/layout/LayoutContainer",
    "dijit/layout/ContentPane",
    "geometria/GActions",
    "geometria/GDictionary",
    "geometria/GHelpToolBar",
    "geometria/GMainContainer"
], function(LayoutContainer, ContentPane, actions, dict, toolBar, mainContainer) {

    var mainUrl = "doc/" + dict.language + "/UsersGuide.html";
    var contentPane;
    var topics = [];
    var currentTopicIndex;
    
    var setTopic = function(topic) {
        url = mainUrl + "#" + topic;
        contentPane.set("content",
            "<iframe frameBorder='0' width='100%' height='100%' src='" + url + "'></iframe");
    };
        
    return {
        startUp: function() {
            var container = new LayoutContainer(arguments[0]);
            
            var dojoToolBar = toolBar.startUp({
                "class": "dijitMenuBar",
                region: "top"
            });
            container.addChild(dojoToolBar);
            
            contentPane = new ContentPane({
                "class": "geometria_help",
                region: "center",
                content: "<iframe frameBorder='0' width='100%' height='100%' src='" +
                    mainUrl + "'></iframe"
            });
            container.addChild(contentPane);
            container.startup();
            return container;
        },
        
        newTopic: function(topic) {
            if (!mainContainer.isHelpVisible()) {
                mainContainer.toggleHelp();
            };
            topic = topic || "";
            if (topics.length > 0) {
                if (topic == topics[currentTopicIndex]) {
                    if (topic.length < 1) {
                        setTopic(topic);
                    }
                    return;
                }
                currentTopicIndex++;
            }
            else {
                currentTopicIndex = 0;
            }
            topics.push(topic);
            setTopic(topic);
            actions.updateStates();
        },

        hasNextTopic: function() {
            return currentTopicIndex < topics.length - 1;
        },
        
        hasPreviousTopic: function() {
            return currentTopicIndex > 0;
        },
        
        nextTopic: function() {
            var topic = topics[++currentTopicIndex];
            setTopic(topic);
        },
        
        previousTopic: function() {
            var topic = topics[--currentTopicIndex];
            setTopic(topic);
        }
    };
});
