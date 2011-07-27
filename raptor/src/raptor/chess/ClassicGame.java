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
package raptor.chess;

import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.diagonalMove;
import static raptor.chess.util.GameUtils.getBitboard;
import static raptor.chess.util.GameUtils.getFile;
import static raptor.chess.util.GameUtils.getOppositeColor;
import static raptor.chess.util.GameUtils.getRank;
import static raptor.chess.util.GameUtils.getSan;
import static raptor.chess.util.GameUtils.getSquare;
import static raptor.chess.util.GameUtils.getString;
import static raptor.chess.util.GameUtils.kingMove;
import static raptor.chess.util.GameUtils.knightMove;
import static raptor.chess.util.GameUtils.moveOne;
import static raptor.chess.util.GameUtils.orthogonalMove;
import static raptor.chess.util.GameUtils.pawnCapture;
import static raptor.chess.util.GameUtils.pawnDoublePush;
import static raptor.chess.util.GameUtils.pawnEpCapture;
import static raptor.chess.util.GameUtils.pawnSinglePush;
import static raptor.chess.util.ZobristUtils.zobrist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnUtils;
import raptor.chess.util.GameUtils;
import raptor.chess.util.SanUtils;
import raptor.chess.util.SanUtils.SanValidations;
import raptor.service.EcoService;

/**
 * Implements Classic game rules and provides protected methods so it can easily
 * be subclassed to override behavior for variants.
 */
public class ClassicGame implements Game {

	protected int[] board = new int[64];
	protected int[] castling = new int[2];
	protected long[] colorBB = new long[2];
	protected int colorToMove;

	protected int[][] dropCounts = new int[2][7];
	protected long emptyBB;
	protected int epSquare = EMPTY_SQUARE;
	protected int fiftyMoveCount;
	protected int halfMoveCount;
	protected String id;
	protected int initialEpSquare = EMPTY_SQUARE;
	protected int[] moveRepHash = new int[MOVE_REP_CACHE_SIZE];
	protected MoveList moves = new MoveList();
	protected long notColorToMoveBB;
	protected long occupiedBB;
	protected Map<PgnHeader, String> pgnHeaderMap = new HashMap<PgnHeader, String>();
	protected long[][] pieceBB = new long[2][7];
	protected int[][] pieceCounts = new int[2][7];
	protected int state;
	protected long zobristGameHash;
	protected long zobristPositionHash;

