/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;
import javax.swing.JOptionPane;

import net.infonode.docking.*;
import net.infonode.docking.util.*;

/**
 * Manage open documents.
 */
public class DocumentManager implements FocusListener {
    int lastIndex;
    TabWindow Documents;
    StringViewMap Views = new StringViewMap();
    public SchemeTextArea activeDocument;
    
    /**
     * Manage documents.
     * @param views View map.
     * @param documents Document tab manager.
     */
    public DocumentManager(StringViewMap views, TabWindow documents) {
        lastIndex = 0;
        Views = views;
        Documents = documents;
    }
    
    /**
     * Create a new document.
     */
    public boolean New() {
        lastIndex++;
        
        String id = "document-" + lastIndex;

        SchemeTextArea ss = new SchemeTextArea();
        ss.code.addFocusListener(this);

        Views.addView(id, new View("<new document>", null, ss));
        ss.myView = Views.getView(id);
        
        Documents.addTab(Views.getView(id));

        ss.code.requestFocusInWindow();

        return true;
    }
    
    /**
     * Load a file from a dialog.
     * @return If the load worked.
     */
    public boolean Open() {
        FileDialog fc = new FileDialog(MainFrame.me(), "Open...", FileDialog.LOAD);
        fc.setVisible(true);
        
        if (fc.getFile() == null)
            return false;

        File file = new File(fc.getFile());
        if (!file.exists())
        {
            ErrorFrame.log("Unable to load file (does not exist): " + fc.getFile());
            return false;
        }
        
        return Open(file);
    }

    /**
     * Load a specific file.
     * @param file The file to load.
     * @return If the load worked.
     */
    public boolean Open(File file)
    {
        lastIndex++;
        
        String id = "document-" + lastIndex;
        String filename = file.getName();
        
        try {
            if (!file.exists())
                file.createNewFile();

            Scanner scanner = new Scanner(file);
            StringBuilder content = new StringBuilder();
            String NL = System.getProperty("line.separator");

            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
                content.append(NL);
            }

            SchemeTextArea ss = new SchemeTextArea(content.toString());
            ss.myFile = file;
            ss.code.addFocusListener(this);

            Views.addView(id, new View(filename, null, ss));
            ss.myView = Views.getView(id);

            Documents.addTab(Views.getView(id));
            
            ss.code.requestFocusInWindow();

            return true;
        }
        catch(IOException ex) {
            ErrorFrame.log("Unable to load file (" + file.getName() + "): " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Save the current file.
     * @return If the save worked.
     */
    public boolean Save() {
        if (activeDocument == null)
            return false;
        if (activeDocument.myFile == null)
            return SaveAs();
        
        try {
            Writer out = new OutputStreamWriter(new FileOutputStream(activeDocument.myFile));
            out.write(activeDocument.getText());
            out.flush();
            out.close();
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
    public boolean SaveAs()
    {
        if (activeDocument == null)
            return false;

        FileDialog fc = new FileDialog(MainFrame.me(), "Save as...", FileDialog.SAVE);
        fc.setVisible(true);
        if (fc.getFile() == null)
            return false;

        File file = new File(fc.getFile());
        activeDocument.myFile = file;
        activeDocument.myView.getViewProperties().setTitle(file.getName());

        return Save();
    }

    /**
     * Close the active document.
     * @return If it worked.
     */
    public boolean Close()
    {
        if (activeDocument == null)
            return false;

        if (!activeDocument.isEmpty())
        {
            String name = activeDocument.myView.getViewProperties().getTitle();
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                    activeDocument,
                    "Save " + name + " before closing?",
                    "Close...",
                    JOptionPane.YES_NO_OPTION
                ))
            {
                Save();
            }
        }
        
        View toClose = activeDocument.myView;
        
        if (Documents.getChildWindowCount() == 1)
        	New();
        
        toClose.close();
        	        
        return true;
    }

    /**
     * Run the active document.
     * @return If it worked.
     */
    public boolean Run()
    {
        if (activeDocument == null)
            return false;

        if (!Save())
            return false;

        MainFrame.me().doCommand("(load \"" + activeDocument.myFile.getAbsolutePath().replace("\\", "/")  + "\")");
        MainFrame.me().REPL.code.requestFocusInWindow();

        return true;
    }

    /**
     * Format the active document.
     * @return If it worked.
     */
    public boolean Format()
    {
        if (activeDocument == null)
            return false;
        
        activeDocument.format();
        
        return true;
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
    @Override
    public void focusLost(FocusEvent e) {
    }
}