/*
 *  XNap Commons
 *
 *  Copyright (C) 2005  Steffen Pingel
 *  Copyright (C) 2005  Felix Berger
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.xnap.commons.gui.shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;

/***
 * Generic class which activates Emacs keybindings for java input
 * {@link JTextComponent}s.
 * 
 * The inner class actions can also be used independently.
 * 
 * @author Felix Berger
 */
public class EmacsKeyBindings {

	public static final String killLineAction = "emacs-kill-line";

	public static final String killRingSaveAction = "emacs-kill-ring-save";

	public static final String killRegionAction = "emacs-kill-region";

	public static final String backwardKillWordAction = "emacs-backward-kill-word";

	public static final String capitalizeWordAction = "emacs-capitalize-word";

	public static final String downcaseWordAction = "emacs-downcase-word";

	public static final String killWordAction = "emacs-kill-word";

	public static final String setMarkCommandAction = "emacs-set-mark-command";

	public static final String yankAction = "emacs-yank";

	public static final String yankPopAction = "emacs-yank-pop";

	public static final String upcaseWordAction = "emacs-upcase-word";

	public static final JTextComponent.KeyBinding[] EMACS_KEY_BINDINGS = {
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
					InputEvent.CTRL_MASK), DefaultEditorKit.pasteAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_W,
					InputEvent.ALT_MASK), DefaultEditorKit.copyAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_W,
					InputEvent.CTRL_MASK), DefaultEditorKit.cutAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_E,
					InputEvent.CTRL_MASK), DefaultEditorKit.endLineAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_A,
					InputEvent.CTRL_MASK), DefaultEditorKit.beginLineAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_D,
					InputEvent.CTRL_MASK),
					DefaultEditorKit.deleteNextCharAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_N,
					InputEvent.CTRL_MASK), DefaultEditorKit.downAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_P,
					InputEvent.CTRL_MASK), DefaultEditorKit.upAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_B,
					InputEvent.ALT_MASK), DefaultEditorKit.previousWordAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(
					KeyEvent.VK_LESS, InputEvent.ALT_MASK),
					DefaultEditorKit.beginAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(
					KeyEvent.VK_LESS, InputEvent.ALT_MASK
							+ InputEvent.SHIFT_MASK),
					DefaultEditorKit.endAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F,
					InputEvent.ALT_MASK), DefaultEditorKit.nextWordAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F,
					InputEvent.CTRL_MASK), DefaultEditorKit.forwardAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_B,
					InputEvent.CTRL_MASK), DefaultEditorKit.backwardAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V,
					InputEvent.CTRL_MASK), DefaultEditorKit.pageDownAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V,
					InputEvent.ALT_MASK), DefaultEditorKit.pageUpAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_D,
					InputEvent.ALT_MASK), EmacsKeyBindings.killWordAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(
					KeyEvent.VK_BACK_SPACE, InputEvent.ALT_MASK),
					EmacsKeyBindings.backwardKillWordAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(
					KeyEvent.VK_SPACE, InputEvent.CTRL_MASK),
					EmacsKeyBindings.setMarkCommandAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_W,
					InputEvent.ALT_MASK), EmacsKeyBindings.killRingSaveAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_W,
					InputEvent.CTRL_MASK), EmacsKeyBindings.killRegionAction),

			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_K,
					InputEvent.CTRL_MASK), EmacsKeyBindings.killLineAction),

			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
					InputEvent.CTRL_MASK), EmacsKeyBindings.yankAction),

			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
					InputEvent.ALT_MASK), EmacsKeyBindings.yankPopAction),

			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_C,
					InputEvent.ALT_MASK), EmacsKeyBindings.capitalizeWordAction),

			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_L,
					InputEvent.ALT_MASK), EmacsKeyBindings.downcaseWordAction),

			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_U,
					InputEvent.ALT_MASK), EmacsKeyBindings.upcaseWordAction), };

	/***
	 * Loads the emacs keybindings for all common <code>JTextComponent</code>s.
	 * 
	 * The shared keymap instances of the concrete subclasses of
	 * {@link JTextComponent} are fed with the keybindings.
	 * 
	 * The original keybindings are stored in a backup array.
	 */
	public static void load() {
		JTextComponent[] jtcs = new JTextComponent[] { new JTextArea(),
				new JTextPane(), new JTextField(), new JEditorPane(), };

		for (int i = 0; i < jtcs.length; i++) {
			Keymap orig = jtcs[i].getKeymap();
			Keymap backup = JTextComponent.addKeymap(jtcs[i].getClass()
					.getName(), null);

			Action[] bound = orig.getBoundActions();
			for (int j = 0; j < bound.length; j++) {
				KeyStroke[] strokes = orig.getKeyStrokesForAction(bound[j]);
				for (int k = 0; k < strokes.length; k++) {
					backup.addActionForKeyStroke(strokes[k], bound[j]);
				}
			}

			backup.setDefaultAction(orig.getDefaultAction());
		}

		loadEmacsKeyBindings();
	}

	/***
	 * Restores the original keybindings for the concrete subclasses of
	 * {@link JTextComponent}.
	 * 
	 */
	public static void unload() {
		JTextComponent[] jtcs = new JTextComponent[] { new JTextArea(),
				new JTextPane(), new JTextField(), new JEditorPane(), };

		for (int i = 0; i < jtcs.length; i++) {
			Keymap backup = JTextComponent.getKeymap(jtcs[i].getClass()
					.getName());

			if (backup != null) {
				Keymap current = jtcs[i].getKeymap();
				current.removeBindings();

				Action[] bound = backup.getBoundActions();
				for (int j = 0; j < bound.length; j++) {
					KeyStroke[] strokes = backup
							.getKeyStrokesForAction(bound[i]);
					for (int k = 0; k < strokes.length; k++) {
						current.addActionForKeyStroke(strokes[k], bound[j]);
					}
				}
				current.setDefaultAction(backup.getDefaultAction());
			}
		}
	}

	/***
	 * Activates Emacs keybindings for all text components extending
	 * {@link JTextComponent}.
	 */
	private static void loadEmacsKeyBindings() {
		JTextComponent[] jtcs = new JTextComponent[] { new JTextArea(),
				new JTextPane(), new JTextField(), new JEditorPane(), };

		for (int i = 0; i < jtcs.length; i++) {

			Keymap k = jtcs[i].getKeymap();

			JTextComponent.loadKeymap(k, EMACS_KEY_BINDINGS,
					jtcs[i].getActions());

			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK),
					new KillWordAction(killWordAction));
			k.addActionForKeyStroke(KeyStroke.getKeyStroke(
					KeyEvent.VK_BACK_SPACE, InputEvent.ALT_MASK),
					new BackwardKillWordAction(backwardKillWordAction));
			k.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
					InputEvent.CTRL_MASK), new SetMarkCommandAction(
					setMarkCommandAction));
			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.ALT_MASK),
					new KillRingSaveAction(killRingSaveAction));
			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK),
					new KillRegionAction(killRegionAction));
			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK),
					new KillLineAction(killLineAction));
			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK),
					new YankAction("emacs-yank"));
			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.ALT_MASK),
					new YankPopAction("emacs-yank-pop"));
			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK),
					new CapitalizeWordAction(capitalizeWordAction));

			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_MASK),
					new DowncaseWordAction(downcaseWordAction));

			k.addActionForKeyStroke(
					KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
					new UpcaseWordAction(upcaseWordAction));
		}
	}

	/***
	 * This action kills the next word.
	 * 
	 * It removes the next word on the right side of the cursor from the active
	 * text component and adds it to the clipboard.
	 */
	public static class KillWordAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public KillWordAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent jtc = getTextComponent(e);
			if (jtc != null) {
				try {
					int offs = jtc.getCaretPosition();
					jtc.setSelectionStart(offs);
					offs = getWordEnd(jtc, offs);
					jtc.setSelectionEnd(offs);
					KillRing.getInstance().add(jtc.getSelectedText());
					jtc.cut();
				} catch (BadLocationException ble) {
					jtc.getToolkit().beep();
				}
			}
		}
	}

	/***
	 * This action kills the previous word.
	 * 
	 * It removes the previous word on the left side of the cursor from the
	 * active text component and adds it to the clipboard.
	 */
	public static class BackwardKillWordAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public BackwardKillWordAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent jtc = getTextComponent(e);
			if (jtc != null) {
				try {
					int offs = jtc.getCaretPosition();
					jtc.setSelectionEnd(offs);
					offs = Utilities.getPreviousWord(jtc, offs);
					jtc.setSelectionStart(offs);
					KillRing.getInstance().add(jtc.getSelectedText());
					jtc.cut();
				} catch (BadLocationException ble) {
					jtc.getToolkit().beep();
				}
			}
		}
	}

	/***
	 * This action copies the marked region and stores it in the killring.
	 */
	public static class KillRingSaveAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public KillRingSaveAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent jtc = getTextComponent(e);
			if (jtc != null && SetMarkCommandAction.isMarked(jtc)) {
				jtc.setSelectionStart(SetMarkCommandAction.getCaretPosition());
				jtc.moveCaretPosition(jtc.getCaretPosition());
				jtc.copy();
				// TODO is this correct?
				KillRing.getInstance().add(jtc.getSelectedText());
				SetMarkCommandAction.reset();
				// todo reset selection
			}
		}
	}

	/***
	 * This action Kills the marked region and stores it in the killring.
	 */
	public static class KillRegionAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public KillRegionAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent jtc = getTextComponent(e);
			if (jtc != null && SetMarkCommandAction.isMarked(jtc)) {
				int start, end;
				if (SetMarkCommandAction.getCaretPosition() < jtc
						.getCaretPosition()) {
					start = SetMarkCommandAction.getCaretPosition();
					end = jtc.getCaretPosition();
				} else {
					start = jtc.getCaretPosition();
					end = SetMarkCommandAction.getCaretPosition();
				}
				if (start != end) {
					jtc.select(start, end);
					SetMarkCommandAction.reset();
					KillRing.getInstance().add(jtc.getSelectedText());
					jtc.cut();
				} else {
					jtc.getToolkit().beep();
				}
			}
		}
	}

	/***
	 * This actin kills text up to the end of the current line and stores it in
	 * the killring.
	 */
	public static class KillLineAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public KillLineAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent jtc = getTextComponent(e);
			if (jtc != null) {
				try {
					int start = jtc.getCaretPosition();
					int end = Utilities.getRowEnd(jtc, start);
					if (start == end && jtc.isEditable()) {
						Document doc = jtc.getDocument();
						doc.remove(end, 1);
					} else {
						jtc.setSelectionStart(start);
						jtc.setSelectionEnd(end);
						KillRing.getInstance().add(jtc.getSelectedText());
						jtc.cut();
						// jtc.replaceSelection("");
					}
				} catch (BadLocationException ble) {
					jtc.getToolkit().beep();
				}
			}
		}
	}

	/***
	 * This action sets a beginning mark for a selection.
	 */
	public static class SetMarkCommandAction extends TextAction {
		private static final long serialVersionUID = 1L;

		private static int position = -1;
		private static JTextComponent jtc;

		public SetMarkCommandAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent e) {
			jtc = getTextComponent(e);
			if (jtc != null) {
				position = jtc.getCaretPosition();
			}
		}

		public static boolean isMarked(JTextComponent jt) {
			return (jtc == jt && position != -1);
		}

		public static void reset() {
			jtc = null;
			position = -1;
		}

		public static int getCaretPosition() {
			return position;
		}
	}

	/***
	 * This action pastes text from the killring.
	 */
	public static class YankAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public static int start = -1;
		public static int end = -1;

		public YankAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent event) {
			JTextComponent jtc = getTextComponent(event);

			if (jtc != null) {
				try {
					start = jtc.getCaretPosition();
					jtc.paste();
					end = jtc.getCaretPosition();
					KillRing.getInstance().add(jtc.getText(start, end));
					KillRing.getInstance().setCurrentTextComponent(jtc);
				} catch (Exception e) {
				}
			}
		}
	}

	/***
	 * This action pastes an element from the killring cycling through it.
	 */
	public static class YankPopAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public YankPopAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent event) {
			JTextComponent jtc = getTextComponent(event);

			if (jtc != null
					&& KillRing.getInstance().getCurrentTextComponent() == jtc
					&& jtc.getCaretPosition() == YankAction.end
					&& !KillRing.getInstance().isEmpty()) {
				jtc.setSelectionStart(YankAction.start);
				jtc.setSelectionEnd(YankAction.end);
				String toYank = KillRing.getInstance().next();
				if (toYank != null) {
					jtc.replaceSelection(toYank);
					YankAction.end = jtc.getCaretPosition();
				} else {
					jtc.getToolkit().beep();
				}
			}
		}
	}

	/***
	 * Manages all killed (cut) text pieces in a ring which is accessible
	 * through {@link YankPopAction}.
	 * <p>
	 * Also provides an unmodifiable copy of all cut pieces.
	 */
	public static class KillRing {
		private JTextComponent jtc;
		private LinkedList<String> ring = new LinkedList<String>();
		Iterator<String> iter = ring.iterator();

		private static final KillRing instance = new KillRing();

		public static KillRing getInstance() {
			return instance;
		}

		void setCurrentTextComponent(JTextComponent jtc) {
			this.jtc = jtc;
		}

		JTextComponent getCurrentTextComponent() {
			return jtc;
		}

		/***
		 * Adds text to the front of the kill ring.
		 * <p>
		 * Deviating from the Emacs implementation we make sure the exact same
		 * text is not somewhere else in the ring.
		 */
		void add(String text) {
			if (text.length() == 0) {
				return;
			}

			ring.remove(text);
			ring.addFirst(text);
			while (ring.size() > 60) {
				ring.removeLast();
			}
			iter = ring.iterator();
			// skip first entry, the one we just added
			iter.next();
		}

		/***
		 * Returns an unmodifiable version of the ring list which contains the
		 * killed texts.
		 * 
		 * @return the content of the kill ring
		 */
		public List<String> getRing() {
			return Collections.unmodifiableList(ring);
		}

		public boolean isEmpty() {
			return ring.isEmpty();
		}

		/***
		 * Returns the next text element which is to be yank-popped.
		 * 
		 * @return <code>null</code> if the ring is empty
		 */
		String next() {
			if (ring.isEmpty()) {
				return null;
			} else if (iter.hasNext()) {
				return iter.next();
			} else {
				iter = ring.iterator();
				// guaranteed to not throw an exception, since ring is not empty
				return iter.next();
			}
		}
	}

	/***
	 * This action capitalizes the next word on the right side of the caret.
	 */
	public static class CapitalizeWordAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public CapitalizeWordAction(String nm) {
			super(nm);
		}

		/***
		 * At first the same code as in
		 * {@link EmacsKeyBindings.DowncaseWordAction} is performed, to ensure
		 * the word is in lower case, then the first letter is capialized.
		 */
		public void actionPerformed(ActionEvent event) {
			JTextComponent jtc = getTextComponent(event);

			if (jtc != null) {
				try {
					/* downcase code */
					int start = jtc.getCaretPosition();
					int end = getWordEnd(jtc, start);
					jtc.setSelectionStart(start);
					jtc.setSelectionEnd(end);
					String word = jtc.getText(start, end - start);
					jtc.replaceSelection(word.toLowerCase());

					/* actual capitalize code */
					int offs = Utilities.getWordStart(jtc, start);
					// get first letter
					String c = jtc.getText(offs, 1);
					// we're at the end of the previous word
					if (c.equals(" ")) {
						/*
						 * ugly java workaround to get the beginning of the
						 * word.
						 */
						offs = Utilities.getWordStart(jtc, ++offs);
						c = jtc.getText(offs, 1);
					}
					if (Character.isLetter(c.charAt(0))) {
						jtc.setSelectionStart(offs);
						jtc.setSelectionEnd(offs + 1);
						jtc.replaceSelection(c.toUpperCase());
					}
					end = Utilities.getWordEnd(jtc, offs);
					jtc.setCaretPosition(end);
				} catch (BadLocationException ble) {
					jtc.getToolkit().beep();
				}
			}
		}
	}

	/***
	 * This action renders all characters of the next word to lowercase.
	 */
	public static class DowncaseWordAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public DowncaseWordAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent event) {
			JTextComponent jtc = getTextComponent(event);

			if (jtc != null) {
				try {
					int start = jtc.getCaretPosition();
					int end = getWordEnd(jtc, start);
					jtc.setSelectionStart(start);
					jtc.setSelectionEnd(end);
					String word = jtc.getText(start, end - start);
					jtc.replaceSelection(word.toLowerCase());
					jtc.setCaretPosition(end);
				} catch (BadLocationException ble) {
					jtc.getToolkit().beep();
				}
			}
		}
	}

	/***
	 * This action renders all characters of the next word to upppercase.
	 */
	public static class UpcaseWordAction extends TextAction {
		private static final long serialVersionUID = 1L;

		public UpcaseWordAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent event) {
			JTextComponent jtc = getTextComponent(event);

			if (jtc != null) {
				try {
					int start = jtc.getCaretPosition();
					int end = getWordEnd(jtc, start);
					jtc.setSelectionStart(start);
					jtc.setSelectionEnd(end);
					String word = jtc.getText(start, end - start);
					jtc.replaceSelection(word.toUpperCase());
					jtc.setCaretPosition(end);
				} catch (BadLocationException ble) {
					jtc.getToolkit().beep();
				}
			}
		}
	}

	private static int getWordEnd(JTextComponent jtc, int start)
			throws BadLocationException {
		try {
			return Utilities.getNextWord(jtc, start);
		} catch (BadLocationException ble) {
			int end = jtc.getText().length();
			if (start < end) {
				return end;
			} else {
				throw ble;
			}
		}
	}
}