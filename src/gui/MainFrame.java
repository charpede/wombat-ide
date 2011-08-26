package gui;

import javax.swing.*;

import wombat.Wombat;
import util.KawaWrap;
import util.OutputIntercept;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.infonode.docking.*;
import net.infonode.docking.util.*;

/**
 * Create a main frame.
 */
public class MainFrame extends JFrame {
	private static final long serialVersionUID = 2574330949324570164L;

	// Woo singletons.
    static MainFrame me;

    // Things we may need access to.
    public RootWindow Root;
    public DocumentManager Documents;
    public HistoryTextArea History;
    public REPLTextArea REPL;
    public KawaWrap kawa;

    /**
     * Don't directly create this, use me().
     * Use this method to set it up though.
     */
    private MainFrame() {
        // Set frame options.
        setTitle("Wombat - Build " + Wombat.VERSION);
        setSize(Options.DisplayWidth, Options.DisplayHeight);
        setLocation(Options.DisplayLeft, Options.DisplayTop);
        setLayout(new BorderLayout(5, 5));
        
        // Wait for the program to end.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	Documents.CloseAll();
            	Options.DisplayTop = Math.max(0, e.getWindow().getLocation().y);
            	Options.DisplayLeft = Math.max(0, e.getWindow().getLocation().x);
            	Options.DisplayWidth = Math.max(400, e.getWindow().getWidth());
            	Options.DisplayHeight = Math.max(400, e.getWindow().getHeight());
            	Options.save();
                System.exit(0);
            }
        });

        // Set up the menus using the above definitions.
        setJMenuBar(MenuManager.menu());
        
        // Create a display for any open documents.
        TabWindow documents = new TabWindow();
        StringViewMap viewMap = new StringViewMap();
        Documents = new DocumentManager(viewMap, documents);
        Documents.New();
         
        // Create displays for a split REPL.
        History = new HistoryTextArea();
        REPL = new REPLTextArea();
        viewMap.addView("REPL - Execute", new View("REPL - Execute", null, REPL));
        viewMap.addView("REPL - History", new View("REPL - History", null, History));
        SplitWindow replSplit = new SplitWindow(false, viewMap.getView("REPL - Execute"), viewMap.getView("REPL - History"));
        
        // Put everything together into the actual dockable display.
        SplitWindow fullSplit = new SplitWindow(false, 0.6f, documents, replSplit);
        Root = DockingUtil.createRootWindow(new ViewMap(), true);
        Root.setWindow(fullSplit);
        add(Root);
        
        // Connect to Kawa.
        kawa = new KawaWrap();
        
        // Bind a to catch anything that goes to stdout or stderr.
        Thread t = new Thread(new Runnable() {
        	public void run() {
        		OutputIntercept.enable();
        		
        		while (true) {
        			if (OutputIntercept.hasContent())
        				History.append(OutputIntercept.getContent() + "\n");
        			
        			try { Thread.sleep(50); } catch(Exception e) {}
        		}
        	}	
        });
        t.setDaemon(true);
        t.start();
    }

	/**
     * Run a command.
     *
     * @param command The command to run.
     */
    void doCommand(String command) {
        command = command.trim();
        if (command.length() == 0)
            return;

        History.append("\n>>> " + command.replace("\n", "\n    ") + "\n");
        
        Object result = kawa.eval(command);
        if (result != null)
        	History.append(result.toString() + "\n");
    }

    /**
     * Access the frame.
     *
     * @return The singleton frame.
     */
    public static MainFrame me() {
        if (me == null)
            me = new MainFrame();

        return me;
    }
}
