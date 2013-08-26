/* 
 * License: source-license.txt
 * If this code is used independently, copy the license here.
 */

package wombat.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;

import org.xnap.commons.gui.shortcut.EmacsKeyBindings;

import wombat.gui.frames.*;
import wombat.gui.text.*;
import wombat.util.errors.ErrorManager;
import wombat.util.files.*;

/**
 * Store options.
 * 
 * TODO: Rewrite this using local files rather than Java preferences.
 */
public final class Options {
	static Preferences prefs = Preferences.userRoot().node("wombat");
	static JMenu optionsMenu;
			
	// Used for the main display. 
	public static int DisplayWidth;
	public static int DisplayHeight;
	public static int DisplayTop;
	public static int DisplayLeft;
	public static boolean DisplayToolbar;
	
	// Keyboard shortcuts.
	public static String CommandRun;
	public static String CommandFormat;
	
	// Options menu options.
	public static boolean LambdaMode;
	public static boolean GreekMode;
	public static boolean EmacsKeybindings;
	public static boolean ConfirmOnRun;
	public static boolean ConfirmOnClose;
	public static boolean BackupOnSave;
	public static boolean ViewLineNumbers;
	
	// Syntax highlighting.
	public static Map<String, Color> Colors;
	public static Map<String, Integer> Keywords;
	public static Map<String, String> KeywordHelpURLs;
	
	public static Font Font;
	public static int FontSize;
	public static int FontWidth;
	public static int FontHeight;
	
	// Previous command history.
	public static int SavedHistoryCount = 20;
	public static String SavedHistory;
	
	// Recently used documents.
	public static String RecentDocuments;
	
	// Greek mode characters
	public static final String[][] GreekModeCharacters = new String[][]{
		{"\u0391", "Alpha"},
		{"\u0392", "Beta"},
		{"\u0393", "Gamma"},
		{"\u0394", "Delta"},
		{"\u0395", "Epsilon"},
		{"\u0396", "Zeta"},
		{"\u0397", "Eta"},
		{"\u0398", "Theta"},
		{"\u0399", "Iota"},
		{"\u039A", "Kappa"},
		{"\u039B", "Lambda"},
		{"\u039C", "Mu"},
		{"\u039D", "Mu"},
		{"\u039E", "Xi"},
		{"\u039F", "Omicron"},
		{"\u03A0", "Pi"},
		{"\u03A1", "Rho"},
		{"\u03A3", "Sigma"}, // skipping 03C2 which is final sigma
		{"\u03A4", "Tau"},
		{"\u03A5", "Upsilon"},
		{"\u03A6", "Phi"},
		{"\u03A7", "Chi"},
		{"\u03A8", "Psi"},
		{"\u03A9", "Omega"},
		{"\u03B1", "alpha"},
		{"\u03B2", "beta"},
		{"\u03B3", "gamma"},
		{"\u03B4", "delta"},
		{"\u03B5", "epsilon"},
		{"\u03B6", "zeta"},
		{"\u03B7", "eta"},
		{"\u03B8", "theta"},
		{"\u03B9", "iota"},
		{"\u03BA", "kappa"},
		{"\u03BB", "lambda"},
		{"\u03BC", "mu"},
		{"\u03BD", "nu"},
		{"\u03BE", "xi"},
		{"\u03BF", "omicron"},
		{"\u03C0", "pi"},
		{"\u03C1", "rho"},
		{"\u03C3", "sigma"}, // skipping 03C2 which is final sigma
		{"\u03B4", "tau"},
		{"\u03B5", "upsilon"},
		{"\u03B6", "phi"},
		{"\u03B7", "chi"},
		{"\u03B8", "psi"},
		{"\u03B9", "omega"}
	};

	/**
     * Initialize.
     */
    static { load(); }
    
    /**
     * Load all intial options.
     */
    private static void load() {
    	DisplayTop = prefs.getInt("Display/Top", 100);
    	DisplayLeft = prefs.getInt("Display/Left", 100);
    	DisplayWidth = prefs.getInt("Display/Width", 800);
    	DisplayHeight = prefs.getInt("Display/Height", 600);
    	DisplayToolbar = prefs.getBoolean("Display/Toolbar", true);
    	
    	CommandRun = prefs.get("Command/Run", "F5");
    	CommandFormat = prefs.get("Command/Format", "F6");

    	ViewLineNumbers = prefs.getBoolean("Options/ViewLineNumbers", true);
    	LambdaMode = prefs.getBoolean("Options/LambdaMode", false);
    	GreekMode = prefs.getBoolean("Options/GreekMode", false);
    	EmacsKeybindings = prefs.getBoolean("Options/EmacsKeybindings", false);
    	ConfirmOnRun = prefs.getBoolean("Options/ConfirmOnRun", true);
    	ConfirmOnClose = prefs.getBoolean("Options/ConfirmOnClose", true);
    	BackupOnSave = prefs.getBoolean("Options/BackupOnSave", true);
    	
    	if (EmacsKeybindings)
    		EmacsKeyBindings.load();
    	
    	Colors = new HashMap<String, Color>();
    	Colors.put("default", new Color(prefs.getInt("Colors/default", 0x000000)));
    	Colors.put("comment", new Color(prefs.getInt("Colors/comment", 0x006600)));
    	Colors.put("keyword", new Color(prefs.getInt("Colors/keyword", 0x000099)));
    	Colors.put("string", new Color(prefs.getInt("Colors/string", 0xFF8C00)));
    	Colors.put("bracket", new Color(prefs.getInt("Colors/bracket", 0x00FFFF)));
    	Colors.put("invalid-bracket", new Color(prefs.getInt("Colors/invalid-bracket", 0xFF0000)));
    	
    	SavedHistory = prefs.get("SavedHistory", "");
    	
    	Keywords = new HashMap<String, Integer>();
    	KeywordHelpURLs = new HashMap<String, String>();
    	
    	FontSize = prefs.getInt("FontSize", 12);
    	calculateFont();
    	
    	SchemeDocument.reload();
    	
    	RecentDocumentManager.setFiles(prefs.get("RecentDocuments", null));
    	loadSyntax();
    }
    
