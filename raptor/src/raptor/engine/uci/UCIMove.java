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
			char pieceChar = uciString.charAt(6);
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
