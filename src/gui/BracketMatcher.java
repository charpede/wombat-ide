package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.event.CaretListener;

import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.StyleConstants;

import util.errors.ErrorManager;

public class BracketMatcher implements CaretListener {
	SchemeTextArea textArea;
	static boolean disabled = false;
	List<Object> activeTags = new ArrayList<Object>();
	
	public BracketMatcher(SchemeTextArea text) {
		textArea = text;
	}
	
	public void caretUpdate(CaretEvent e) {
		if (disabled)
			return;
		
		try {
			Highlighter h = textArea.code.getHighlighter();
			for (Object tag : activeTags)
				h.removeHighlight(tag);
			activeTags.clear();

			int pos = e.getDot() - 1;
        
            if (pos >= 0 && pos < textArea.getText().length() && "()[]".contains(textArea.code.getDocument().getText(pos, 1))) {
                // Find the matching bracket.
                String text = textArea.code.getDocument().getText(0, textArea.code.getDocument().getLength());
                char c = text.charAt(pos), cc;
                int matchPos, d = ((c == '(' || c == '[') ? 1 : -1);
                Stack<Character> brackets = new Stack<Character>();
                boolean foundMatch = false;
                for (matchPos = pos; matchPos >= 0 && matchPos < text.length(); matchPos += d) {
                    cc = text.charAt(matchPos);

                    if (!brackets.isEmpty() && brackets.peek() == cc) {
                        brackets.pop();
                        if (brackets.isEmpty()) {
                            foundMatch = true;
                            break;
                        }
                    } else if (cc == '(') brackets.push(')');
                    else if (cc == ')') brackets.push('(');
                    else if (cc == '[') brackets.push(']');
                    else if (cc == ']') brackets.push('[');
                }

                // Highlight it.
                if (foundMatch) {
                    Highlighter.HighlightPainter hp = new DefaultHighlighter.DefaultHighlightPainter(
                            StyleConstants.getForeground(SchemeDocument.attributes.get("bracket"))
                    );

                    try {
                        activeTags.add(h.addHighlight(pos, pos + 1, hp));
                        activeTags.add(h.addHighlight(matchPos, matchPos + 1, hp));
                    } catch (BadLocationException ble) {
                    }
                }
            }
        } catch (BadLocationException ex) {
			
        } catch (Exception ex) {
        	ErrorManager.logError("Unable to match paranthesis: " + ex.getMessage());
        	disabled = true;
        }
    }

}
