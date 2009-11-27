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
package raptor.engine.uci;

import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.util.GameUtils;

public class UCIMove {
	protected String value;
	protected int startSquare;
	protected int endSquare;
	protected int promotedPiece = 0;

	public UCIMove(Move move) {
		startSquare = move.getFrom();
		endSquare = move.getEpSquare();
		if (move.isPromotion()) {
			promotedPiece = move.getPiecePromotedTo();
			value = GameUtils.getSan(startSquare) + GameUtils.getSan(endSquare)
					+ "=" + GameConstants.PIECE_TO_SAN.charAt(promotedPiece);
			;
		} else {
			value = GameUtils.getSan(startSquare) + GameUtils.getSan(endSquare);
		}
	}

	public UCIMove(String uciString) {
		value = uciString;
		startSquare = GameUtils.getSquare(uciString.substring(0, 2));
		endSquare = GameUtils.getSquare(uciString.substring(2, 4));
		if (uciString.length() > 4) {
			char pieceChar = uciString.charAt(4);
			promotedPiece = GameConstants.PIECE_TO_SAN.indexOf(pieceChar);
		}
	}

	/**
	 * Returns the end square constant in GameConstants representing the to
	 * square for the move.
	 */
	public int getEndSquare() {
		return endSquare;
	}

	/**
	 * Returns the piece constant in GameConstants representing the promoted
	 * piece.
	 */
	public int getPromotedPiece() {
		return promotedPiece;
	}

	/**
	 * Returns the start square constant in GameConstants representing the from
	 * square for the move.
	 */
	public int getStartSquare() {
		return startSquare;
	}

	public String getValue() {
		return value;
	}

	public boolean isPromotion() {
		return promotedPiece != 0;
	}

	public void setEndSquare(int endSquare) {
		this.endSquare = endSquare;
	}

	public void setStartSquare(int startSquare) {
		this.startSquare = startSquare;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

}
