/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package raptor.swt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

import raptor.util.RaptorStringTokenizer;

/**
 * Extends StyledText to change the paste behavior of copy and paste to handle
 * ICS text properly. If StyledText is SWT.MULTI then \n\\{spaceSequence} format
 * is removed on copies. and replaced with \n. If StyledText is SWT.SINGLE then
 * \n\r\t are replaced by spaces and then all spaceSequences greater than 1 are
 * replaced by a single space.
 */
public class RaptorStyledText extends StyledText {
	protected Clipboard clipBoard;

	public RaptorStyledText(Composite parent, int style) {
		super(parent, style);
		clipBoard = new Clipboard(getDisplay());
	}

	Object getMyClipboardContent(final int clipboardType) {
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		return clipBoard.getContents(plainTextTransfer, clipboardType);
	}

	@Override
	public void copy() {
		if ((getStyle() & SWT.SINGLE) != 0) {
			super.copy();
		} else {

			String text = getSelectionText();
			if (!StringUtils.isEmpty(text)) {
				StringBuilder builder = new StringBuilder(text);
				int newLineIndex = builder.indexOf("\n\\");

				while (newLineIndex != -1) {
					int cursor = newLineIndex + 2;
					while (Character.isWhitespace(builder.charAt(cursor))) {
						cursor++;
					}
					if (cursor > newLineIndex + 2) {
						builder.delete(newLineIndex, cursor);
					}
					newLineIndex = builder.indexOf("\n\\", newLineIndex + 1);
				}

				clipBoard.setContents(new Object[] { builder.toString() },
						new Transfer[] { TextTransfer.getInstance() },
						DND.CLIPBOARD);
			}

		}
	}

	@Override
	public void paste() {
		if ((getStyle() & SWT.SINGLE) != 0) {
			checkWidget();
			String text = (String) getMyClipboardContent(DND.CLIPBOARD);
			if (text.contains("\n") || text.contains("\r")) {
				if (text != null && text.length() > 0) {
					text = StringUtils.replace(text, "\n", " ");
					text = StringUtils.replace(text, "\r", " ");
					text = StringUtils.replace(text, "\t", " ");

					// Replace multiple spaces with a single space.
					RaptorStringTokenizer tok = new RaptorStringTokenizer(text,
							" ", true);
					StringBuilder result = new StringBuilder(text.length());
					while (tok.hasMoreTokens()) {
						result.append(tok.nextToken() + " ");
					}
					replaceTextRange(getSelection().x, 0, result.toString());
				}
			} else {
				super.paste();
			}
		}
	}
}
