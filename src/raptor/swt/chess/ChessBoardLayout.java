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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public abstract class ChessBoardLayout extends Layout implements BoardConstants {
	public static enum Field {
		CLOCK_LABEL, CURRENT_PREMOVE_LABEL, GAME_DESCRIPTION_LABEL, LAG_LABEL, NAME_RATING_LABEL, OPENING_DESCRIPTION_LABEL, STATUS_LABEL, TO_MOVE_INDICATOR;
	}

	protected ChessBoard board;

	public ChessBoardLayout(ChessBoard board) {
		super();
		this.board = board;
	}

	@Override
	protected Point computeSize(Composite composite, int hint, int hint2,
			boolean flushCache) {
		return composite.getSize();
	}

	public void dispose() {
		board = null;
	}

	public ChessBoard getBoard() {
		return board;
	}

	public abstract String getName();

	public abstract int getStyle(Field field);

	protected void layoutChessBoard(Point topLeft, int squareSideSize) {
		int x = topLeft.x;
		int y = topLeft.y;

		if (!board.isWhiteOnTop()) {
			for (int i = 7; i > -1; i--) {
				for (int j = 0; j < 8; j++) {
					ChessSquare square = board.getSquare(i, j);
					square.setBounds(x, y, squareSideSize, squareSideSize);
					x += squareSideSize;
				}
				x = topLeft.x;
				y += squareSideSize;

			}
		} else {
			for (int i = 0; i < 8; i++) {
				for (int j = 7; j > -1; j--) {
					ChessSquare square = board.getSquare(i, j);
					square.setBounds(x, y, squareSideSize, squareSideSize);
					x += squareSideSize;
				}
				x = topLeft.x;
				y += squareSideSize;
			}
		}
	}
}