    /**
     * Load syntax options from file.
     */
    private static void loadSyntax() {
		try {
	        ClassLoader loader = Options.class.getClassLoader();
			final Scanner s = new Scanner(new File(loader.getResource("lib/syntax.csv").toURI()));
			String line;
			String[] parts;
			while (s.hasNextLine()) {
				line = s.nextLine().trim();
				if ("".equals(line) || line.charAt(0) == ';') continue;
				
				parts = line.split(",");
				if (parts.length == 2) { // old style: keyword,indent
					Keywords.put(parts[0], Integer.parseInt(parts[1]));
				} else if (parts.length == 4) { // new style: keyword,kind,indent,help URL
					Keywords.put(parts[0], Integer.parseInt(parts[2]));
					if (!parts[3].trim().isEmpty())
						KeywordHelpURLs.put(parts[0], parts[3]);
				}
			}
			s.close();
		} catch(FileNotFoundException ex) {
			ErrorManager.logError("Unable to find syntax file.");
			return;
		} catch (URISyntaxException e) {
			ErrorManager.logError("Unable to find syntax file.");
			return;
		}
	}

	/**
     * Determine how wide the font actually is per character.
     */
    private static void calculateFont() {
    	Font = new Font("Monospaced", java.awt.Font.PLAIN, FontSize);
    	
    	Component c = new Component(){ private static final long serialVersionUID = 366311035336037525L; };
    	FontMetrics fm = c.getFontMetrics(Font);
    	FontWidth = fm.charWidth(' ');
    	FontHeight = fm.getHeight();
	}

	/**
     * Save preferences.
     */
    public static void save() {
    	prefs.putInt("Display/Top", DisplayTop);
    	prefs.putInt("Display/Left", DisplayLeft);
    	prefs.putInt("Display/Width", DisplayWidth);
    	prefs.putInt("Display/Height", DisplayHeight);
    	prefs.putBoolean("Display/Toolbar", DisplayToolbar);
    	
    	prefs.put("Command/Run", CommandRun);
    	prefs.put("Command/Format", CommandFormat);
    	
    	prefs.putBoolean("Options/ViewLineNumbers", ViewLineNumbers);
    	prefs.putBoolean("Options/LambdaMode", LambdaMode);
    	prefs.putBoolean("Options/GreekMode", GreekMode);
    	prefs.putBoolean("Options/EmacsKeybindings", EmacsKeybindings);
    	prefs.putBoolean("Options/ConfirmOnRun", ConfirmOnRun);
    	prefs.putBoolean("Options/ConfirmOnClose", ConfirmOnClose);
    	prefs.putBoolean("Options/BackupOnSave", BackupOnSave);
    	
    	for (String key : Colors.keySet())
    		prefs.putInt("Colors/" + key, Colors.get(key).getRGB());
    	
    	prefs.putInt("FontSize", FontSize);
    	
    	prefs.put("RecentDocuments", RecentDocumentManager.getFiles());
    	
    	prefs.put("SavedHistory", MainFrame.getHistory());
    }

