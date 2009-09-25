package raptor.game;

import raptor.game.util.GameUtils;

public class Move implements GameConstants {
	public static final int KINGSIDE_CASTLING_CHARACTERISTIC = 1;
	public static final int QUEENSIDE_CASTLING_CHARACTERISTIC = 2;
	public static final int DOUBLE_PAWN_PUSH_CHARACTERISTIC = 4;
	public static final int PROMOTION_CHARACTERISTIC = 8;
	public static final int EN_PASSANT_CHARACTERISTIC = 16;
	public static final int DROP_CHARACTERISTIC = 32;

	// Bytes are used because they take up less space than ints and there is no
	// need for the extra space.
	protected byte from = EMPTY_SQUARE;
	protected byte to = EMPTY_SQUARE;
	protected byte piece;
	protected byte capture;
	protected byte piecePromotedTo;
	protected byte color;
	protected byte epSquare = EMPTY_SQUARE;
	protected byte castlingType;
	protected byte moveCharacteristic;
	protected byte lastCastleState;
	protected byte previous50MoveCount;

	protected String san;

	/**
	 * Constructor for drop moves.
	 */
	public Move(int to, int piece) {
		this.to = (byte) to;
		this.piece = (byte) piece;
		this.moveCharacteristic = DROP_CHARACTERISTIC;
	}

	public Move(int from, int to, int piece, int color, int capture) {
		super();

		this.piece = (byte) piece;
		this.color = (byte) color;
		this.capture = (byte) capture;
		this.from = (byte) from;
		this.to = (byte) to;
	}

	public Move(int from, int to, int piece, int color, int capture,
			int moveCharacteristic) {
		super();
		this.piece = (byte) piece;
		this.color = (byte) color;
		this.capture = (byte) capture;
		this.from = (byte) from;
		this.to = (byte) to;
		this.moveCharacteristic = (byte) moveCharacteristic;
	}

	public Move(int from, int to, int piece, int color, int capture,
			int piecePromotedTo, int epSquare, int moveCharacteristic) {
		super();
		this.piece = (byte) piece;
		this.color = (byte) color;
		this.capture = (byte) capture;
		this.from = (byte) from;
		this.to = (byte) to;
		this.piecePromotedTo = (byte) piecePromotedTo;
		this.epSquare = (byte) epSquare;
		this.moveCharacteristic = (byte) moveCharacteristic;
	}

	/**
	 * Returns the capture without the promote mask.
	 */
	public int getCapture() {
		return capture & NOT_PROMOTED_MASK;
	}

	public int getCaptureColor() {
		return GameUtils.getOppositeColor(getColor());
	}

	/**
	 * Returns the capture with the promote mask.
	 */
	public int getCaptureWithPromoteMask() {
		return this.capture;
	}

	public int getColor() {
		return color;
	}

	public int getEpSquare() {
		return epSquare;
	}

	public int getFrom() {
		return from;
	}

	public String getLan() {
		return isCastleKSide() ? "O-O" : isCastleQSide() ? "O-O-O"
				: isDrop() ? PIECE_TO_SAN.charAt(getPiece()) + "@"
						+ SQUARE_TO_FILE_SAN.charAt(getTo())
						+ SQUARE_TO_RANK_SAN.charAt(getTo()) : ""
						+ SQUARE_TO_FILE_SAN.charAt(getFrom())
						+ SQUARE_TO_RANK_SAN.charAt(getFrom())
						+ "-"
						+ SQUARE_TO_FILE_SAN.charAt(getTo())
						+ SQUARE_TO_RANK_SAN.charAt(getTo())
						+ (isPromotion() ? "="
								+ PIECE_TO_SAN.charAt(piecePromotedTo
										& NOT_PROMOTED_MASK) : "");
	}

	public int getLastCastleState() {
		return lastCastleState;
	}

	public int getMoveCharacteristic() {
		return moveCharacteristic;
	}

	public int getPiece() {
		return piece;
	}

	public int getPiecePromotedTo() {
		return piecePromotedTo;
	}

	public int getPrevious50MoveCount() {
		return previous50MoveCount;
	}

	public String getSan() {
		return san;
	}

	public int getTo() {
		return to;
	}

	public boolean isCapture() {
		return getCapture() != GameConstants.EMPTY;
	}

	public boolean isPromotion() {
		return piecePromotedTo != GameConstants.EMPTY;
	}

	public boolean isWhitesMove() {
		return color == GameConstants.WHITE;
	}

	public void setCapture(int capture) {
		this.capture = (byte) capture;
	}

	public void setColor(int color) {
		this.color = (byte) color;
	}

	public void setEpSquare(int epSquare) {
		this.epSquare = (byte) epSquare;
	}

	public void setFrom(int from) {
		this.from = (byte) from;
	}

	public void setLastCastleState(int lastCastleState) {
		this.lastCastleState = (byte) lastCastleState;
	}

	public void setMoveCharacteristic(int moveCharacteristic) {
		this.moveCharacteristic = (byte) moveCharacteristic;
	}

	public void setPiece(int piece) {
		this.piece = (byte) piece;
	}

	public void setPiecePromotedTo(int piecePromotedTo) {
		this.piecePromotedTo = (byte) piecePromotedTo;
	}

	public void setPrevious50MoveCount(int previous50MoveCount) {
		this.previous50MoveCount = (byte) previous50MoveCount;
	}

	public void setSan(String san) {
		this.san = san;
	}

	public void setTo(int to) {
		this.to = (byte) to;
	}

	public boolean isDrop() {
		return (moveCharacteristic & DROP_CHARACTERISTIC) != 0;
	}

	public boolean isCastleKSide() {
		return (moveCharacteristic & KINGSIDE_CASTLING_CHARACTERISTIC) != 0;
	}

	public boolean isCastleQSide() {
		return (moveCharacteristic & QUEENSIDE_CASTLING_CHARACTERISTIC) != 0;
	}

	public boolean isEnPassant() {
		return (moveCharacteristic & EN_PASSANT_CHARACTERISTIC) != 0;
	}

	@Override
	public String toString() {
		return getSan() != null ? getSan() : getLan();
	}
}
