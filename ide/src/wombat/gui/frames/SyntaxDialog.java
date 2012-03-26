/* 
 * License: source-license.txt
 * If this code is used independently, copy the license here.
 */

package wombat.gui.frames;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import wombat.util.Options;

/**
 * Options for syntax, both syntax highlighting for keywords and indent level.
 */
public final class SyntaxDialog {
	/*
	 * Syntax to use if nothing else has been set.
	 * TODO: Load this from a file. 
	 */
	public final static String DEFAULT_SYNTAX =
		"define		2 \n" +
		"lambda		2 \n" +
		"if			4 \n" +
		"cond		2 \n" +
		"and		5 \n" +
		"or			4 \n" +
		"+			3 \n" +
		"-			3 \n" +
		"*			3 \n" +
		"/			3 \n" +
		"add1		6 \n" +
		"sub1		6 \n" +
		"list		6 \n" +
		"cons		6 \n" +
		"car		5 \n" +
		"cdr		5 \n" +
		"let		2 \n" +
		"let*		2 \n" +
		"letrec		2 \n" +
		"quote		2 \n" +
		"case		2 \n" +
		"do			2 \n" +
		"else		1 \n" +
		"trace-define	\n" + 
		"color\ncolor?\ncolor-equal?\ncolor-ref\n" +
		"image?\nimage-equal?\nimage-rows\nimage-cols\nimage-ref\nimage-set!\nread-image\nwrite-image\ndraw-image\nimage-map\nmake-image\ndraw-image-file\n" +
		"tree\ntree?\nleaf\nleaf?\nempty-tree\nempty-tree?\nleft-subtree\nright-subtree\nroot-value\ndraw-tree";
	
	static JDialog dialog;
	static List<String> syntaxNames = new ArrayList<String>();
	
	/**
	 * Create a new syntax frame to add new keywords and set their indentation.
	 */
	private SyntaxDialog() {
		dialog = new JDialog();
		dialog.setTitle("Syntax options");
		dialog.setSize(400, 400);
		dialog.setLayout(new BorderLayout());
		dialog.setLocationByPlatform(true);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		JTable table = new JTable(new AbstractTableModel() {
			private static final long serialVersionUID = -8298784246564975586L;

			/**
			 * gets the value at the specific row and col
			 * @param row the row of the desired value
			 * @param col the col of the desired value
			 * @return value
			 */
			public Object getValueAt(int row, int col) {
				if (row == syntaxNames.size())
					return null;
				else {
					if (col == 0) {
						return syntaxNames.get(row);
					} else {
						String name = syntaxNames.get(row);
						if (Options.Keywords.containsKey(name))
							return Options.Keywords.get(name);
						else
							return 2;
					}
				}
			}
			
			/**
			 * Set either a keyword name or it's indentation.
			 * @param val The keyword or indentation.
			 * @param row The specific keyword
			 * @param col 0 for the keyword, 1 for the indentation.
			 */
			public void setValueAt(Object val, int row, int col) {
				String sval = (String) val;
				
				// Last item, new keyword.
				if (row == syntaxNames.size()) {
					if (syntaxNames.contains(sval))
						JOptionPane.showConfirmDialog(dialog, "Duplicated keyword: " + sval, "Duplicated keyword", JOptionPane.OK_CANCEL_OPTION);
					else {
						syntaxNames.add(sval);
						Options.Keywords.put(sval, 2);
					}
				}
				
				// Previously defined item.
				else {
					// Keyword name.
					if (col == 0) {
						syntaxNames.remove(row);
						syntaxNames.add(row, sval);
						
						Options.Keywords.put(sval, Options.Keywords.get(sval));
						Options.Keywords.remove(sval);
					}
					
					// Indentation of a defined keyword.
					else {
						try {
							Options.Keywords.put(syntaxNames.get(row), Integer.parseInt(sval));
						} catch (NumberFormatException nfe) {
							JOptionPane.showConfirmDialog(dialog, "Invalid number format: " + sval, "Invalid number format", JOptionPane.OK_CANCEL_OPTION);
						}
					}
				}
			}
			
			/**
			 * Get the number of rows.
			 * @return Number of keywords + 1 for expansion.
			 */
			public int getRowCount() { return syntaxNames.size() + 1; }
			
			/**
			 * Always two columns.
			 * @return 2
			 */
			@Override
			public int getColumnCount() { return 2; }
			
			/**
			 * Names of the columens.
			 * @paran col Which column we want.
			 * @return String "Keyword" or "Indentation"
			 */
			@Override
			public String getColumnName(int col) { return (col == 0 ? "Keyword" : "Indentation"); }
			
			/**
			 * Can we edit the cell?
			 * @return Yes.
			 */
			@Override
			public boolean isCellEditable(int row, int col) { return true; }
		});
		dialog.add(new JScrollPane(table));
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2));
		
		// Reset all keywords to their defaults.
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSyntax(null);
				dialog.setVisible(false);
			}
		});
		buttons.add(resetButton);
		
		// Close and save the syntax items.
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		buttons.add(closeButton);
		
		dialog.add(buttons, BorderLayout.SOUTH);
	}
	
	/**
	 * Show the dialog.
	 */
	public static void show() {
		Collections.sort(syntaxNames);
		if (dialog == null) new SyntaxDialog();
		dialog.setVisible(true);
	}
	
	/**
	 * Get an encoded string of all of the syntax items.
	 * @return Syntax.
	 */
	public static String getSyntax() {
		Collections.sort(syntaxNames);
		StringBuilder sb = new StringBuilder();
		
		for (String key : syntaxNames) {
			sb.append(key);
			sb.append("\t");
			sb.append(Options.Keywords.get(key));
			sb.append("\n");
		}
		sb.delete(sb.length() - 1, sb.length());
		
		return sb.toString();
	}
	
	/**
	 * Load syntax from an encoded string.
	 * @param val Encoded string.
	 */
	public static void setSyntax(String val) {
		syntaxNames.clear();
		
		if (val == null)
			val = DEFAULT_SYNTAX; 
		
		val = val.replaceAll("\t+", "\t");
		for (String line : val.split("\\s*\n\\s*")) {
			String[] pair = line.split("\t");

			String name = null;
			int indent = 2;
			
			if (pair.length == 1) {
				name = pair[0].trim();
				indent = 2;
			} else if (pair.length == 2) {
				name = pair[0].trim();
				try { indent = Integer.parseInt(pair[1].trim()); } catch(NumberFormatException nfe) { }		
			} else {
				continue;
			}
			
			syntaxNames.add(name);
			Options.Keywords.put(name, indent);
		}
		
		Collections.sort(syntaxNames);
	}
}