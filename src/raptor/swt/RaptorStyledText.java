/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

	Object getMyClipboardContent(final int clipboardType) {
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		return clipBoard.getContents(plainTextTransfer, clipboardType);
	}
}
