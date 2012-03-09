/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wombat.util.files;


import java.awt.Component;
import java.awt.FileDialog;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import wombat.gui.frames.FindReplaceDialog;
import wombat.gui.frames.MainFrame;
import wombat.gui.text.SchemeTextArea;
import wombat.gui.text.SharedTextArea;
import wombat.util.Options;
import wombat.util.errors.ErrorManager;

import net.infonode.docking.*;
import net.infonode.docking.util.*;

/**
 * Manage open documents.
 */
public final class DocumentManager implements FocusListener {
	static DocumentManager me;
	
	// Next file to create.
	int lastIndex;
	
	// GUI references.
	MainFrame Main;
	RootWindow Root;
    TabWindow Documents;
    StringViewMap Views = new StringViewMap();
    
    // Active documents.
    List<SchemeTextArea> allDocuments;
    SchemeTextArea activeDocument;
    
    // Hide constructor.
    private DocumentManager() {}
    
    /**
     * Manage documents.
     * 
     * @param main The main frame.
     * @param root The root window (for splitting when strange things happen).
     * @param views View map (holds all of the documents).
     * @param documents Document tab manager (holds open documents at first).
     */
    public static DocumentManager init(MainFrame main, RootWindow root, StringViewMap views, TabWindow documents) {
    	if (me == null) {
	    	me = new DocumentManager();
	    	
	        me.lastIndex = 0;
	        
	        me.Main = main;
	        me.Root = root;
	        me.Views = views;
	        me.Documents = documents;
	        
	        me.allDocuments = new ArrayList<SchemeTextArea>();
    	}
        
        return me;
    }
    
    /**
     * Create a new document.
     */
    public static boolean New() {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
        me.lastIndex++;
        String id = "document-" + me.lastIndex;

        SchemeTextArea ss = new SchemeTextArea();
        me.allDocuments.add(ss);
        ss.code.addFocusListener(me);
        
        me.Views.addView(id, new View("<new document>", null, ss));
        ss.myView = me.Views.getView(id);
        ss.myView.addListener(new ViewCloseListener(ss));
        
        me.Documents.addTab(me.Views.getView(id));
        
        if (me.Root != null && !me.Documents.isShowing())
        	me.Root.setWindow(new SplitWindow(false, 0.6f, me.Documents, me.Root.getWindow()));
         
        ss.code.requestFocusInWindow();
        
        return true;
    }
    
    /**
     * Load a file from a dialog.
     * @return If the load worked.
     */
    public static boolean Open() {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
    	FileDialog fc = new FileDialog(me.Main, "Open...", FileDialog.LOAD);
        fc.setVisible(true);
        
        if (fc.getFile() == null)
            return false;
        
        File file = new File(fc.getDirectory(), fc.getFile());
        if (!file.exists())
        {
        	ErrorManager.logError("Unable to load file (does not exist): " + fc.getFile());
            return false;
        }
        
        return Open(file);
    }

