/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.awt.Cursor;
import java.awt.Frame;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.event.GDocumentModifiedEvent;
import net.geocentral.geometria.event.GEventListener;
import net.geocentral.geometria.io.GFileReader;
import net.geocentral.geometria.io.GFileWriter;
import net.geocentral.geometria.model.GCalculator;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GOptions;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.model.GXmlEntity;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GOptionsManager;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.util.GVersionManager;
import net.geocentral.geometria.util.GXmlUtils;
import net.geocentral.geometria.view.GContainer;
import net.geocentral.geometria.view.GFrame;
import net.geocentral.geometria.view.GMainPanel;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class GDocumentHandler implements GEventListener {

    public static final String SAMPLES = "samples";
    
    private static GDocumentHandler instance;

    private Map<String, AbstractAction> actionHandlers;

    private GContainer container;

    private GMainPanel mainPanel;

    private GCalculator calculator;

    private GDocument activeDocument;

    private GSolution masterSolution;

    private boolean documentModified;
    
    private String title;
    
    private String problemPath;

    private String solutionPath;

    private String figurePath;

    private List<GUndoable> actions;

    private int actionIndex = -1;
    
    private boolean selectorOn = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GDocumentHandler() {
        calculator = new GCalculator();
    }

    public static GDocumentHandler getInstance() {
        if (instance == null) {
            instance = new GDocumentHandler();
        }
        return instance;
    }

    public void init() throws Exception {
        setPaths();
        loadActions();
        updateActionHandlerStates();
    }
    
    private void setPaths() {
        File samplesDir;
        try {
            GOptions options = GOptionsManager.getInstance().getOptions();
            String language = options.getLanguage();
            samplesDir = new File(new File(new File(".").getCanonicalPath(), SAMPLES), language);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            return;
        }
        figurePath = new File(samplesDir, GDictionary.get("figures")).getPath();
        problemPath = new File(samplesDir, GDictionary.get("problems")).getPath();
        solutionPath = new File(samplesDir, GDictionary.get("solutions")).getPath();
    }
    
    private void loadActions() throws Exception {
        logger.info("");
        actionHandlers = new GActionLoader().loadActions();
        actions = new ArrayList<GUndoable>();
        actionIndex = -1;
    }

    public void updateActionHandlerStates() {
        logger.info("");
        GLog log = null;
        boolean logPlaying = false;
        if (activeDocument instanceof GSolution) {
            log = masterSolution.getLog();
            logPlaying = log.isPlaying();
        }
        getActionHandler("document.newProblem").setEnabled(
                !logPlaying);
        getActionHandler("document.newSolution").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("document.openProblem").setEnabled(
                !logPlaying);
        getActionHandler("document.openSolution").setEnabled(
                !logPlaying);
        getActionHandler("document.saveDocument").setEnabled(
                container instanceof GFrame
        		&& activeDocument != null
                && !logPlaying
                && documentModified);
        getActionHandler("document.saveDocumentAs").setEnabled(
                container instanceof GFrame
                && activeDocument != null
                && !logPlaying);
        getActionHandler("document.lockSaveProblemAs").setEnabled(
                container instanceof GFrame
                && activeDocument instanceof GProblem);
        getActionHandler("document.closeDocument").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("edit.undo").setEnabled(
                actionIndex > -1
                && !logPlaying);
        getActionHandler("edit.undo").putValue(AbstractAction.SHORT_DESCRIPTION,
                getActionHandler("edit.undo").isEnabled() ? GDictionary.get("Undo") + " "
                + getCurrentAction().getShortDescription() : GDictionary.get("Undo"));
        getActionHandler("edit.redo").setEnabled(
                actionIndex < actions.size() - 1
                && !logPlaying);
        getActionHandler("edit.redo").putValue(AbstractAction.SHORT_DESCRIPTION,
                getActionHandler("edit.redo").isEnabled() ? GDictionary.get("Redo") + " "
                + getNextAction().getShortDescription() : GDictionary.get("Redo"));
        getActionHandler("edit.answer").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("edit.envelope").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("edit.renameVariable").setEnabled(
                activeDocument != null
                && !activeDocument.getNotepad().getVariables().isEmpty());
        getActionHandler("edit.clearNotepad").setEnabled(
                activeDocument instanceof GProblem
                && !activeDocument.getNotepad().isEmpty());
        getActionHandler("edit.selectAll").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("figure.prism.3").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.prism.4").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.prism.5").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.prism.6").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.prism.N").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.pyramid.3").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.pyramid.4").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.pyramid.5").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.pyramid.6").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.pyramid.N").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.tetrahedron").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.cube").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.octahedron").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.dodecahedron").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.icosahedron").setEnabled(
                activeDocument != null
                && !logPlaying);
        getActionHandler("figure.open").setEnabled(
                activeDocument instanceof GProblem);
        getActionHandler("figure.clone").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("figure.save").setEnabled(
                container instanceof GFrame
        		&& activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("figure.exportOff").setEnabled(
                container instanceof GFrame
                && activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("figure.exportImage").setEnabled(
                container instanceof GFrame
                && activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("figure.rename").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && (activeDocument instanceof GProblem
                || !logPlaying
                && !((GSolution)activeDocument).isImported(
                        activeDocument.getSelectedFigure())));
        getActionHandler("figure.remove").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && (activeDocument instanceof GProblem
                || !logPlaying
                && !((GSolution)activeDocument).isImported(
                        activeDocument.getSelectedFigure())));
        getActionHandler("figure.print").setEnabled(
                container instanceof GFrame
                && activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("measure.distance").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("measure.angle").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("measure.area").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("measure.volume").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("transform.scale").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && (activeDocument instanceof GProblem
                || !logPlaying
                && !((GSolution)activeDocument).isImported(
                        activeDocument.getSelectedFigure())));
        getActionHandler("transform.shear").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && (activeDocument instanceof GProblem
                || !logPlaying
                && !((GSolution)activeDocument).isImported(
                        activeDocument.getSelectedFigure())));
        getActionHandler("transform.cut").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("transform.join").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.drawLine").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.drawPerpendicular").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.divideLine").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.drawMidpoint").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.divideAngle").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.drawBisector").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.intersectLines").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.layDistance").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.layAngle").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.renamePoint").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && (activeDocument instanceof GProblem));
        getActionHandler("draw.eraseLine").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("draw.eraseSelection").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !logPlaying);
        getActionHandler("view.toggleSelector").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("view.zoomIn").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("view.zoomOut").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("view.fitToView").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("view.initialAttitude").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("view.defaultAttitude").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("view.toggleTransparency").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures());
        getActionHandler("view.toggleLabels").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && activeDocument.getSelectedFigure().isTransparent());
        getActionHandler("view.setColor").setEnabled(
                activeDocument != null
                && activeDocument.hasFigures()
                && !activeDocument.getSelectedFigure().isTransparent());
        getActionHandler("log.playBack").setEnabled(
                activeDocument instanceof GSolution
                && !logPlaying
                && !log.isEmpty());
        getActionHandler("log.stop").setEnabled(
                logPlaying);
        getActionHandler("log.next").setEnabled(
                logPlaying
                && log.getCurrentPos() < log.size() - 1);
        getActionHandler("log.clear").setEnabled(
                activeDocument instanceof GSolution
                && !log.isEmpty());
        getActionHandler("log.cut").setEnabled(
                logPlaying
                && log.getCurrentPos() >= 0
                && log.getCurrentPos() < log.size() - 1);
        GOptions options = GOptionsManager.getInstance().getOptions();
        int fontSize = options.getFont().getSize();
        String actionName = String.format("options.font.%s", fontSize);
        getActionHandler(actionName).putValue(Action.SELECTED_KEY, true); 
        String language = options.getLanguage();
        actionName = String.format("options.language.%s", language);
        getActionHandler(actionName).putValue(Action.SELECTED_KEY, true); 
    }

    public boolean onCloseDocument() {
        logger.info("");
        if (!(container instanceof GFrame))
        	return true;
        GDocument document = activeDocument instanceof GSolution ?
            masterSolution: activeDocument;
        if (document == null)
            return true;
        if (!documentModified)
            return true;
        int option = GGraphicsFactory.getInstance().showYesNoCancelDialog(GDictionary.get("SaveDocument"));
        if (option == JOptionPane.YES_OPTION) {
            try {
                if (saveDocument())
                    return true;
            }
            catch (Exception exception) {
                logger.error("Could not save document. Closing cancelled");
                return false;
            }
        }
        if (option == JOptionPane.NO_OPTION)
            return true;
        return false;
    }

    public boolean saveDocument() {
        logger.info("");
        GDocument document = activeDocument instanceof GSolution ?
        		masterSolution : activeDocument;
        String filePath = document instanceof GProblem ? problemPath
                : solutionPath;
        if (document.isPrime() || filePath == null)
            return saveDocumentAs();
        StringBuffer buf = new StringBuffer();
        document.serialize(buf, true);
        FileFilter[] fileFilters = {};
        GFileWriter writer = getFileWriter(getOwnerFrame(),
                filePath, fileFilters, false);
        try {
            writer.write(String.valueOf(buf));
        }
        catch (Exception exception) {
            error(exception);
            return false;
        }
        document.setPrime(false);
        setDocumentModified(false);
        logger.info(filePath);
        return true;
    }

    public boolean saveDocumentAs() {
        logger.info("");
        GDocument document = activeDocument instanceof GSolution ?
                masterSolution : activeDocument;
        String filePath = document instanceof GProblem ? problemPath
                : solutionPath;
        StringBuffer buf = new StringBuffer();
        document.serialize(buf, true);
        FileFilter[] fileFilters = {};
        GFileWriter writer = getFileWriter(getOwnerFrame(),
                filePath, fileFilters, true);
        try {
            writer.selectFile();
            if (!writer.approved())
                return false;
            if (writer.fileExists()) {
                int option = GGraphicsFactory.getInstance().showYesNoDialog(
                    GDictionary.get("FileExistsOverwrite"));
                if (option != JOptionPane.YES_OPTION)
                    return false;
            }
            writer.write(String.valueOf(buf));
        }
        catch (Exception exception) {
            error(exception);
            return false;
        }
        filePath = writer.getSelectedFilePath();
        if (document instanceof GProblem)
            problemPath = filePath;
        else
            solutionPath = filePath;
        document.setPrime(false);
        String fileName = new File(filePath).getName();
        setTitle(fileName);
        setDocumentModified(false);
        logger.info(filePath);
        return true;
    }

    public boolean lockSaveProblemAs() {
        logger.info("");
        StringBuffer buf = new StringBuffer();
        ((GProblem)activeDocument).serialize(buf, true, true);
        FileFilter[] fileFilters = {};
        GFileWriter writer = getFileWriter(getOwnerFrame(),
                problemPath, fileFilters, true);
        try {
            writer.selectFile();
            if (!writer.approved())
                return false;
            if (writer.fileExists()) {
                int option = GGraphicsFactory.getInstance().showYesNoDialog(
                    GDictionary.get("FileExistsOverwrite"));
                if (option != JOptionPane.YES_OPTION)
                    return false;
            }
            writer.write(String.valueOf(buf));
        }
        catch (Exception exception) {
            error(exception);
            return false;
        }
        problemPath = writer.getSelectedFilePath();
        logger.info(problemPath);
        return true;
    }
    
    public GFigure newFigure(GSolid solid, double zoomFactor) {
        logger.info(solid);
        GFigure figure = new GFigure(solid, zoomFactor);
        List<String> figureNames = activeDocument.getFigureNames();
        String name = GLabelFactory.getInstance().newFigureName(figureNames);
        figure.setName(name);
        activeDocument.addFigure(figure);
        activeDocument.setSelectedFigure(name);
        addFigure(figure);
        return figure;
    }

    public GFigure newFigure(GSolid solid) {
        return newFigure(solid, 1);
    }
    
    public void addFigure(GFigure figure, int index) {
        logger.info(figure.getName() + ", " + index);
        mainPanel.addFigure(figure, index);
    }

    public void addFigure(GFigure figure)
    {
        logger.info(figure.getName());
        mainPanel.addFigure(figure);
    }
    
    public void removeFigure(String figureName) {
        logger.info(figureName);
        mainPanel.removeFigure(figureName);
    }

    public void renameFigure(String oldName, String newName) {
        logger.info(oldName + ", " + newName);
        mainPanel.renameFigure(oldName, newName);
    }

    public void clearUndoableActions() {
        logger.info("");
        actions.clear();
        actionIndex = -1;
    }

    public AbstractAction getActionHandler(String name) {
        return actionHandlers.get(name);
    }

    public void addAction(GUndoable action) {
        for (int i = actions.size() - 1; i > actionIndex; i--)
            actions.remove(i);
        actions.add(action);
        actionIndex++;
    }

    public GUndoable getCurrentAction() {
        return actions.get(actionIndex);
    }
    
    public void toPreviousAction() {
        actionIndex--;
    }

    public GUndoable toNextAction() {
        return actions.get(++actionIndex);
    }
    
    private GUndoable getNextAction() {
        return actions.get(actionIndex + 1);
    }
    
    public boolean hasNextAction() {
        return actionIndex < actions.size() - 1;
    }
    
    public void removeLoggableFollowing(GLoggable action) {
        logger.info(action);
        int index = actions.indexOf(action);
        for (int i = actions.size() - 1; i > index; i--) {
            GUndoable a = actions.get(i);
            if (a instanceof GLoggable)
                actions.remove(i);
        }
        actionIndex = actions.size() - 1;
    }

    public void setDocument(String documentLocation, String filePath) throws Exception {
        logger.info(documentLocation);
        Frame ownerFrame = getOwnerFrame();
        ownerFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        InputSource source = null;
        try {
            InputStream inputStream = GDocumentHandler.class.getResourceAsStream(documentLocation);
            if (inputStream != null) {
                source = new InputSource(inputStream);
            }
        }
        catch (Exception exception) {
        }
        if (source == null) {
            source = new InputSource(documentLocation);
        }
        GXmlEntity xmlEntity;
        try {
            xmlEntity = GXmlUtils.readXmlEntity(source);
            if (xmlEntity instanceof GProblem) {
                GOpenProblemAction action = new GOpenProblemAction();
                action.execute((GProblem)xmlEntity);
                if (filePath != null) {
                    action.setFilePath(filePath);
                }
                updateActionHandlerStates();
            }
            if (xmlEntity instanceof GSolution) {
                GOpenSolutionAction action = new GOpenSolutionAction();
                action.execute((GSolution)xmlEntity);
                if (filePath != null) {
                    action.setFilePath(filePath);
                }
                updateActionHandlerStates();
            }
            String[] tokens = documentLocation.split("[\\\\\\/]");
            int tokenCount = tokens.length;
            if (tokenCount > 0) {
                String documentName = tokens[tokenCount - 1];
                setTitle(documentName);
            }
        }
        catch (Exception exception) {
            String message = String.format("Cannot open document at %s: %s", documentLocation, exception);
            throw new Exception(message);
        }
        finally {
            ownerFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    public void exit() {
        logger.info("");
        container.exit();
    }

    public void handleEvent(EventObject event) {
        logger.info(event.getSource());
        if (event instanceof GDocumentModifiedEvent) {
            updateActionHandlerStates();
        }
    }

    public void error(Exception exception) {
        error(exception.getMessage());
    }

    public void error(String message) {
        GGraphicsFactory.getInstance().showErrorDialog(message);
    }

    public void setContainer(GContainer container) {
        this.container = container;
        setTitle(null);
    }

    public GDocument getActiveDocument() {
        return activeDocument;
    }

    public void setActiveDocument(GDocument document) {
        logger.info("");
        activeDocument = document;
    }

    public GSolution getMasterSolution() {
        return masterSolution;
    }

    public void setMasterSolution(GSolution document) {
        logger.info("");
        masterSolution = document;
    }

    public Frame getOwnerFrame() {
        return container.getOwnerFrame();
    }

    public String getProblemPath() {
        return problemPath;
    }

    public String getSolutionPath() {
        return solutionPath;
    }

    public String getFigurePath() {
        return figurePath;
    }

    public void setProblemPath(String filePath) {
        logger.info(filePath);
        problemPath = filePath;
    }

    public void setSolutionPath(String filePath) {
        logger.info(filePath);
        solutionPath = filePath;
    }

    public void setFigurePath(String filePath) {
        logger.info(filePath);
        figurePath = filePath;
    }

    public GFileReader getFileReader(Frame ownerFrame, String filePath,
            FileFilter[] filters, boolean acceptAllFileFilter) {
        return container.getFileReader(ownerFrame, filePath, filters, acceptAllFileFilter);
    }

    public GFileWriter getFileWriter(Frame ownerFrame, String filePath, 
            FileFilter[] filters, boolean acceptAllFileFilter) {
        return container.getFileWriter(ownerFrame, filePath, filters, acceptAllFileFilter);
    }

    public void documentChanged() {
        logger.info("");
        mainPanel.documentChanged(activeDocument);
    }

    public void figureSelectionChanged() {
        logger.info("");
        mainPanel.figureSelectionChanged();
    }
    
    public void setTitle(String title) {
        this.title = title;
        updateTitleBar();
    }
    
    private void updateTitleBar() {
        if (!(container instanceof GFrame))
            return;
        String barTitle = GVersionManager.getInstance().getApplicationName();
        if (title != null) {
            barTitle = title + " - " + barTitle;
            if (documentModified && !title.startsWith("*")) {
                barTitle = "*" + barTitle;
            }
        }
        getOwnerFrame().setTitle(barTitle);
    }
    
    public void notepadChanged() {
        logger.info("");
        mainPanel.notepadChanged(activeDocument);
    }

    public boolean isSelectorOn() {
        return selectorOn;
    }

    public void setSelectorOn(boolean selectorOn) {
        logger.info(selectorOn);
        this.selectorOn = selectorOn;
    }

    public GCalculator getCalculator() {
        return calculator;
    }

    public void setMainPanel(GMainPanel mainPanel) {
        logger.info("");
        this.mainPanel = mainPanel;
    }

    public void setDocumentModified(boolean modified) {
        documentModified = modified;
        updateTitleBar();
    }
    
    public boolean getDocumentModified() {
        return documentModified;
    }
    
    public void updateSaveActionHandlerState(boolean enabled) {
        getActionHandler("document.saveDocument").setEnabled(enabled);
    }
    
    public void languageChanged() throws Exception {
        container.load();
        setTitle(null);
    }
    
    public void fontChanged() throws Exception {
        container.load();
    }
}
