/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "exports",
    "geometria/action/GAnswerAction",
    "geometria/action/GAreaAction",
    "geometria/action/GBisectorAction",
    "geometria/action/GCalculateAction",
    "geometria/action/GDeselectAllAction",
    "geometria/action/GClearNotepadAction",
    "geometria/action/GCloneAction",
    "geometria/action/GColorAction",
    "geometria/action/GContentsAction",
    "geometria/action/GCubeAction",
    "geometria/action/GCutAction",
    "geometria/action/GDefaultAttitudeAction",
    "geometria/action/GRemoveFileAction",
    "geometria/action/GDivideAngleAction",
    "geometria/action/GDivideSegmentAction",
    "geometria/action/GDodecahedronAction",
    "geometria/action/GEraseSegmentAction",
    "geometria/action/GEraseSelectionAction",
    "geometria/action/GExitAction",
    "geometria/action/GExportDocumentAction",
    "geometria/action/GFitToViewAction",
    "geometria/action/GHelpAction",
    "geometria/action/GHomePageAction",
    "geometria/action/GIcosahedronAction",
    "geometria/action/GImportDocumentAction",
    "geometria/action/GInitialAttitudeAction",
    "geometria/action/GIntersectionAction",
    "geometria/action/GJoinFiguresAction",
    "geometria/action/GJoinPointsAction",
    "geometria/action/GLabelsAction",
    "geometria/action/GLanguageAction",
    "geometria/action/GLayAngleAction",
    "geometria/action/GLayDistanceAction",
    "geometria/action/GLockProblemAction",
    "geometria/action/GLogCutAction",
    "geometria/action/GLogNextAction",
    "geometria/action/GLogRewindAction",
    "geometria/action/GLogStopAction",
    "geometria/action/GMeasureAngleAction",
    "geometria/action/GMeasureDistanceAction",
    "geometria/action/GMidpointAction",
    "geometria/action/GNavigatorAction",
    "geometria/action/GNewFolderAction",
    "geometria/action/GNewProblemAction",
    "geometria/action/GNextTopicAction",
    "geometria/action/GOctahedronAction",
    "geometria/action/GOpenAction",
    "geometria/action/GPerpendicularAction",
    "geometria/action/GPreviousTopicAction",
    "geometria/action/GPrintFigureAction",
    "geometria/action/GPrismAction",
    "geometria/action/GPropertiesAction",
    "geometria/action/GPyramidAction",
    "geometria/action/GRedoAction",
    "geometria/action/GRemoveFigureAction",
    "geometria/action/GRenameFileAction",
    "geometria/action/GRenameFigureAction",
    "geometria/action/GRenamePointAction",
    "geometria/action/GRenameVariableAction",
    "geometria/action/GSaveDocumentAction",
    "geometria/action/GSaveDocumentAsAction",
    "geometria/action/GSaveFigureAction",
    "geometria/action/GScaleAction",
    "geometria/action/GSelectAllAction",
    "geometria/action/GSelectorAction",
    "geometria/action/GShearAction",
    "geometria/action/GSolveProblemAction",
    "geometria/action/GTetrahedronAction",
    "geometria/action/GTotalAreaAction",
    "geometria/action/GWireframeAction",
    "geometria/action/GUndoAction",
    "geometria/action/GVolumeAction",
    "geometria/action/GZoomInAction",
    "geometria/action/GZoomOutAction"
], function(
        exports,
        answerAction,
        areaAction,
        bisectorAction,
        calculateAction,
        deselectAllAction,
        clearNotepadAction,
        cloneAction,
        colorAction,
        contentsAction,
        cubeAction,
        cutAction,
        defaultAttitudeAction,
        removeFileAction,
        divideAngleAction,
        divideSegmentAction,
        dodecahedronAction,
        eraseSegmentAction,
        eraseSelectionAction,
        exitAction,
        exportDocumentAction,
        fitToViewAction,
        helpAction,
        homePageAction,
        icosahedronAction,
        importDocumentAction,
        initialAttitudeAction,
        intersectionAction,
        joinFiguresAction,
        joinPointsAction,
        labelsAction,
        languageAction,
        layAngleAction,
        layDistanceAction,
        lockProblemAction,
        logCutAction,
        logNextAction,
        logRewindAction,
        logStopAction,
        measureAngleAction,
        measureDistanceAction,
        midpointAction,
        navigatorAction,
        newFolderAction,
        newProblemAction,
        nextTopicAction,
        octahedronAction,
        openAction,
        perpendicularAction,
        previousTopicAction,
        printFigureAction,
        prismAction,
        propertiesAction,
        pyramidAction,
        redoAction,
        removeFigureAction,
        renameFileAction,
        renameFigureAction,
        renamePointAction,
        renameVariableAction,
        saveDocumentAction,
        saveDocumentAsAction,
        saveFigureAction,
        scaleAction,
        selectAllAction,
        selectorAction,
        shearAction,
        solveProblemAction,
        tetrahedronAction,
        totalAreaAction,
        wireframeAction,
        undoAction,
        volumeAction,
        zoomInAction,
        zoomOutAction
    ) {

    var actions = Array.prototype.slice.call(arguments).slice(1);
    require(["geometria/action/GActionBase"], function(GActionBase) {
        $.each(actions, function() {
            this.base = new GActionBase(this);
        });
    });
    var actionQueue = [];
    var currentActionIndex;
    
    exports.updateStates = function() {
        $.each(actions, function() {
            this.base.updateState();
        });
    };
    exports.queueAction = function(record) {
        actionQueue.splice(currentActionIndex + 1, actionQueue.length - currentActionIndex - 1);
        actionQueue.push(record);
        currentActionIndex++;
    };
    exports.getCurrentAction = function() {
        return actionQueue[currentActionIndex];
    };
    exports.getCurrentActionIndex = function() {
        return currentActionIndex;
    };
    exports.getNextAction = function() {
        return currentActionIndex < actionQueue.length - 1 &&
            actionQueue[currentActionIndex + 1];
    };
    exports.actionUndone = function() {
        currentActionIndex--;
    };
    exports.actionRedone = function() {
        ++currentActionIndex;
    };
    exports.clearActionQueue = function() {
        actionQueue = [];
        currentActionIndex = -1;
    };
    exports.cropActionQueue = function(toRecord) {
        currentActionIndex = -1;
        var actionIndex;
        for (actionIndex = 0; actionIndex < actionQueue.length; actionIndex++) {
            if (actionQueue[actionIndex] == toRecord) {
                currentActionIndex = actionIndex;
                break;
            }
        }
        actionQueue.splice(currentActionIndex + 1,
            actionQueue.length - currentActionIndex - 1);
    };
    exports.answerAction = answerAction;
    exports.areaAction = areaAction;
    exports.bisectorAction = bisectorAction;
    exports.calculateAction = calculateAction;
    exports.deselectAllAction = deselectAllAction;
    exports.clearNotepadAction = clearNotepadAction;
    exports.cloneAction = cloneAction;
    exports.colorAction = colorAction;
    exports.contentsAction = contentsAction;
    exports.cubeAction = cubeAction;
    exports.cutAction = cutAction;
    exports.defaultAttitudeAction = defaultAttitudeAction;
    exports.divideAngleAction = divideAngleAction;
    exports.divideSegmentAction = divideSegmentAction;
    exports.dodecahedronAction = dodecahedronAction;
    exports.eraseSegmentAction = eraseSegmentAction;
    exports.eraseSelectionAction = eraseSelectionAction;
    exports.exitAction = exitAction;
    exports.exportDocumentAction = exportDocumentAction;
    exports.fitToViewAction = fitToViewAction;
    exports.helpAction = helpAction;
    exports.homePageAction = homePageAction;
    exports.icosahedronAction = icosahedronAction;
    exports.importDocumentAction = importDocumentAction;
    exports.initialAttitudeAction = initialAttitudeAction;
    exports.intersectionAction = intersectionAction;
    exports.joinFiguresAction = joinFiguresAction;
    exports.joinPointsAction = joinPointsAction;
    exports.labelsAction = labelsAction;
    exports.languageAction = languageAction;
    exports.layAngleAction = layAngleAction;
    exports.layDistanceAction = layDistanceAction;
    exports.lockProblemAction = lockProblemAction;
    exports.logCutAction = logCutAction;
    exports.logNextAction = logNextAction;
    exports.logRewindAction = logRewindAction;
    exports.logStopAction = logStopAction;
    exports.measureAngleAction = measureAngleAction;
    exports.measureDistanceAction = measureDistanceAction;
    exports.midpointAction = midpointAction;
    exports.navigatorAction = navigatorAction;
    exports.newFolderAction = newFolderAction;
    exports.newProblemAction = newProblemAction;
    exports.nextTopicAction = nextTopicAction;
    exports.octahedronAction = octahedronAction;
    exports.openAction = openAction;
    exports.perpendicularAction = perpendicularAction;
    exports.previousTopicAction = previousTopicAction;
    exports.printFigureAction = printFigureAction;
    exports.prismAction = prismAction;
    exports.propertiesAction = propertiesAction;
    exports.pyramidAction = pyramidAction;
    exports.redoAction = redoAction;
    exports.removeFigureAction = removeFigureAction;
    exports.removeFileAction = removeFileAction;
    exports.renameFileAction = renameFileAction;
    exports.renameFigureAction = renameFigureAction;
    exports.renamePointAction = renamePointAction;
    exports.renameVariableAction = renameVariableAction;
    exports.saveDocumentAction = saveDocumentAction;
    exports.saveDocumentAsAction = saveDocumentAsAction;
    exports.saveFigureAction = saveFigureAction;
    exports.scaleAction = scaleAction;
    exports.selectAllAction = selectAllAction;
    exports.selectorAction = selectorAction;
    exports.shearAction = shearAction;
    exports.solveProblemAction = solveProblemAction;
    exports.tetrahedronAction = tetrahedronAction;
    exports.totalAreaAction = totalAreaAction;
    exports.wireframeAction = wireframeAction;
    exports.undoAction = undoAction;
    exports.volumeAction = volumeAction;
    exports.zoomInAction = zoomInAction;
    exports.zoomOutAction = zoomOutAction;
});
