package gui;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.io.File;
import java.util.Stack;

/**
 * Text area specialized for Scheme (Woo!)
 */
public class SchemeTextArea extends JPanel {
	private static final long serialVersionUID = -5290625425897085428L;

	File myFile;
    JScrollPane pane;
    net.infonode.docking.View myView;
    boolean dirty;

    public JEditorPane code;
    public static String NL = "\n"; //System.getProperty("line.separator");
    
    
    /**
     * Create a new Scheme text area.
     */
    public SchemeTextArea() {
        super();
        setLayout(new BorderLayout());

        code = new JEditorPane();
        pane = new JScrollPane(code);
        add(pane);

        final SchemeDocument doc = new SchemeDocument();
        final StyledEditorKit sek = new StyledEditorKit() {
			private static final long serialVersionUID = 8558935103754214456L;

			public Document createDefaultDocument() {
                return doc;
            }
        };

        code.setEditorKitForContentType("text/scheme", sek);
        code.setContentType("text/scheme");
        code.setEditorKit(sek);
        code.setDocument(doc);

        code.getInputMap().put(KeyStroke.getKeyStroke("TAB"), new actions.Tab());
        code.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), new actions.Return());
        
        for (String name : new String[]{"New", "Open", "Save", "Save as", "Close", "Exit", "Run", "Format"}) {
        	JMenuItem item = MenuManager.itemForName(name);
        	code.getInputMap().put(item.getAccelerator(), item.getAction());
        }
        
        // Bracket highlighting.
        code.addCaretListener(new BracketMatcher(this));
    }

    /**
     * Create a new Scheme text area with content.
     *
     * @param text Content.
     */
    public SchemeTextArea(String text) {
        this();
        setText(text);
    }

    /**
     * Perform a tab at the current position.
     */
    public void tab() {
        // Things that break tokens.
        String delimiters = "()[] ";

        // Get the text and current position.
        String text = getText();
        int pos = code.getCaretPosition();
        int len = text.length();

        // Variables we are trying to determine.
        int indentNow = 0;
        int indentTo = 0;
        int insertAt = 0;
        int tokenStart = 0;
        int tokenEnd = 0;

        // Get the start of this line.
        int lineStart = text.lastIndexOf(NL, pos);
        insertAt = (lineStart < 0 ? 0 : lineStart + NL.length());

        // Get the indentation on the current line.
        for (int i = Math.max(0, lineStart + NL.length()); i < len && text.charAt(i) == ' '; i++)
            indentNow++;

        // If we're on the first line, don't indent.
        if (lineStart == -1)
            indentTo = 0;

            // Otherwise, figure out how far we want to indent.
        else {
            // Scan upwards until we find the first unmatched opening bracket.
            boolean unmatched = false;
            Stack<Character> brackets = new Stack<Character>();
            for (int i = lineStart; i >= 0; i--) {
                char c = text.charAt(i);
                if (i > 0) {
                    char cp = (i > 0 ? text.charAt(i - 1) : '\0');

                    if ((delimiters.indexOf(cp) != -1) && !(delimiters.indexOf(c) != -1))
                        tokenStart = i;

                    if (!(delimiters.indexOf(cp) != -1) && (delimiters.indexOf(c) != -1))
                        tokenEnd = i;
                }

                if (c == ')') brackets.push('(');
                if (c == ']') brackets.push('[');

                if (c == '(' || c == '[') {
                    if (brackets.isEmpty() || brackets.peek() != c) {
                        int thatLine = text.lastIndexOf(NL, i);
                        if (thatLine < 0)
                            thatLine = 0;
                        else
                            thatLine = thatLine + NL.length();

                        indentTo = i - thatLine;
                        unmatched = true;
                        break;
                    } else {
                        brackets.pop();
                    }
                }
            }

            // Get the token.
            String token = null;
            try {
                token = text.substring(tokenStart, tokenEnd);
            } catch (StringIndexOutOfBoundsException sioobe) {

            }

            // If there aren't any unmatched brackets, start a line.
            if (!unmatched)
                indentTo = 0;

                // Otherwise, if there's a valid keyword, indent based on that.
            else if (Options.Keywords.containsKey(token))
                indentTo += Options.Keywords.get(token);

                // Otherwise, fall back on the default indentation.
            else
                indentTo += 2;
        }

        // Add new indentation if we need to.
        if (indentNow < indentTo) {
            String toInsert = "";
            for (int i = indentNow; i < indentTo; i++)
                toInsert += " ";

            setText(text.substring(0, insertAt) + toInsert + text.substring(insertAt));
            code.setCaretPosition(pos + (indentTo - indentNow));
        }

        // Or remove it, if we need to.
        else if (indentNow > indentTo) {
            setText(text.substring(0, insertAt) + text.substring(insertAt + (indentNow - indentTo)));
            code.setCaretPosition(pos - (indentNow - indentTo));
        }
    }

    /**
     * Format the document.
     */
    public void format() {
        int next = -1;
        while (true) {
            next = getText().indexOf(NL, next + 1) + NL.length();

            if (next > 0 && next < getText().length()) {
                try {
                    code.setCaretPosition(next);
                    tab();
                } catch (IllegalArgumentException iae) {
                    // Means there's extra lines. Just ignore it.
                }
            } else
                break;
        }
    }

    /**
     * Is the text area empty?
     *
     * @return True/false
     */
    public boolean isEmpty() {
        return getText().length() == 0;
    }

    /**
     * Set the file that this code is associated with.
     *
     * @param f The file.
     */
    public void setFile(File f) {
        myFile = f;
    }

    /**
     * Get the file that this code is associated with (might be null).
     *
     * @return The file.
     */
    public File getFile() {
        return myFile;
    }

    /**
     * Get the code.
     *
     * @return The code.
     */
    public String getText() {
        return code.getText();
    }

    /**
     * Set the code.
     */
    public void setText(String text) {
        code.setText(text);
    }

    /**
     * Append text to the end of the code area.
     *
     * @param text Text to append.
     */
    public void append(String text) {
        setText(getText() + text);
    }
}
