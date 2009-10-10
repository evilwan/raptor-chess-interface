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
package raptor.swt.chess;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import raptor.swt.SWTUtils;

/**
 * A labeled chess square. Contains a label in the top right. Currently used for
 * piece jails and drop squares to show the number of pieces.
 */
public class LabeledChessSquare extends ChessSquare {
	public static final int LABEL_HEIGHT_PERCENTAGE = 30;

	protected String text = "";
	protected PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent arg0) {
			if (StringUtils.isNotBlank(getText())) {
				Point size = getSize();
				arg0.gc.setForeground(getPreferences().getColor(
						BOARD_PIECE_JAIL_LABEL_COLOR));
				arg0.gc.setFont(SWTUtils.getProportionalFont(getPreferences()
						.getFont(BOARD_PIECE_JAIL_FONT),
						LABEL_HEIGHT_PERCENTAGE, size.y));

				int width = arg0.gc.getFontMetrics().getAverageCharWidth()
						* text.length() + 2;

				arg0.gc.drawString(getText(), size.x - width, 0, true);
			}
		}
	};

	public LabeledChessSquare(Composite parent, ChessBoard board, int id) {
		super(parent, board, id, true);
		ignoreBackgroundImage = true;
		addPaintListener(paintListener);
	}

	/**
	 * Returns the current label.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the label.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}
}