    /**
     * Load a specific file.
     * @param file The file to load.
     * @return If the load worked.
     */
    public static boolean Open(File file) {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
    	// Check if the document was already opened, it it was use that one.
    	for (SchemeTextArea ss : me.allDocuments) {
    		if (ss.myFile != null && ss.myFile.equals(file)) {
    			for (int i = 0; i < me.Documents.getChildWindowCount(); i++) {
    	    		if (me.Documents.getChildWindow(i).equals(ss.myView)) {
    	    			me.Documents.setSelectedTab(i);
    	    			ss.code.requestFocusInWindow();
    	    			return true;
    	    		}
    			}
    		}
    	}
    	
    	// Otherwise, load the document.
        me.lastIndex++;
        
        String id = "document-" + me.lastIndex;
        String filename = file.getName();
        
        try {
            if (!file.exists())
                file.createNewFile();

            SchemeTextArea ss = new SchemeTextArea(file);
            me.allDocuments.add(ss);
            ss.myFile = file;
            ss.code.addFocusListener(me);

            me.Views.addView(id, new View(filename, null, ss));
            ss.myView = me.Views.getView(id);
            ss.myView.addListener(new ViewCloseListener(ss));

            me.Documents.addTab(me.Views.getView(id));
            
            if (me.Root != null && !me.Documents.isShowing())
            	me.Root.setWindow(new SplitWindow(false, 0.6f, me.Documents, me.Root.getWindow()));
            
            ss.code.requestFocusInWindow();

            RecentDocumentManager.addFile(file);
            
            return true;
        }
        catch(IOException ex) {
        	ErrorManager.logError("Unable to load file (" + file.getName() + "): " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Save the current file.
     * @return If the save worked.
     */
    public static boolean Save() {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
        if (me.activeDocument == null) return false;
        if (me.activeDocument.myFile == null) return SaveAs();
        
        try {
            me.activeDocument.save();
            
            RecentDocumentManager.addFile(me.activeDocument.myFile);
            
            return true;
        } catch(FileNotFoundException ex) {
            return false;
        } catch(IOException ex) {
            return false;
        }
    }

    /**
     * Save the active file with a new name.
     * @return If it worked.
     */
    public static boolean SaveAs() {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
        if (me.activeDocument == null) return false;

        FileDialog fc = new FileDialog(me.Main, "Save as...", FileDialog.SAVE);
        fc.setVisible(true);
        if (fc.getFile() == null) return false;

        File file = new File(fc.getDirectory(), fc.getFile());
        me.activeDocument.myFile = file;
        me.activeDocument.myView.getViewProperties().setTitle(file.getName());

        return Save();
    }

    /**
     * Close the active document.
     * @return If it worked.
     */
    public static boolean Close(boolean force) {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
        if (me.activeDocument == null) return false;

        if (!me.activeDocument.isEmpty()) {
            String name = me.activeDocument.myView.getViewProperties().getTitle();
            if (me.activeDocument.isDirty()) {
            	if (Options.ConfirmOnClose) {
            		int result = JOptionPane.showConfirmDialog(
            				me.activeDocument,
            				"Save " + name + " before closing?\n\n(If you select no, any unsaved work will be lost.)",
            				"Close...",
            				(force ? JOptionPane.YES_NO_OPTION : JOptionPane.YES_NO_CANCEL_OPTION));
            		
            		if (result == JOptionPane.YES_OPTION){
            			if (!Save())
            				return false;
            		} else if (result == JOptionPane.CANCEL_OPTION) {
            			return false;
            		}
            	} else {
            		Save();
            	}
            }
        }
        
        me.allDocuments.remove(me.activeDocument);
        me.activeDocument.close();
        me.activeDocument.myView.close();
        
        System.out.println("currently opened documents: " + me.allDocuments.size());
        	        
        return true;
    }
    
    /**
     * Close all documents.
     * @return If it worked.
     */
    public static boolean CloseAll() {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
    	boolean closedAll = true;
    	while (!me.allDocuments.isEmpty())
    	{
    		me.activeDocument = me.allDocuments.get(0);
    		closedAll &= Close(true);
    	}
    	return closedAll;
    }
    
    /**
     * Reload all documents (to update formatting).
     * @return If it worked.
     */
    public static boolean ReloadAll() {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
    	for (SchemeTextArea ss : me.allDocuments)
			ss.updateFont();
    	
    	return me.Main.updateDisplay();
    }

    /**
     * Run the active document.
     * @return If it worked.
     */
    public static boolean Run() {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
        if (me.activeDocument == null)
            return false;
        
        String name = me.activeDocument.myView.getViewProperties().getTitle();
        if (me.activeDocument.myFile == null || me.activeDocument.isDirty()) {
        	if (Options.ConfirmOnRun) {
        		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
	                    me.activeDocument,
	                    "Save " + name + " before running?\n\n(You must save your code to run it.)",
	                    "Save...",
	                    JOptionPane.YES_NO_OPTION)) {
        			if (!Save())
        				return false;
        		} else {
        			return false;
        		}
        	} else {
        		if (!Save()) return false;
        	}
        }
        
        try {
			me.Main.doCommand("(load \"" + me.activeDocument.myFile.getCanonicalPath().replace("\\", "/")  + "\")");
			me.Main.focusREPL();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return true;
    }

    /**
     * Format the active document.
     * @return If it worked.
     */
    public static boolean Format() {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
    	
        if (me.activeDocument == null)
            return false;
        
        me.activeDocument.format();
        
        return true;
    }
    
    /**
     * Insert a tab / return.
     * 
     * @param insertReturn Insert a newline before tabbing.
     * @return If it worked.
     */
	public static boolean Tab(boolean insertReturn) {
		if (me == null) throw new RuntimeException("Document manager not initialized.");
		
		SchemeTextArea doc = me.activeDocument;
		if (doc == null)
			return false;
		
		if (insertReturn) {
			try {
				doc.code.getDocument().insertString(doc.code.getCaretPosition(), "\n", null);
	        } catch (BadLocationException ble) {
	        	ErrorManager.logError("Unable to add a new line on ENTER.");
	        }
		}
		
		doc.tab();
		return true;
	}
	
	/**
	 * Undo on the active document.
	 * @return If it worked.
	 */
	public static boolean Undo() {
		if (me == null) throw new RuntimeException("Document manager not initialized.");
		SchemeTextArea doc = me.activeDocument;
		
		try {
            if (doc.Undo.canUndo())
            	doc.Undo.undo();
            return true;
        } catch (CannotUndoException e) {
        	return false;
        }
	}
	
	/**
	 * Redo on the active document.
	 * @return If it worked.
	 */
	public static boolean Redo() {
		if (me == null) throw new RuntimeException("Document manager not initialized.");
		SchemeTextArea doc = me.activeDocument;
		
		try {
            if (doc.Undo.canRedo())
            	doc.Undo.redo();
            return true;
        } catch (CannotRedoException e) {
        	return false;
        }
	}
	
	/**
	 * Show the find/replace dialog for the current document.
	 */
	public static void FindReplace() {
		if (me == null) throw new RuntimeException("Document manager not initialized.");
		SchemeTextArea doc = me.activeDocument;
		
		new FindReplaceDialog(me.Main, doc.code).setVisible(true);
	}

    /**
     * Keep track of which text area last had focus.
     * @param e The event.
     */
    @Override
    public void focusGained(FocusEvent e) {
        if (!(e.getSource() instanceof Component))
            return;

        Component c = (Component) e.getSource();
        while (c != null)
        {
            if (c instanceof SchemeTextArea)
            {
                activeDocument = (SchemeTextArea) c;
                return;
            }

            c = c.getParent();
        }
    }

    /**
     * Ignore this.
     * @param e
     */
    @Override public void focusLost(FocusEvent e) {
    }

    /**
     * Create a new shared document.
     * @throws Exception If we cannot host.
     */
	public static boolean NewShared() throws Exception {
    	if (me == null) throw new RuntimeException("Document manager not initialized.");
        
        SharedTextArea ss = SharedTextArea.host();
        me.allDocuments.add(ss);
        ss.code.addFocusListener(me);
        
        me.lastIndex++;
        String id = "document-" + me.lastIndex;
        
        me.Views.addView(id, new View("<new document>", null, ss));
        ss.myView = me.Views.getView(id);
        
        me.Documents.addTab(me.Views.getView(id));
        
        if (me.Root != null && !me.Documents.isShowing())
        	me.Root.setWindow(new SplitWindow(false, 0.6f, me.Documents, me.Root.getWindow()));
         
        ss.code.requestFocusInWindow();
        
        return true;
		
	}

	/**
	 * Connect to a shared document.
	 * @param The server to connect to.
	 * @throws Exception If we cannot join.
	 */
	public static boolean OpenShared(String connectTo) throws Exception {
		if (me == null) throw new RuntimeException("Document manager not initialized.");
        
        SharedTextArea ss = SharedTextArea.join(connectTo);
        me.allDocuments.add(ss);
        ss.code.addFocusListener(me);
        
        me.lastIndex++;
        String id = "document-" + me.lastIndex;
        
        me.Views.addView(id, new View("<new document>", null, ss));
        ss.myView = me.Views.getView(id);
        
        me.Documents.addTab(me.Views.getView(id));
        
        if (me.Root != null && !me.Documents.isShowing())
        	me.Root.setWindow(new SplitWindow(false, 0.6f, me.Documents, me.Root.getWindow()));
         
        ss.code.requestFocusInWindow();
        
        return true;
	}

	/**
	 * Get the file that the active document is using.
	 * @return The filename.
	 */
	public static File getActiveFile() {
		if (me == null) throw new RuntimeException("Document manager not initialized.");
		
		if (me.activeDocument == null) 
			return null;
		else 
			return me.activeDocument.myFile;
	}
}

/**
 * Remove documents from the scheme text area when they're closed. 
 */
class ViewCloseListener implements DockingWindowListener {
	SchemeTextArea SS;
		
	public ViewCloseListener(SchemeTextArea ss) {
		SS = ss;
	}

	/**
	 * When the window is closed.
	 * @param event Event parameters.
	 */
	@Override public void windowClosed(DockingWindow event) {
		SS.close();
		DocumentManager.me.allDocuments.remove(SS);
	}
	
	@Override public void windowUndocking(DockingWindow event) throws OperationAbortedException {}
	@Override public void windowUndocked(DockingWindow event) {}
	@Override public void windowShown(DockingWindow event) {}
	@Override public void windowRestoring(DockingWindow event) throws OperationAbortedException {}
	@Override public void windowRestored(DockingWindow event) {}
	@Override public void windowRemoved(DockingWindow event, DockingWindow arg1) {}
	@Override public void windowMinimizing(DockingWindow event) throws OperationAbortedException {}
	@Override public void windowMinimized(DockingWindow event) {}
	@Override public void windowMaximizing(DockingWindow event) throws OperationAbortedException {}
	@Override public void windowMaximized(DockingWindow event) {}
	@Override public void windowHidden(DockingWindow event) {}
	@Override public void windowDocking(DockingWindow event) throws OperationAbortedException {}
	@Override public void windowDocked(DockingWindow event) {}
	@Override public void windowClosing(DockingWindow event) throws OperationAbortedException {}
	@Override public void windowAdded(DockingWindow event, DockingWindow arg1) {}
	@Override public void viewFocusChanged(View event, View arg1) {}
}