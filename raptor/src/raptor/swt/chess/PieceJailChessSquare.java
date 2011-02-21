/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.SWTUtils;

/**
 * A labeled chess square. Contains a label in the top right. Currently used for
 * piece jails and drop squares to show the number of pieces.
 */
public class PieceJailChessSquare extends ChessSquare {

	protected PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			e.gc.setAdvanced(true);

			Point size = getSize();
			e.gc.fillRectangle(0, 0, size.x, size.y);

			int imageSide = getImageSize();
			if (pieceImage == null) {
				pieceImage = getChessPieceImage(pieceJailPiece, imageSide);
			}

			int pieceImageX = (size.x - imageSide) / 2;
			int pieceImageY = (size.y - imageSide) / 2;

			if (piece != EMPTY && !isHidingPiece()) {
				e.gc.drawImage(pieceImage, pieceImageX, pieceImageY);

				if (StringUtils.isNotBlank(getText())) {
					e.gc.setForeground(getPreferences().getColor(
							BOARD_PIECE_JAIL_LABEL_COLOR));
					e.gc.setFont(SWTUtils.getProportionalFont(getPreferences()
							.getFont(BOARD_PIECE_JAIL_FONT),
							getPieceJailLabelPercentage(), size.y));

					int width = e.gc.getFontMetrics().getAverageCharWidth()
							* text.length() + 2;

					e.gc.drawString(getText(), size.x - width, 0, true);
				}
			} else {
				e.gc.setAlpha(getPieceJailShadowAlpha());
				e.gc.drawImage(pieceImage, pieceImageX, pieceImageY);
				e.gc.setAlpha(255);
			}
		}
	};

	protected String text = "";

	protected int pieceJailPiece;

	public PieceJailChessSquare(Composite parent, ChessBoard board,
			int pieceJailPiece, int id) {
		super(parent, board, id, true);
		this.pieceJailPiece = pieceJailPiece;
		ignorePaint = true;
		addPaintListener(paintListener);
	}

	/**
	 * Creates a ChessSquare not tied to a board. Useful in preferences. Use
	 * with care, this does'nt add any listeners besides the PaointListener and
	 * board will be null.
	 */
	public PieceJailChessSquare(Composite parent, int id, int pieceJailPiece) {
		super(parent, id, true);
		ignorePaint = true;
		this.pieceJailPiece = pieceJailPiece;
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
		if (!StringUtils.equals(this.text, text)) {
		    this.text = text;
		    isDirty = true;
		}
	}

	protected int getPieceJailLabelPercentage() {
		return Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.BOARD_PIECE_JAIL_LABEL_PERCENTAGE);
	}

	protected int getPieceJailShadowAlpha() {
		return getPreferences().getInt(BOARD_PIECE_JAIL_SHADOW_ALPHA);
	}
}