	/**
     * Build the options menu (only build once).
     * @return An options menu.
     */
    public static JMenu buildOptionsMenu(final MainFrame main) {
    	if (optionsMenu == null) {
    		optionsMenu = new JMenu("Options");
    		
    		//  Display options
    		JCheckBoxMenuItem showLineNumber = new JCheckBoxMenuItem("Show line numbers", ViewLineNumbers);
    		showLineNumber.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					ViewLineNumbers = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
					SchemeDocument.reload();
					DocumentManager.ReloadAll();
				}
    		});
    		optionsMenu.add(showLineNumber);
    		
    		final JCheckBoxMenuItem lambdaMode = new JCheckBoxMenuItem("\u03BB mode", LambdaMode);
    		final JCheckBoxMenuItem greekMode = new JCheckBoxMenuItem("Greek mode", GreekMode);
    		
    		lambdaMode.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					LambdaMode = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
					if (LambdaMode) {
						GreekMode = false;
						greekMode.setSelected(false);
					}
					SchemeDocument.reload();
					DocumentManager.ReloadAll();
				}
    		});
    		optionsMenu.add(lambdaMode);
    		
    		greekMode.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					GreekMode = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
					if (GreekMode) {
						LambdaMode = false;
						lambdaMode.setSelected(false);
					}
					SchemeDocument.reload();
					DocumentManager.ReloadAll();
				}
    		});
    		optionsMenu.add(greekMode);
    		
    		JCheckBoxMenuItem emacsMode = new JCheckBoxMenuItem("Use emacs keybindings", EmacsKeybindings);
    		emacsMode.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					EmacsKeybindings = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
					if (EmacsKeybindings)
						EmacsKeyBindings.load();
					else
						EmacsKeyBindings.unload();
				}
    		});
    		optionsMenu.add(emacsMode);
    		
    		JCheckBoxMenuItem toolbar = new JCheckBoxMenuItem("Display toolbar", DisplayToolbar);
    		toolbar.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					DisplayToolbar = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
					main.toggleToolbar(DisplayToolbar);
				}
    		});
    		optionsMenu.add(toolbar);
    		
    		optionsMenu.addSeparator();
    		
    		// Confirm options.
    		JCheckBoxMenuItem confirmRun = new JCheckBoxMenuItem("Confirm on Run", ConfirmOnRun);
    		confirmRun.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					ConfirmOnRun = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
				}
    		});
    		optionsMenu.add(confirmRun);
    		
    		JCheckBoxMenuItem confirmClose = new JCheckBoxMenuItem("Confirm on Close", ConfirmOnClose);
    		confirmClose.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					ConfirmOnClose = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
				}
    		});
    		optionsMenu.add(confirmClose);
    		
    		JCheckBoxMenuItem backup = new JCheckBoxMenuItem("Backup on Save", BackupOnSave);
    		backup.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					BackupOnSave = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
				}
    		});
    		optionsMenu.add(backup);
    		
    		optionsMenu.addSeparator();
    		
    		// Display the dialog to add keywords to syntax highlighting / indentation depth. 
//    		JMenuItem resetSyntax = new JMenuItem("Keywords");
//        	resetSyntax.addActionListener(new ActionListener() {
//    			@Override
//    			public void actionPerformed(ActionEvent e) {
//    				SyntaxDialog.show();
//    			}    		
//        	});
//        	optionsMenu.add(resetSyntax);
    		
        	// Special option to reset colors to defaults.
    		JMenu colorMenu = new JMenu("Colors");
    		JMenuItem resetColors = new JMenuItem("Reset colors");
    		resetColors.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Colors.put("default", new Color(0x000000));
	    	    	Colors.put("comment", new Color(0x006600));
	    	    	Colors.put("keyword", new Color(0x000099));
	    	    	Colors.put("string", new Color(0xFF8C00));
	    	    	Colors.put("bracket", new Color(0x00FFFF));
	    	    	Colors.put("invalid-bracket", new Color(0x00FFFF));
	    	    	SchemeDocument.reload();
					DocumentManager.ReloadAll();
				};
    		});
    		colorMenu.add(resetColors);
    		colorMenu.addSeparator();
    		
    		// Special menu for choosing font colors.
    		for (final String key : Colors.keySet()) {
    			String fixed = ("" + key.charAt(0)).toUpperCase() + key.substring(1);
    			final JMenuItem item = new JMenuItem(fixed);
    			item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						Color newColor = JColorChooser.showDialog(item, "Choose color for " + key, Colors.get(key));
						Colors.put(key, newColor);
						SchemeDocument.reload();
						DocumentManager.ReloadAll();
					}
    			});
    			colorMenu.add(item);
    		}
    		optionsMenu.add(colorMenu);
    		
    		// Special menu for font size choices.
    		// 8 to 30 by 2s.
    		final JMenu fontSizeMenu = new JMenu("Font size");
    		for (int i = 8; i <= 30; i += 2) {
    			final int fontSize = i;
    			final JCheckBoxMenuItem item = new JCheckBoxMenuItem("" + fontSize, false);
    			if (fontSize == FontSize)
    				item.setSelected(true);
    			item.addActionListener(new ActionListener() {
    				public void actionPerformed(ActionEvent arg0) {
    					for (int i = 0; i < fontSizeMenu.getItemCount(); i++)
    						((JCheckBoxMenuItem) fontSizeMenu.getItem(i)).setSelected(false);
    					((JCheckBoxMenuItem) arg0.getSource()).setSelected(true);
    					
						FontSize = fontSize;
						SchemeDocument.reload();
						DocumentManager.ReloadAll();
						
						calculateFont();
					}
    			});
    			fontSizeMenu.add(item);
    		}
    		optionsMenu.add(fontSizeMenu);
    	}
    	
    	return optionsMenu;
    }
    
    // Hide this.
    private Options() {};
}