	public ClassicGame() {
		setHeader(PgnHeader.Variant, Variant.classic.name());
		setHeader(PgnHeader.Result, Result.ON_GOING.getDescription());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addState(int state) {
		setState(getState() | state);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean areBothKingsOnBoard() {
		return getPieceBB(WHITE, KING) != 0L && getPieceBB(BLACK, KING) != 0L;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBlackCastleLong() {
		return (castling[BLACK] & CASTLE_LONG) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBlackCastleShort() {
		return (castling[BLACK] & CASTLE_SHORT) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canWhiteCastleLong() {
		return (castling[WHITE] & CASTLE_LONG) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canWhiteCastleShort() {
		return (castling[WHITE] & CASTLE_SHORT) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		board = new int[64];
		castling = new int[2];
		colorBB = new long[2];
		colorToMove = 0;
		dropCounts = new int[2][7];
		emptyBB = 0L;
		epSquare = EMPTY_SQUARE;
		fiftyMoveCount = 0;
		halfMoveCount = 0;
		initialEpSquare = EMPTY_SQUARE;
		zobristGameHash = 0L;
		zobristPositionHash = 0L;
		moveRepHash = new int[MOVE_REP_CACHE_SIZE];
		notColorToMoveBB = 0L;
		occupiedBB = 0L;
		pieceBB = new long[2][7];
		pieceCounts = new int[2][7];
		moves = new MoveList();
		setHeader(PgnHeader.Result, Result.ON_GOING.getDescription());
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearState(int state) {
		setState(getState() & ~state);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClassicGame deepCopy(boolean ignoreHashes) {
		ClassicGame result = new ClassicGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void forceMove(Move move) {
		move.setLastWhiteCastlingState(getCastling(WHITE));
		move.setLastBlackCastlingState(getCastling(BLACK));
		setSan(move);
		switch (move.getMoveCharacteristic()) {
		case Move.EN_PASSANT_CHARACTERISTIC:
			makeEPMove(move);
			break;
		case Move.SHORT_CASTLING_CHARACTERISTIC:
		case Move.LONG_CASTLING_CHARACTERISTIC:
			makeCastlingMove(move);
			break;
		case Move.DROP_CHARACTERISTIC:
			makeDropMove(move);
			break;
		default:
			makeNonEpNonCastlingMove(move);
			break;
		}

		int oppToMove = getOppositeColor(getColorToMove());

		move.setPrevious50MoveCount(getFiftyMoveCount());
		if (move.isCapture()) {
			decrementPieceCount(oppToMove, move.getCaptureWithPromoteMask());
			incrementDropCount(getColorToMove(), move
					.getCaptureWithPromoteMask());
			setFiftyMoveCount(0);
		} else if (move.isDrop()) {
			incrementPieceCount(getColorToMove(), move.getPiece());
			decrementDropCount(getColorToMove(), move.getPiece());
			setFiftyMoveCount(0);
		} else if (move.getPiece() == PAWN) {
			setFiftyMoveCount(0);
		} else {
			setFiftyMoveCount(getFiftyMoveCount() + 1);
		}

		setColorToMove(oppToMove);
		setNotColorToMoveBB(~getColorBB(getColorToMove()));
		setHalfMoveCount(getHalfMoveCount() + 1);

		getMoveList().append(move);

		updateZobristHash();
		incrementRepCount();
		
		move.setFullMoveCount((getHalfMoveCount() - 1) / 2 + 1);
		move.setHalfMoveCount(getHalfMoveCount());
		
		if (move.getFullMoveCount() < 19) {
			updateEcoHeaders(move);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public PgnHeader[] getAllHeaders() {
		return pgnHeaderMap.keySet().toArray(new PgnHeader[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public PgnHeader[] getAllNonRequiredHeaders() {
		List<PgnHeader> result = new ArrayList<PgnHeader>(pgnHeaderMap.size());
		for (PgnHeader key : pgnHeaderMap.keySet()) {
			if (!key.isRequired()) {
				result.add(key);
			}
		}
		return result.toArray(new PgnHeader[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] getBoard() {
		return board;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCastling(int color) {
		return castling[color];
	}

	/**
	 * {@inheritDoc}
	 */
	public long getColorBB(int color) {
		return colorBB[color];
	}

	/**
	 * {@inheritDoc}
	 */
	public int getColorToMove() {
		return colorToMove;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getDropCount(int color, int piece) {
		return dropCounts[color][piece];
	}

	/**
	 * {@inheritDoc}
	 */
	public long getEmptyBB() {
		return emptyBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getEpSquare() {
		return epSquare;
	}

	public String getFenCastle() {
		String whiteCastlingFen = getCastling(WHITE) == CASTLE_NONE ? ""
				: getCastling(WHITE) == CASTLE_BOTH ? "KQ"
						: getCastling(WHITE) == CASTLE_SHORT ? "K" : "Q";
		String blackCastlingFen = getCastling(BLACK) == CASTLE_NONE ? ""
				: getCastling(BLACK) == CASTLE_BOTH ? "kq"
						: getCastling(BLACK) == CASTLE_SHORT ? "k" : "q";

		return whiteCastlingFen.equals("") && blackCastlingFen.equals("") ? "-"
				: whiteCastlingFen + blackCastlingFen;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFiftyMoveCount() {
		return fiftyMoveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFullMoveCount() {
		return getHalfMoveCount() / 2 + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getHalfMoveCount() {
		return halfMoveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHeader(PgnHeader header) {
		return pgnHeaderMap.get(header);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInitialEpSquare() {
		return initialEpSquare;
	}

	/**
	 * {@inheritDoc}
	 */
	public Move getLastMove() {
		if (moves.getSize() != 0) {
			return moves.get(moves.getSize() - 1);
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public PriorityMoveList getLegalMoves() {
		PriorityMoveList result = getPseudoLegalMoves();

		for (int i = 0; i < result.getHighPrioritySize(); i++) {
			Move move = result.getHighPriority(i);
			forceMove(move);
			if (!isLegalPosition()) {
				result.removeHighPriority(i);
				i--;
			}

			rollback();
		}

		for (int i = 0; i < result.getLowPrioritySize(); i++) {
			Move move = result.getLowPriority(i);
			forceMove(move);

			if (!isLegalPosition()) {
				result.removeLowPriority(i);
				i--;
			}

			rollback();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public MoveList getMoveList() {
		return moves;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getNotColorToMoveBB() {
		return notColorToMoveBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getOccupiedBB() {
		return occupiedBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPiece(int square) {
		return board[square] & NOT_PROMOTED_MASK;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPieceBB(int piece) {
		return pieceBB[0][piece] | pieceBB[1][piece];
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPieceBB(int color, int piece) {
		return pieceBB[color][piece];
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPieceCount(int color, int piece) {
		return pieceCounts[color][piece & NOT_PROMOTED_MASK];
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] getPieceJailCounts(int color) {
		int totalKings = 0;
		int totalPawns = 0;
		int totalKnights = 0;
		int totalBishops = 0;
		int totalRooks = 0;
		int totalQueens = 0;

		for (int i = 0; i < board.length; i++) {
			int piece = board[i];
			if (piece != EMPTY
					&& (GameUtils.getBitboard(i) & getColorBB(color)) != 0) {
				if ((piece & PROMOTED_MASK) != 0) {
					totalPawns++;
				} else {
					switch (piece) {
					case KING:
						totalKings++;
						break;
					case PAWN:
						totalPawns++;
						break;
					case KNIGHT:
						totalKnights++;
						break;
					case BISHOP:
						totalBishops++;
						break;
					case ROOK:
						totalRooks++;
						break;
					case QUEEN:
						totalQueens++;
						break;
					}
				}
			}
		}

		int[] result = new int[7];
		result[PAWN] = 8 - totalPawns;
		result[KNIGHT] = 2 - totalKnights;
		result[BISHOP] = 2 - totalBishops;
		result[ROOK] = 2 - totalRooks;
		result[QUEEN] = 1 - totalQueens;
		result[KING] = 1 - totalKings;

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPieceWithPromoteMask(int square) {
		return board[square];
	}

	/**
	 * {@inheritDoc}
	 */
	public PriorityMoveList getPseudoLegalMoves() {
		PriorityMoveList result = new PriorityMoveList();
		generatePseudoQueenMoves(result);
		generatePseudoKnightMoves(result);
		generatePseudoBishopMoves(result);
		generatePseudoRookMoves(result);
		generatePseudoPawnMoves(result);
		generatePseudoKingMoves(result);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRepCount() {
		return moveRepHash[getRepHash()];
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRepHash() {
		return (int) (zobristPositionHash & MOVE_REP_CACHE_SIZE_MINUS_1);
	}

	/**
	 * {@inheritDoc}
	 */
	public Result getResult() {
		return Result.get(getHeader(PgnHeader.Result));
	}

	/**
	 * {@inheritDoc}
	 */
	public int getState() {
		return state;
	}

	/**
	 * {@inheritDoc}
	 */
	public Variant getVariant() {
		Variant result = Variant.classic;
		String variant = getHeader(PgnHeader.Variant);
		if (StringUtils.isNotBlank(variant)) {
			try {
				result = Variant.valueOf(variant);
			} catch (IllegalArgumentException iae) {
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getZobristGameHash() {
		return zobristGameHash;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getZobristPositionHash() {
		return zobristPositionHash;
	}

	/**
	 * {@inheritDoc}
	 */
	public void incrementRepCount() {
		moveRepHash[getRepHash()]++;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCheckmate() {
		return isCheckmate(getLegalMoves());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCheckmate(PriorityMoveList moveList) {
		return moveList.getSize() == 0 && isInCheck(getColorToMove());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInCheck() {
		return isInCheck(colorToMove, getPieceBB(colorToMove, KING));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInCheck(int color) {
		return isInCheck(color, getPieceBB(color, KING));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInCheck(int color, long pieceBB) {
		long kingBB = pieceBB;
		int kingSquare = bitscanForward(kingBB);
		int oppositeColor = getOppositeColor(color);

		return !(pawnCapture(oppositeColor, getPieceBB(oppositeColor, PAWN),
				kingBB) == 0L
				&& (orthogonalMove(kingSquare, getEmptyBB(), getOccupiedBB()) & (getPieceBB(
						oppositeColor, ROOK) | getPieceBB(oppositeColor, QUEEN))) == 0L
				&& (diagonalMove(kingSquare, getEmptyBB(), getOccupiedBB()) & (getPieceBB(
						oppositeColor, BISHOP) | getPieceBB(oppositeColor,
						QUEEN))) == 0L
				&& (kingMove(kingSquare) & getPieceBB(oppositeColor, KING)) == 0L && (knightMove(kingSquare) & getPieceBB(
				oppositeColor, KNIGHT)) == 0L);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInState(int state) {
		return (getState() & state) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLegalPosition() {
		return areBothKingsOnBoard()
				&& !isInCheck(getOppositeColor(getColorToMove()));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSettingEcoHeaders() {
		return isInState(UPDATING_ECO_HEADERS_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSettingMoveSan() {
		return isInState(UPDATING_SAN_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isStalemate() {
		return isStalemate(getLegalMoves());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isStalemate(PriorityMoveList moveList) {
		return moveList.getSize() == 0 && !isInCheck(getColorToMove());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isWhitesMove() {
		return colorToMove == WHITE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Move makeDropMove(int piece, int destination)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not supported in classical");
	}

	/**
	 * {@inheritDoc}
	 */
	public Move makeLanMove(String lan) throws IllegalArgumentException {
		Move move = null;

		Move[] legals = getLegalMoves().asArray();

		for (int i = 0; move == null && i < legals.length; i++) {
			Move candidate = legals[i];
			if (candidate.getLan().equals(lan)) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: " + lan + " \n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * Makes a move using the start/end square.
	 * 
	 * @param startSquare
	 *            The start square.
	 * @param endSquare
	 *            The end square.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move is illegal.
	 */
	public Move makeMove(int startSquare, int endSquare)
			throws IllegalArgumentException {
		Move move = null;

		Move[] legals = getLegalMoves().asArray();

		for (int i = 0; move == null && i < legals.length; i++) {
			Move candidate = legals[i];
			if (candidate.getFrom() == startSquare
					&& candidate.getTo() == endSquare) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: "
					+ getSan(startSquare) + " " + getSan(endSquare) + " \n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * Makes a move using the start/end square and the specified promotion
	 * piece.
	 * 
	 * @param startSquare
	 *            The start square.
	 * @param endSquare
	 *            The end square.
	 * @param promotionPiece
	 *            The non colored piece constant representing the promoted
	 *            piece.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move is illegal.
	 */
	public Move makeMove(int startSquare, int endSquare, int promotePiece)
			throws IllegalArgumentException {
		Move move = null;

		Move[] legals = getLegalMoves().asArray();

		for (int i = 0; move == null && i < legals.length; i++) {
			Move candidate = legals[i];
			if (candidate.getFrom() == startSquare
					&& candidate.getTo() == endSquare
					&& candidate.getPiecePromotedTo() == promotePiece) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: "
					+ getSan(startSquare) + "-" + getSan(endSquare) + "="
					//+ GameConstants.PIECE_TO_SAN.charAt(promotePiece) + "\n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * {@inheritDoc}
	 */
	public Move makeSanMove(String shortAlgebraic)
			throws IllegalArgumentException {
		SanValidations validations = SanUtils.getValidations(shortAlgebraic);
		Move[] pseudoLegals = getPseudoLegalMoves().asArray();

		Move result = makeSanMoveOverride(shortAlgebraic, validations,
				pseudoLegals);
		if (result == null) {
			// Examples:
			// e4 (a pawn move to e4).
			// e8=Q (a pawn promotion without a capture).
			// de=Q (a pawn promotion from a capture).
			// ed (e pawn captures d pawn).
			// Ne3 (a Knight moving to e3).
			// N5e3 (disambiguity for two knights which can move to e3, the 5th
			// rank
			// knight is the one that should move).
			// Nfe3 (disambiguity for two knights which can move to e3, the
			// knight
			// on the f file is the one that should move).
			// Nf1e3 (disambiguity for three knights which cam move to e3, the
			// f1
			// knight is the one that should move).
			if (!validations.isValidStrict()) {
				throw new IllegalArgumentException("Invalid short algebraic: "
						+ shortAlgebraic);
			}

			int candidatePromotedPiece = EMPTY;

			if (validations.isCastleKSideStrict()) {
				for (Move move : pseudoLegals) {
					if (move != null
							&& (move.getMoveCharacteristic() & Move.SHORT_CASTLING_CHARACTERISTIC) != 0) {
						result = move;
						break;
					}
				}
			} else if (validations.isCastleQSideStrict()) {
				for (Move move : pseudoLegals) {
					if (move != null
							&& (move.getMoveCharacteristic() & Move.LONG_CASTLING_CHARACTERISTIC) != 0) {
						result = move;
						break;
					}
				}
			} else {
				MoveList matches = new MoveList(10);
				if (validations.isPromotion()) {
					char pieceChar = validations.getStrictSan().charAt(
							validations.getStrictSan().length() - 1);
					candidatePromotedPiece = SanUtils.sanToPiece(pieceChar);
				}

				if (validations.isPawnMove()) {
					int candidatePieceMoving = PAWN;
					if (validations.isEpOrAmbigPxStrict()
							|| validations.isAmbigPxPromotionStrict()) {

						int end = getSquare(GameConstants.RANK_FROM_SAN
								.indexOf(validations.getStrictSan().charAt(2)),
								GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(1)));

						int startRank = getRank(end)
								+ (colorToMove == WHITE ? -1 : +1);

						if (startRank > 7 || startRank < 0) {
							throw new IllegalArgumentException(
									"Invalid short algebraic: "
											+ shortAlgebraic);
						}

						int start = getSquare(startRank,
								GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(0)));

						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece() == candidatePieceMoving
									&& move.isCapture()
									&& move.getFrom() == start
									&& move.getTo() == end
									&& move.getPiecePromotedTo() == candidatePromotedPiece) {
								matches.append(move);
							}
						}
					} else {
						// handle captures
						if (validations.isPxStrict()
								|| validations.isPxPPromotionStrict()) {
							int startFile = GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											0));
							int endFile = GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											1));

							for (Move move : pseudoLegals) {
								if (move != null
										&& move.getPiece() == candidatePieceMoving
										&& getFile(move.getFrom()) == startFile
										&& getFile(move.getTo()) == endFile
										&& move.isCapture()
										&& move.getPiecePromotedTo() == candidatePromotedPiece) {
									matches.append(move);
								}
							}
						}
						// handle non captures.
						else {
							int end = getSquare(GameConstants.RANK_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											1)), GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											0)));

							for (Move move : pseudoLegals) {
								if (move != null
										&& move.getPiece() == candidatePieceMoving
										&& !move.isCapture()
										&& move.getTo() == end
										&& move.getPiecePromotedTo() == candidatePromotedPiece) {
									matches.append(move);
								}
							}
						}
					}
				} else {
					int candidatePieceMoving = SanUtils.sanToPiece(validations
							.getStrictSan().charAt(0));
					int end = getSquare(GameConstants.RANK_FROM_SAN
							.indexOf(validations.getStrictSan().charAt(
									validations.getStrictSan().length() - 1)),
							GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan()
											.charAt(
													validations.getStrictSan()
															.length() - 2)));

					if (validations.isDisambigPieceRankStrict()) {
						int startRank = RANK_FROM_SAN.indexOf(validations
								.getStrictSan().charAt(1));
						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece() == candidatePieceMoving
									&& move.getTo() == end
									&& getRank(move.getFrom()) == startRank) {
								matches.append(move);
							}
						}
					} else if (validations.isDisambigPieceFileStrict()) {
						int startFile = FILE_FROM_SAN.indexOf(validations
								.getStrictSan().charAt(1));
						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece() == candidatePieceMoving
									&& move.getTo() == end
									&& getFile(move.getFrom()) == startFile) {
								matches.append(move);
							}
						}
					} else if (validations.isDisambigPieceRankFileStrict()) {
						int startSquare = getSquare(GameConstants.RANK_FROM_SAN
								.indexOf(validations.getStrictSan().charAt(2)),
								GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(1)));
						FILE_FROM_SAN.indexOf(validations.getStrictSan()
								.charAt(1));
						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece() == candidatePieceMoving
									&& move.getTo() == end
									&& move.getFrom() == startSquare) {
								matches.append(move);
							}
						}
					} else {
						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece() == candidatePieceMoving
									&& move.getTo() == end) {
								matches.append(move);
							}
						}
					}
				}
				result = testForSanDisambiguationFromCheck(shortAlgebraic,
						matches);
			}
		}

		if (result == null) {
			throw new IllegalArgumentException("Illegal move " + shortAlgebraic
					+ "\n " + toString());
		}

		result.setSan(shortAlgebraic);
		if (!move(result)) {
			throw new IllegalArgumentException("Illegal move: " + result);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean move(Move move) {
		// first make the move.
		forceMove(move);
		if (!isLegalPosition()) {
			rollback();
			return false;
		}
		return true;
	}

	/**
	 * Copys the information from this game into the passed in game.
	 */
	public void overwrite(Game game, boolean ignoreHashes) {
		ClassicGame gameToOverwrite = (ClassicGame) game;
		gameToOverwrite.id = id;
		gameToOverwrite.state = state;
		gameToOverwrite.pgnHeaderMap = new HashMap<PgnHeader, String>(
				pgnHeaderMap);

		gameToOverwrite.moves = moves.deepCopy();
		gameToOverwrite.halfMoveCount = halfMoveCount;
		System.arraycopy(colorBB, 0, gameToOverwrite.colorBB, 0,
				gameToOverwrite.colorBB.length);
		for (int i = 0; i < pieceBB.length; i++) {
			System.arraycopy(pieceBB[i], 0, gameToOverwrite.pieceBB[i], 0,
					pieceBB[i].length);
		}
		System.arraycopy(board, 0, gameToOverwrite.board, 0, board.length);
		gameToOverwrite.occupiedBB = occupiedBB;
		gameToOverwrite.emptyBB = emptyBB;
		gameToOverwrite.notColorToMoveBB = notColorToMoveBB;
		System.arraycopy(castling, 0, gameToOverwrite.castling, 0,
				castling.length);
		gameToOverwrite.initialEpSquare = initialEpSquare;
		gameToOverwrite.epSquare = epSquare;
		gameToOverwrite.colorToMove = colorToMove;
		gameToOverwrite.fiftyMoveCount = fiftyMoveCount;
		for (int i = 0; i < pieceCounts.length; i++) {
			System.arraycopy(pieceCounts[i], 0, gameToOverwrite.pieceCounts[i],
					0, pieceCounts[i].length);
		}
		for (int i = 0; i < dropCounts.length; i++) {
			System.arraycopy(dropCounts[i], 0, gameToOverwrite.dropCounts[i],
					0, dropCounts[i].length);
		}
		gameToOverwrite.zobristPositionHash = zobristPositionHash;
		gameToOverwrite.zobristGameHash = zobristGameHash;

		if (!ignoreHashes) {
			System.arraycopy(moveRepHash, 0, gameToOverwrite.moveRepHash, 0,
					moveRepHash.length);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeHeader(PgnHeader headerName) {
		pgnHeaderMap.remove(headerName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void rollback() {
		Move move = getMoveList().removeLast();
		decrementRepCount();

		switch (move.getMoveCharacteristic()) {
		case Move.EN_PASSANT_CHARACTERISTIC:
			rollbackEpMove(move);
			break;
		case Move.SHORT_CASTLING_CHARACTERISTIC:
		case Move.LONG_CASTLING_CHARACTERISTIC:
			rollbackCastlingMove(move);
			break;
		case Move.DROP_CHARACTERISTIC:
			rollbackDropMove(move);
			break;
		default:
			rollbackNonEpNonCastlingMove(move);
			break;
		}

		int oppositeToMove = getOppositeColor(getColorToMove());

		if (move.isCapture()) {
			incrementPieceCount(getColorToMove(), move
					.getCaptureWithPromoteMask());
			decrementDropCount(oppositeToMove, move.getCaptureWithPromoteMask());
		} else if (move.isDrop()) {
			decrementPieceCount(oppositeToMove, move.getPiece());
			incrementDropCount(oppositeToMove, move.getPiece());
		}

		setColorToMove(oppositeToMove);
		setNotColorToMoveBB(~getColorBB(getColorToMove()));
		setHalfMoveCount(getHalfMoveCount() - 1);

		setFiftyMoveCount(move.getPrevious50MoveCount());
		setCastling(WHITE, move.getLastWhiteCastlingState());
		setCastling(BLACK, move.getLastBlackCastlingState());

		updateZobristHash();

		rollbackEcoHeaders(move);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBoard(int[] board) {
		this.board = board;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCastling(int color, int castling) {
		this.castling[color] = castling;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setColorBB(int color, long bb) {
		colorBB[color] = bb;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setColorToMove(int color) {
		colorToMove = color;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDropCount(int color, int piece, int count) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		}
		dropCounts[color][piece] = count;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEmptyBB(long emptyBB) {
		this.emptyBB = emptyBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEpSquare(int epSquare) {
		this.epSquare = epSquare;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFiftyMoveCount(int fiftyMoveCount) {
		this.fiftyMoveCount = fiftyMoveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHalfMoveCount(int halfMoveCount) {
		this.halfMoveCount = halfMoveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHeader(PgnHeader header, String value) {
		pgnHeaderMap.put(header, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInitialEpSquare(int initialEpSquare) {
		this.initialEpSquare = initialEpSquare;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNotColorToMoveBB(long notColorToMoveBB) {
		this.notColorToMoveBB = notColorToMoveBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOccupiedBB(long occupiedBB) {
		this.occupiedBB = occupiedBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPiece(int square, int piece) {
		board[square] = piece;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPieceBB(int color, int piece, long bb) {
		pieceBB[color][piece] = bb;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPieceCount(int color, int piece, int count) {
		pieceCounts[color][piece & NOT_PROMOTED_MASK] = count;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setZobristGameHash(long hash) {
		zobristGameHash = hash;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setZobristPositionHash(long hash) {
		zobristPositionHash = hash;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toFen() {
		// 

		StringBuilder result = new StringBuilder(77);
		result.append(toFenPosition());

		result.append(colorToMove == WHITE ? " w" : " b");

		result.append(" " + getFenCastle());
		result.append(" " + getSan(epSquare));
		result.append(" " + fiftyMoveCount);
		result.append(" " + getFullMoveCount());

		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toFenPosition() {
		StringBuilder result = new StringBuilder(77);
		for (int j = 7; j > -1; j--) {
			int consecutiveEmpty = 0;
			for (int i = 0; i < 8; i++) {
				int square = getSquare(j, i);
				int piece = getPiece(square);

				if (piece == EMPTY) {
					consecutiveEmpty++;
				} else {
					long squareBB = getBitboard(square);
					int color = (getPieceBB(WHITE, piece) & squareBB) != 0L ? WHITE
							: BLACK;
					if (consecutiveEmpty > 0) {
						result.append(consecutiveEmpty);
						consecutiveEmpty = 0;
					}
					result.append(COLOR_PIECE_TO_CHAR[color].charAt(piece));
				}
			}
			if (j != 0) {
				result.append((consecutiveEmpty != 0 ? consecutiveEmpty : "")
						+ "/");
			} else if (j == 0) {
				result.append(consecutiveEmpty != 0 ? consecutiveEmpty : "");
			}
		}
		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toPgn() {
		StringBuilder builder = new StringBuilder(2500);

		// Set all of the required headers.
		for (PgnHeader requiredHeader : PgnHeader.REQUIRED_HEADERS) {
			String headerValue = getHeader(requiredHeader);
			if (StringUtils.isBlank(headerValue)) {
				headerValue = PgnHeader.UNKNOWN_VALUE;
				setHeader(requiredHeader, headerValue);
			}
		}

		List<PgnHeader> pgnHeaders = new ArrayList<PgnHeader>(pgnHeaderMap
				.keySet());
		Collections.sort(pgnHeaders);

		for (PgnHeader header : pgnHeaders) {
			PgnUtils.getHeaderLine(builder, header.name(), getHeader(header));
			builder.append("\n");
		}
		builder.append("\n");

		boolean nextMoveRequiresNumber = true;
		int charsInCurrentLine = 0;

		// TO DO: add breaking up lines in comments.
		for (int i = 0; i < getHalfMoveCount(); i++) {
			int charsBefore = builder.length();
			nextMoveRequiresNumber = PgnUtils.getMove(builder, getMoveList()
					.get(i), nextMoveRequiresNumber);
			charsInCurrentLine += builder.length() - charsBefore;

			if (charsInCurrentLine > 75) {
				charsInCurrentLine = 0;
				builder.append("\n");
			} else {
				builder.append(" ");
			}
		}

		builder.append(getResult().getDescription());
		return builder.toString();
	}

	/**
	 * Returns a dump of the game class suitable for debugging. Quite a lot of
	 * information is produced and its an expensive operation, use with care.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(1000);

		result.append(getString(new String[] { "emptyBB", "occupiedBB",
				"notColorToMoveBB", "color[WHITE]", "color[BLACK]" },
				new long[] { emptyBB, occupiedBB, notColorToMoveBB,
						getColorBB(WHITE), getColorBB(BLACK) })
				+ "\n\n");

		result.append(getString(new String[] { "[WHITE][PAWN]",
				"[WHITE][KNIGHT]", "[WHITE][BISHOP]", "[WHITE][ROOK]",
				"[WHITE][QUEEN]", "[WHITE][KING]" }, new long[] {
				getPieceBB(WHITE, PAWN), getPieceBB(WHITE, KNIGHT),
				getPieceBB(WHITE, BISHOP), getPieceBB(WHITE, ROOK),
				getPieceBB(WHITE, QUEEN), getPieceBB(WHITE, KING) })
				+ "\n\n");

		result.append(getString(new String[] { "[BLACK][PAWN]",
				"[BLACK][KNIGHT]", "[BLACK][BISHOP]", "[BLACK][ROOK]",
				"[BLACK][QUEEN]", "[BLACK][KING]" }, new long[] {
				getPieceBB(BLACK, PAWN), getPieceBB(BLACK, KNIGHT),
				getPieceBB(BLACK, BISHOP), getPieceBB(BLACK, ROOK),
				getPieceBB(BLACK, QUEEN), getPieceBB(BLACK, KING) })
				+ "\n\n");

		for (int i = 7; i > -1; i--) {
			for (int j = 0; j < 8; j++) {
				int square = getSquare(i, j);
				int piece = getPiece(square);
				int color = (getBitboard(square) & getColorBB(colorToMove)) != 0L ? colorToMove
						: getOppositeColor(colorToMove);

				result.append("|" + COLOR_PIECE_TO_CHAR[color].charAt(piece));
			}
			result.append("|   ");

			switch (i) {
			case 7:
				result.append("To Move: " + COLOR_DESCRIPTION[colorToMove]
						+ " " + "Last Move: "
						+ (moves.getSize() == 0 ? "" : moves.getLast()));
				break;

			case 6:
				result.append(getPieceCountsString());
				break;
			case 5:
				result.append("Moves: " + halfMoveCount + " EP: "
						+ getSan(epSquare) + " Castle: " + getFenCastle());
				break;
			case 4:
				result.append("FEN: " + toFen());
				break;
			case 3:
				result.append("State: " + state + " Variant="
						+ getHeader(PgnHeader.Variant) + " Result="
						+ getResult());
				break;
			case 2:
				result.append("Event: " + getHeader(PgnHeader.Event) + " Site="
						+ getHeader(PgnHeader.Site) + " Date="
						+ getHeader(PgnHeader.Date));
				break;
			case 1:
				result.append("WhiteName: " + getHeader(PgnHeader.White)
						+ " BlackName=" + getHeader(PgnHeader.Black)
						+ " WhiteTime="
						+ getHeader(PgnHeader.WhiteRemainingMillis)
						+ " whiteLag=" + getHeader(PgnHeader.WhiteLagMillis)
						+ " blackRemainingTImeMillis = "
						+ getHeader(PgnHeader.BlackRemainingMillis)
						+ " blackLag=" + getHeader(PgnHeader.BlackLagMillis));

				break;
			default:
				result.append("initialWhiteClock: "
						+ getHeader(PgnHeader.WhiteClock)
						+ " initialBlackClocks="
						+ getHeader(PgnHeader.BlackClock));
				break;
			}

			result.append("\n");
		}

		String legalMovesString = Arrays.toString(getLegalMoves().asArray());
		result.append("\n");
		result.append(WordUtils.wrap("\nLegals=" + legalMovesString, 80, "\n",
				true));
		result.append(WordUtils.wrap("\nMovelist=" + moves, 80, "\n", true));

		List<String> squaresWithPromoteMasks = new LinkedList<String>();
		for (int i = 0; i < board.length; i++) {
			if ((getPieceWithPromoteMask(i) & PROMOTED_MASK) != 0) {
				squaresWithPromoteMasks.add(getSan(i));
			}
		}
		result.append("\nSquares with promote masks: "
				+ squaresWithPromoteMasks);

		return result.toString();
	}

	/**
	 * Currently places captures and promotions ahead of non captures.
	 */
	protected void addMove(Move move, PriorityMoveList moves) {
		if (move.isCapture() || move.isPromotion()) {
			moves.appendHighPriority(move);
		} else {
			moves.appendLowPriority(move);
		}
	}

	/**
	 * Decrements the drop count for the specified piece. This method handles
	 * promotion masks as well.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	protected void decrementDropCount(int color, int piece) {

		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;

		}
		dropCounts[color][piece]--;
	}

	/**
	 * Decrements the piece count for the specified piece. This method handles
	 * promotion masks as well.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	protected void decrementPieceCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece &= NOT_PROMOTED_MASK;
		}
		pieceCounts[color][piece]--;
	}

	/**
	 * Decrements the current positions repetition count.
	 */
	protected void decrementRepCount() {
		moveRepHash[getRepHash()]--;
	}

	/**
	 * Generates all of the pseudo legal bishop moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoBishopMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), BISHOP);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = diagonalMove(fromSquare, getEmptyBB(), getOccupiedBB())
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);

				addMove(new Move(fromSquare, toSquare,
						getPieceWithPromoteMask(fromSquare), getColorToMove(),
						contents), moves);
				toBB = bitscanClear(toBB);
			}
			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal king castling moves in the position and
	 * adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		// The king destination square isnt checked, its checked when legal
		// getMoves() are checked.

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == E1 && getPiece(SQUARE_G1) == EMPTY
				&& GameUtils.isWhitePiece(this, SQUARE_H1)
				&& getPiece(SQUARE_H1) == ROOK && getPiece(SQUARE_F1) == EMPTY
				&& !isInCheck(WHITE, E1) && !isInCheck(WHITE, F1)) {
			moves
					.appendLowPriority(new Move(SQUARE_E1, SQUARE_G1, KING,
							getColorToMove(), EMPTY,
							Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == E1 && GameUtils.isWhitePiece(this, SQUARE_A1)
				&& getPiece(SQUARE_A1) == ROOK && getPiece(SQUARE_D1) == EMPTY
				&& getPiece(SQUARE_C1) == EMPTY && getPiece(SQUARE_B1) == EMPTY
				&& !isInCheck(WHITE, E1) && !isInCheck(WHITE, D1)) {
			moves
					.appendLowPriority(new Move(SQUARE_E1, SQUARE_C1, KING,
							getColorToMove(), EMPTY,
							Move.LONG_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == E8 && !GameUtils.isWhitePiece(this, SQUARE_H8)
				&& getPiece(SQUARE_H8) == ROOK && getPiece(SQUARE_G8) == EMPTY
				&& getPiece(SQUARE_F8) == EMPTY && !isInCheck(BLACK, E8)
				&& !isInCheck(BLACK, F8)) {
			moves
					.appendLowPriority(new Move(SQUARE_E8, SQUARE_G8, KING,
							getColorToMove(), EMPTY,
							Move.SHORT_CASTLING_CHARACTERISTIC));

		}

		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& !GameUtils.isWhitePiece(this, SQUARE_A8)
				&& getPiece(SQUARE_A8) == ROOK && fromBB == E8
				&& getPiece(SQUARE_D8) == EMPTY && getPiece(SQUARE_C8) == EMPTY
				&& getPiece(SQUARE_B8) == EMPTY && !isInCheck(BLACK, E8)
				&& !isInCheck(BLACK, D8)) {
			moves
					.appendLowPriority(new Move(SQUARE_E8, SQUARE_C8, KING,
							getColorToMove(), EMPTY,
							Move.LONG_CASTLING_CHARACTERISTIC));
		}
	}

	/**
	 * Generates all of the pseudo legal king moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoKingMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), KING);
		int fromSquare = bitscanForward(fromBB);
		long toBB = kingMove(fromSquare) & getNotColorToMoveBB();

		generatePseudoKingCastlingMoves(fromBB, moves);

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);

			int contents = getPieceWithPromoteMask(toSquare);

			addMove(new Move(fromSquare, toSquare, KING, getColorToMove(),
					contents), moves);
			toBB = bitscanClear(toBB);
			toSquare = bitscanForward(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal knight moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoKnightMoves(PriorityMoveList moves) {

		long fromBB = getPieceBB(getColorToMove(), KNIGHT);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = knightMove(fromSquare) & getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);
				int contents = getPieceWithPromoteMask(toSquare);

				addMove(new Move(fromSquare, toSquare,
						getPieceWithPromoteMask(fromSquare), getColorToMove(),
						contents), moves);

				toBB = bitscanClear(toBB);
				toSquare = bitscanForward(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal pawn captures in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnCaptures(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {

		long toBB = pawnCapture(getColorToMove(), fromBB,
				getColorBB(oppositeColor));

		while (toBB != 0L) {
			int toSquare = bitscanForward(toBB);
			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), KNIGHT,
						EMPTY_SQUARE, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), BISHOP,
						EMPTY_SQUARE, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), QUEEN, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), ROOK, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare)), moves);
			}
			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal double pawn pushes in the position and
	 * adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnDoublePush(int fromSquare, long fromBB,
			int oppositeColor, int epModifier, PriorityMoveList moves) {

		long toBB = pawnDoublePush(getColorToMove(), fromBB, getEmptyBB());

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);
			addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
					EMPTY, EMPTY, toSquare + epModifier,
					Move.DOUBLE_PAWN_PUSH_CHARACTERISTIC), moves);
			toBB = bitscanClear(toBB);
		}

	}

	/**
	 * Generates all of the pseudo En-Passant moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnEPCaptures(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {
		if (getEpSquare() != EMPTY) {

			long toBB = pawnEpCapture(getColorToMove(), fromBB, getPieceBB(
					oppositeColor, PAWN), getBitboard(getEpSquare()));

			if (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						PAWN, EMPTY, EMPTY_SQUARE,
						Move.EN_PASSANT_CHARACTERISTIC), moves);
			}
		}
	}

	/**
	 * Generates all of the pseudo legal pawn moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnMoves(PriorityMoveList moves) {
		long pawnsBB = getPieceBB(getColorToMove(), PAWN);
		int oppositeColor, epModifier;

		if (getColorToMove() == WHITE) {
			oppositeColor = BLACK;
			epModifier = -8;
		} else {
			oppositeColor = WHITE;
			epModifier = +8;
		}

		while (pawnsBB != 0) {
			int fromSquare = bitscanForward(pawnsBB);
			long fromBB = getBitboard(fromSquare);

			generatePseudoPawnEPCaptures(fromSquare, fromBB, oppositeColor,
					moves);
			generatePseudoPawnCaptures(fromSquare, fromBB, oppositeColor, moves);
			generatePseudoPawnSinglePush(fromSquare, fromBB, oppositeColor,
					moves);
			generatePseudoPawnDoublePush(fromSquare, fromBB, oppositeColor,
					epModifier, moves);

			pawnsBB = bitscanClear(pawnsBB);
		}
	}

	/**
	 * Generates all of the pseudo legal single push pawn moves in the position
	 * and adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnSinglePush(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {

		long toBB = pawnSinglePush(getColorToMove(), fromBB, getEmptyBB());

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);

			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, KNIGHT, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, BISHOP, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, QUEEN, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, ROOK, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY), moves);
			}

			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal queen moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoQueenMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), QUEEN);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = (orthogonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB()) | diagonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB()))
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);
				addMove(new Move(fromSquare, toSquare,
						getPieceWithPromoteMask(fromSquare), getColorToMove(),
						contents), moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal rook moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoRookMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), ROOK);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = orthogonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB())
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);
				addMove(new Move(fromSquare, toSquare,
						getPieceWithPromoteMask(fromSquare), getColorToMove(),
						contents), moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	protected String getDropCountsString() {
		return "Drop counts [WP=" + getDropCount(WHITE, PAWN) + " WN="
				+ getDropCount(WHITE, KNIGHT) + " WB="
				+ getDropCount(WHITE, BISHOP) + " WR="
				+ getDropCount(WHITE, ROOK) + " WQ="
				+ getDropCount(WHITE, QUEEN) + " WK="
				+ getDropCount(WHITE, KING) + "][BP="
				+ getDropCount(BLACK, PAWN) + " BN= "
				+ getDropCount(BLACK, KNIGHT) + " BB="
				+ getDropCount(BLACK, BISHOP) + " BR="
				+ getDropCount(BLACK, ROOK) + " BQ="
				+ getDropCount(BLACK, QUEEN) + " BK="
				+ getDropCount(BLACK, KING) + "]";
	}

	protected String getPieceCountsString() {
		return "Piece counts [WP=" + getPieceCount(WHITE, PAWN) + " WN="
				+ getPieceCount(WHITE, KNIGHT) + " WB="
				+ getPieceCount(WHITE, BISHOP) + " WR="
				+ getPieceCount(WHITE, ROOK) + " WQ="
				+ getPieceCount(WHITE, QUEEN) + " WK="
				+ getPieceCount(WHITE, KING) + "][BP="
				+ getPieceCount(BLACK, PAWN) + " BN= "
				+ getPieceCount(BLACK, KNIGHT) + " BB="
				+ getPieceCount(BLACK, BISHOP) + " BR="
				+ getPieceCount(BLACK, ROOK) + " BQ="
				+ getPieceCount(BLACK, QUEEN) + " BK="
				+ getPieceCount(BLACK, KING) + "]";
	}

	/**
	 * Returns true if the specified color has at least one drop piece.
	 * 
	 * @param color
	 *            THe color to check,
	 * @return True if the color has a drop piece, otherwise false.
	 */
	protected boolean hasNonPawnDropPiece(int color) {
		boolean result = false;
		for (int i = 2; i < dropCounts[color].length; i++) {
			if (dropCounts[color][i] > 0) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Increments the drop count. This method handles incrementing pieces with a
	 * promote mask.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The uncolored piece constant.
	 */
	protected void incrementDropCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		}
		dropCounts[color][piece]++;
	}

	/**
	 * Increments the piece count. This method handles incrementing pieces with
	 * a promote mask.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The uncolored piece constant.
	 */
	protected void incrementPieceCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece &= NOT_PROMOTED_MASK;
		}
		pieceCounts[color][piece]++;
	}

	protected void makeCastlingMove(Move move) {
		long kingFromBB, kingToBB, rookFromBB, rookToBB;

		if (move.getColor() == WHITE) {
			kingFromBB = E1;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = G1;
				rookFromBB = H1;
				rookToBB = F1;
				updateZobristPOCastleKsideWhite();
			} else {
				kingToBB = C1;
				rookFromBB = A1;
				rookToBB = D1;
				updateZobristPOCastleQsideWhite();
			}
		} else {
			kingFromBB = E8;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = G8;
				rookFromBB = H8;
				rookToBB = F8;
				updateZobristPOCastleKsideBlack();
			} else {
				kingToBB = C8;
				rookFromBB = A8;
				rookToBB = D8;
				updateZobristPOCastleQsideBlack();
			}
		}

		setPiece(bitscanForward(kingFromBB), EMPTY);
		setPiece(bitscanForward(kingToBB), KING);
		setPiece(bitscanForward(rookFromBB), EMPTY);
		setPiece(bitscanForward(rookToBB), ROOK);

		long kingFromTo = kingToBB | kingFromBB;
		long rookFromTo = rookToBB | rookFromBB;

		xor(move.getColor(), KING, kingFromTo);
		xor(move.getColor(), kingFromTo);
		setOccupiedBB(getOccupiedBB() ^ kingFromTo);
		setEmptyBB(getEmptyBB() ^ kingFromTo);

		xor(move.getColor(), ROOK, rookFromTo);
		xor(move.getColor(), rookFromTo);
		setOccupiedBB(getOccupiedBB() ^ rookFromTo);
		setEmptyBB(getEmptyBB() ^ rookFromTo);

		setCastling(getColorToMove(), CASTLE_NONE);

		setEpSquare(EMPTY_SQUARE);
	}

	/**
	 * Makes a drop move.
	 * 
	 * @param move
	 */
	protected void makeDropMove(Move move) {
		long toBB = getBitboard(move.getTo());
		int oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), toBB);
		xor(move.getColor(), move.getPiece(), toBB);
		setOccupiedBB(getOccupiedBB() ^ toBB);
		setEmptyBB(getEmptyBB() ^ toBB);

		updateZobristDrop(move, oppositeColor);

		setPiece(move.getTo(), move.getPiece());
		setEpSquare(EMPTY_SQUARE);
	}

	protected void makeEPMove(Move move) {
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;
		long captureBB = getColorToMove() == WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);

		int captureSquare = bitscanForward(captureBB);

		xor(move.getColor(), move.getPiece(), fromToBB);
		xor(move.getColor(), fromToBB);
		setOccupiedBB(getOccupiedBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ fromToBB);

		xor(move.getCaptureColor(), move.getPiece(), captureBB);
		xor(move.getCaptureColor(), captureBB);
		setOccupiedBB(getOccupiedBB() ^ captureBB);
		setEmptyBB(getEmptyBB() ^ captureBB);

		setPiece(move.getFrom(), EMPTY);
		setPiece(move.getTo(), PAWN);
		setPiece(captureSquare, EMPTY);

		updateZobristEP(move, captureSquare);
		setEpSquare(EMPTY_SQUARE);
	}

	protected void makeNonEpNonCastlingMove(Move move) {
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;
		int oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), fromToBB);

		if (move.isCapture()) {
			setOccupiedBB(getOccupiedBB() ^ fromBB);
			setEmptyBB(getEmptyBB() ^ fromBB);

			xor(oppositeColor, move.getCapture(), toBB);
			xor(oppositeColor, toBB);
			updateZobristPOCapture(move, oppositeColor);
		} else {
			setOccupiedBB(getOccupiedBB() ^ fromToBB);
			setEmptyBB(getEmptyBB() ^ fromToBB);
			updateZobristPONoCapture(move, oppositeColor);
		}

		if (move.isPromotion()) {
			xor(move.getColor(), move.getPiece(), fromBB);

			xor(move.getColor(), move.getPiecePromotedTo() & NOT_PROMOTED_MASK,
					toBB);

			setPiece(move.getTo(), move.getPiecePromotedTo() | PROMOTED_MASK);
			setPiece(move.getFrom(), EMPTY);

			// capture is handled in forceMove.
			// promoted piece never has a promote mask only captures do.
			// Promotes do not effect drop pieces.
			decrementPieceCount(getColorToMove(), PAWN);
			incrementPieceCount(getColorToMove(), move.getPiecePromotedTo());
		} else {
			xor(move.getColor(), move.getPiece(), fromToBB);

			setPiece(move.getTo(), move.getPieceWithPromoteMask());
			setPiece(move.getFrom(), EMPTY);
		}

		updateCastlingRightsForNonEpNonCastlingMove(move);

		setEpSquare(move.getEpSquare());
	}

	/**
	 * A method that makeSanMove invokes with the SanValidations it created. If
	 * a move can be made it should be returned. This method is provided so
	 * subclasses can enhance utilize the SanValidations without having to
	 * override makeSanMove and run the SAN validations again.
	 * 
	 * This method may also set certain pseudoLegals to ignore to null.
	 */
	protected Move makeSanMoveOverride(String shortAlgebraic,
			SanValidations validations, Move[] pseudoLegals) {
		return null;
	}

	protected void rollbackCastlingMove(Move move) {
		long kingFromBB, kingToBB, rookFromBB, rookToBB;

		if (move.getColor() == WHITE) {
			kingFromBB = E1;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = G1;
				rookFromBB = H1;
				rookToBB = F1;
				updateZobristPOCastleKsideWhite();

			} else {
				kingToBB = C1;
				rookFromBB = A1;
				rookToBB = D1;
				updateZobristPOCastleQsideWhite();
			}
		} else {
			kingFromBB = E8;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = G8;
				rookFromBB = H8;
				rookToBB = F8;
				updateZobristPOCastleKsideBlack();
			} else {
				kingToBB = C8;
				rookFromBB = A8;
				rookToBB = D8;
				updateZobristPOCastleQsideBlack();
			}
		}

		setPiece(bitscanForward(kingFromBB), KING);
		setPiece(bitscanForward(kingToBB), EMPTY);
		setPiece(bitscanForward(rookFromBB), ROOK);
		setPiece(bitscanForward(rookToBB), EMPTY);

		long kingFromTo = kingToBB | kingFromBB;
		long rookFromTo = rookToBB | rookFromBB;

		xor(move.getColor(), KING, kingFromTo);
		xor(move.getColor(), kingFromTo);
		setOccupiedBB(getOccupiedBB() ^ kingFromTo);
		setEmptyBB(getEmptyBB() ^ kingFromTo);

		xor(move.getColor(), ROOK, rookFromTo);
		xor(move.getColor(), rookFromTo);
		setOccupiedBB(getOccupiedBB() ^ rookFromTo);
		setEmptyBB(getEmptyBB() ^ rookFromTo);

		setEpSquareFromPreviousMove();
	}

	protected void rollbackDropMove(Move move) {
		long toBB = getBitboard(move.getTo());
		int oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), toBB);
		xor(move.getColor(), move.getPiece(), toBB);
		setOccupiedBB(getOccupiedBB() ^ toBB);
		setEmptyBB(getEmptyBB() ^ toBB);

		updateZobristDrop(move, oppositeColor);

		setPiece(move.getTo(), EMPTY);
		setEpSquareFromPreviousMove();
	}

	protected void rollbackEcoHeaders(Move move) {
		if (isSettingEcoHeaders()) {
			if (StringUtils.isNotBlank(move.getPreviousEcoHeader())) {
				setHeader(PgnHeader.ECO, move.getPreviousEcoHeader());
			} else {
				removeHeader(PgnHeader.ECO);
			}
			if (StringUtils.isNotBlank(move.getPreviousOpeningHeader())) {
				setHeader(PgnHeader.Opening, move.getPreviousOpeningHeader());
			} else {
				removeHeader(PgnHeader.Opening);
			}
		}
	}

	protected void rollbackEpMove(Move move) {
		int oppositeColor = getOppositeColor(getColorToMove());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		long captureBB = oppositeColor == WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);
		int captureSquare = bitscanForward(captureBB);

		xor(oppositeColor, move.getPiece(), fromToBB);
		xor(oppositeColor, fromToBB);
		setOccupiedBB(getOccupiedBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ captureBB);
		setOccupiedBB(getOccupiedBB() ^ captureBB);

		xor(getColorToMove(), move.getCapture(), captureBB);
		xor(getColorToMove(), captureBB);

		setPiece(move.getTo(), EMPTY);
		setPiece(move.getFrom(), PAWN);
		setPiece(captureSquare, PAWN);

		updateZobristEP(move, captureSquare);
		setEpSquareFromPreviousMove();
	}

	protected void rollbackNonEpNonCastlingMove(Move move) {
		int oppositeColor = getOppositeColor(move.getColor());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		xor(move.getColor(), fromToBB);

		if (move.isCapture()) {
			setOccupiedBB(getOccupiedBB() ^ fromBB);
			setEmptyBB(getEmptyBB() ^ fromBB);

			xor(oppositeColor, move.getCapture(), toBB);
			xor(oppositeColor, toBB);

			updateZobristPOCapture(move, oppositeColor);

		} else {
			setOccupiedBB(getOccupiedBB() ^ fromToBB);
			setEmptyBB(getEmptyBB() ^ fromToBB);

			updateZobristPONoCapture(move, oppositeColor);
		}

		if (move.isPromotion()) {
			xor(move.getColor(), move.getPiece(), fromBB);
			xor(move.getColor(), move.getPiecePromotedTo() & NOT_PROMOTED_MASK,
					toBB);

			// capture is handled in rollback.
			// promoted pieces never have a promote mask.
			// Promotions do not change drop counts.
			incrementPieceCount(move.getColor(), PAWN);
			decrementPieceCount(move.getColor(), move.getPiecePromotedTo());
		} else {
			xor(move.getColor(), move.getPiece(), fromToBB);
		}

		setPiece(move.getFrom(), move.getPieceWithPromoteMask());
		setPiece(move.getTo(), move.getCaptureWithPromoteMask());

		setEpSquareFromPreviousMove();
	}

	protected void setEpSquareFromPreviousMove() {
		switch (getMoveList().getSize()) {
		case 0:
			setEpSquare(getInitialEpSquare());
			break;
		default:
			setEpSquare(getMoveList().getLast().getEpSquare());
			break;
		}
	}

	/**
	 * Should be called before the move is made to update the san field.
	 */
	protected void setSan(Move move) {
		if (isSettingMoveSan() && move.getSan() == null) {
			// TO DO: possible add + or ++ for check/checkmate
			String shortAlgebraic = null;

			if (move.isDrop()) {
				move.setSan(PIECE_TO_SAN.charAt(move.getPiece()) + "@"
						+ getSan(move.getTo()));
			} else if (move.isCastleShort()) {
				shortAlgebraic = "O-O";
			} else if (move.isCastleLong()) {
				shortAlgebraic = "O-O-O";
			} else if (move.getPiece() == PAWN
					&& (move.getMoveCharacteristic() & Move.EN_PASSANT_CHARACTERISTIC) != 0) // e.p.
			// is
			// optional but
			// the x is
			// required.
			// (pawn eps
			// are never
			// unambiguous)
			{
				shortAlgebraic = SanUtils.squareToFileSan(move.getFrom()) + "x"
						+ SanUtils.squareToSan(move.getTo());
			} else if (move.getPiece() == PAWN && move.isCapture()) // Possible
			// formats ed
			// ed5 edQ
			// (pawn captures
			// can be
			// ambiguous)
			{
				int oppositeColorToMove = GameUtils
						.getOppositeColor(getColorToMove());
				long fromBB = getPieceBB(getColorToMove(), PAWN);
				int movesFound = 0;
				while (fromBB != 0) {
					int fromSquare = bitscanForward(fromBB);

					long allPawnCapturesBB = pawnCapture(getColorToMove(),
							getBitboard(fromSquare),
							getColorBB(oppositeColorToMove));

					while (allPawnCapturesBB != 0) {
						int toSquare = bitscanForward(allPawnCapturesBB);
						if (getFile(toSquare) == GameUtils
								.getFile(move.getTo())) {
							movesFound++;
						}
						allPawnCapturesBB = bitscanClear(allPawnCapturesBB);
					}
					fromBB = bitscanClear(fromBB);
				}

				if (movesFound > 1) {
					shortAlgebraic = SanUtils.squareToFileSan(move.getFrom())
							+ "x"
							+ SanUtils.squareToSan(move.getTo())
							+ (move.isPromotion() ? "="
									+ PIECE_TO_SAN.charAt(move
											.getPiecePromotedTo()) : "");
				} else {
					shortAlgebraic = SanUtils.squareToFileSan(move.getFrom())
							+ "x"
							+ SanUtils.squareToFileSan(move.getTo())
							+ (move.isPromotion() ? "="
									+ PIECE_TO_SAN.charAt(move
											.getPiecePromotedTo()) : "");
				}
			} else if (move.getPiece() == PAWN) // e4 (pawn moves
			// are never
			// ambiguous)
			{
				shortAlgebraic = SanUtils.squareToSan(move.getTo())
						+ (move.isPromotion() ? "="
								+ PIECE_TO_SAN
										.charAt(move.getPiecePromotedTo()) : "");
			} else {
				long fromBB = getPieceBB(getColorToMove(), move.getPiece());
				long toBB = getBitboard(move.getTo());

				int sameFilesFound = 0;
				int sameRanksFound = 0;
				int matchesFound = 0;

				if (move.getPiece() != KING) {
					while (fromBB != 0) {
						int fromSquare = bitscanForward(fromBB);
						long resultBB = 0;

						switch (move.getPiece()) {
						case KNIGHT:
							resultBB = knightMove(fromSquare) & toBB;
							break;
						case BISHOP:
							resultBB = diagonalMove(fromSquare, getEmptyBB(),
									getOccupiedBB())
									& getNotColorToMoveBB() & toBB;
							break;
						case ROOK:
							resultBB = orthogonalMove(fromSquare, getEmptyBB(),
									getOccupiedBB())
									& getNotColorToMoveBB() & toBB;
							break;
						case QUEEN:
							resultBB = orthogonalMove(fromSquare, getEmptyBB(),
									getOccupiedBB())
									& getNotColorToMoveBB()
									& toBB
									| diagonalMove(fromSquare, getEmptyBB(),
											getOccupiedBB())
									& getNotColorToMoveBB() & toBB;
							break;
						}

						if (resultBB != 0) {
							int toSquare = bitscanForward(resultBB);

							if (toSquare == move.getTo()) {
								matchesFound++;
								if (getFile(fromSquare) == GameUtils
										.getFile(move.getFrom())) {
									sameFilesFound++;
								}
								if (getRank(fromSquare) == GameUtils
										.getRank(move.getFrom())) {
									sameRanksFound++;
								}
							}
						}
						fromBB = bitscanClear(fromBB);
					}
				}

				shortAlgebraic = "" + PIECE_TO_SAN.charAt(move.getPiece());
				boolean hasHandledAmbiguity = false;
				if (sameRanksFound > 1) {
					shortAlgebraic += SanUtils.squareToFileSan(move.getFrom());
					hasHandledAmbiguity = true;
				}
				if (sameFilesFound > 1) {
					shortAlgebraic += SanUtils.squareToRankSan(move.getFrom());
					hasHandledAmbiguity = true;
				}
				if (matchesFound > 1 && !hasHandledAmbiguity) {
					shortAlgebraic += SanUtils.squareToFileSan(move.getFrom());
				}

				shortAlgebraic += (move.isCapture() ? "x" : "")
						+ SanUtils.squareToSan(move.getTo());
			}

			move.setSan(shortAlgebraic);
		}
	}

	protected void setState(int state) {
		this.state = state;
	}

	/**
	 * If the match list contains no ambiguity after taking disambiguity by
	 * check into consideration the move is returned. Otherwise an
	 * IllegalArgumentException is raised
	 */
	protected Move testForSanDisambiguationFromCheck(String shortAlgebraic,
			MoveList matches) throws IllegalArgumentException {
		Move result = null;
		if (matches.getSize() == 0) {
			throw new IllegalArgumentException("Invalid move " + shortAlgebraic
					+ "\n" + toString());
		} else if (matches.getSize() == 1) {
			result = matches.get(0);
		} else {
			// now do legality checking on whats left.
			int kingSquare = bitscanForward(getPieceBB(colorToMove, KING));
			int cachedColorToMove = colorToMove;
			int matchesCount = 0;

			if (kingSquare != EMPTY_SQUARE) { // Now trim illegals
				for (int i = 0; i < matches.getSize(); i++) {
					Move current = matches.get(i);

					// Needed for FR.
					if (current.isCastleLong() || current.isCastleShort()) {
						continue;
					}
					synchronized (this) {
						try {
							forceMove(current);
							if (current.getPiece() == KING) {
								int newKingCoordinates = GameUtils
										.bitscanForward(getPieceBB(
												cachedColorToMove, KING));
								if (!isInCheck(cachedColorToMove, GameUtils
										.getBitboard(newKingCoordinates))) {
									result = current;
									matchesCount++;
								} else {
								}
							} else {
								if (!isInCheck(cachedColorToMove,
										getBitboard(kingSquare))) {
									result = current;
									matchesCount++;
								} else {
								}
							}
							rollback();
						} catch (IllegalArgumentException ie) {
						}
					}
				}
			}

			if (matchesCount == 0) {
				throw new IllegalArgumentException("Invalid move "
						+ shortAlgebraic + "\n" + toString());
			} else if (matchesCount > 1) {
				throw new IllegalArgumentException("Ambiguous move "
						+ shortAlgebraic + "\n" + toString());
			}
		}
		return result;
	}

	/**
	 * Provided so it can be easily implemented for Fischer Random type of
	 * games.
	 */
	protected void updateCastlingRightsForNonEpNonCastlingMove(Move move) {
		switch (move.getPiece()) {
		case KING:
			setCastling(getColorToMove(), CASTLE_NONE);
			break;
		default:
			if (move.getPiece() == ROOK && move.getFrom() == SQUARE_A1
					&& getColorToMove() == WHITE || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_A1 && getColorToMove() == BLACK) {
				setCastling(WHITE, getCastling(WHITE) & CASTLE_SHORT);
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_H1
					&& getColorToMove() == WHITE || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_H1 && getColorToMove() == BLACK) {
				setCastling(WHITE, getCastling(WHITE) & CASTLE_LONG);
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_A8
					&& getColorToMove() == BLACK || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_A8 && getColorToMove() == WHITE) {
				setCastling(BLACK, getCastling(BLACK) & CASTLE_SHORT);
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_H8
					&& getColorToMove() == BLACK || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_H8 && getColorToMove() == WHITE) {
				setCastling(BLACK, getCastling(BLACK) & CASTLE_LONG);
			}
			break;
		}
	}

	protected void updateEcoHeaders(Move move) {
		if (isSettingEcoHeaders()) {
			move.setPreviousEcoHeader(getHeader(PgnHeader.ECO));
			move.setPreviousOpeningHeader(getHeader(PgnHeader.Opening));

			String ecoCode = EcoService.getInstance().getEco(this);
			String description = EcoService.getInstance().getLongDescription(
					this);
			if (StringUtils.isNotBlank(ecoCode)) {
				setHeader(PgnHeader.ECO, ecoCode);
			}
			if (StringUtils.isNotBlank(description)) {
				setHeader(PgnHeader.Opening, description);
			}

		}
	}

	protected void updateZobristDrop(Move move, int oppositeColor) {
		zobristPositionHash ^= zobrist(move.getColor(), move.getPiece()
				& NOT_PROMOTED_MASK, move.getTo());
	}

	protected void updateZobristEP(Move move, int captureSquare) {
		zobristPositionHash ^= zobrist(move.getColor(), PAWN, move.getFrom())
				^ zobrist(move.getColor(), PAWN, move.getTo())
				^ zobrist(move.getCaptureColor(), PAWN, captureSquare);
	}

	protected void updateZobristHash() {
		zobristGameHash = zobristPositionHash
				^ zobrist(getColorToMove(), getEpSquare(), getCastling(WHITE),
						getCastling(BLACK));
	}

	protected void updateZobristPOCapture(Move move, int oppositeColor) {
		zobristPositionHash ^= zobrist(move.getColor(),
				move.isPromotion() ? move.getPiecePromotedTo()
						& NOT_PROMOTED_MASK : move.getPiece()
						& NOT_PROMOTED_MASK, move.getTo())
				^ zobrist(oppositeColor, move.getCapture() & NOT_PROMOTED_MASK,
						move.getTo())
				^ zobrist(move.getColor(), move.getPiece() & NOT_PROMOTED_MASK,
						move.getFrom());
	}

	protected void updateZobristPOCastleKsideBlack() {
		zobristPositionHash ^= zobrist(BLACK, KING, SQUARE_E8)
				^ zobrist(BLACK, KING, SQUARE_G8)
				^ zobrist(BLACK, ROOK, SQUARE_H8)
				^ zobrist(BLACK, ROOK, SQUARE_F8);
	}

	protected void updateZobristPOCastleKsideWhite() {
		zobristPositionHash ^= zobrist(WHITE, KING, SQUARE_E1)
				^ zobrist(WHITE, KING, SQUARE_G1)
				^ zobrist(WHITE, ROOK, SQUARE_H1)
				^ zobrist(WHITE, ROOK, SQUARE_F1);
	}

	protected void updateZobristPOCastleQsideBlack() {
		zobristPositionHash ^= zobrist(BLACK, KING, SQUARE_E8)
				^ zobrist(BLACK, KING, SQUARE_C8)
				^ zobrist(BLACK, ROOK, SQUARE_A8)
				^ zobrist(BLACK, ROOK, SQUARE_D8);
	}

	protected void updateZobristPOCastleQsideWhite() {
		zobristPositionHash ^= zobrist(WHITE, KING, SQUARE_E1)
				^ zobrist(WHITE, KING, SQUARE_C1)
				^ zobrist(WHITE, ROOK, SQUARE_A1)
				^ zobrist(WHITE, ROOK, SQUARE_D1);
	}

	protected void updateZobristPONoCapture(Move move, int oppositeColor) {
		zobristPositionHash ^= zobrist(move.getColor(),
				move.isPromotion() ? move.getPiecePromotedTo()
						& NOT_PROMOTED_MASK : move.getPiece()
						& NOT_PROMOTED_MASK, move.getTo())
				^ zobrist(move.getColor(), move.getPiece() & NOT_PROMOTED_MASK,
						move.getFrom());
	}

	/**
	 * Exclusive bitwise ors the games piece bitboard with the specified
	 * bitboard.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The non-colored piece type.
	 * @param bb
	 *            The bitmap to XOR.
	 */
	protected void xor(int color, int piece, long bb) {
		pieceBB[color][piece] ^= bb;
	}

	/**
	 * Exclusive bitwise ors the games color bitboard with the specified
	 * bitboard.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param bb
	 *            The bitmap to XOR.
	 */
	protected void xor(int color, long bb) {
		colorBB[color] ^= bb;
	}

}
