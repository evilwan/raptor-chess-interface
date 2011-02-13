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
import static raptor.chess.util.GameUtils.getOppositeColor;
import static raptor.chess.util.GameUtils.kingMove;
import static raptor.chess.util.GameUtils.knightMove;
import static raptor.chess.util.GameUtils.moveOne;
import static raptor.chess.util.GameUtils.orthogonalMove;
import static raptor.chess.util.GameUtils.pawnCapture;
import static raptor.chess.util.GameUtils.pawnEpCapture;

import java.util.ArrayList;
import java.util.List;

import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;

/**
 * A chess game which follows FICS atomic rules. Type help atomic on fics for
 * the rules.
 */
public class AtomicGame extends ClassicGame {

	public AtomicGame() {
		super();
		setHeader(PgnHeader.Variant, Variant.atomic.name());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AtomicGame deepCopy(boolean ignoreHashes) {
		AtomicGame result = new AtomicGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * Overridden to allow for moves which explode the enemy king. If you are in
	 * check yet you can explode the enemy king, then exploding the enemy king
	 * is a legal move even if you are in check afterwards. {@inheritDoc}
	 */
	@Override
	public PriorityMoveList getLegalMoves() {
		PriorityMoveList result = getPseudoLegalMoves();

		for (int i = 0; i < result.getHighPrioritySize(); i++) {
			Move move = result.getHighPriority(i);
			forceMove(move);

			if (!isLegalPosition()) {
				if (move.getAtomicExplosionInfo() != null) {
					int kingExplosionCount = 0;
					boolean wasOppositeKingExploded = false;
					for (AtomicExplosionInfo info : move
							.getAtomicExplosionInfo()) {
						if (info.piece == KING) {
							kingExplosionCount++;
							if (info.color == getColorToMove()) {
								wasOppositeKingExploded = true;
							}
						}
					}

					if (kingExplosionCount > 0) {
						System.err.println("king explosion count = "
								+ kingExplosionCount + " "
								+ wasOppositeKingExploded);
					}

					if (wasOppositeKingExploded && kingExplosionCount == 1) {
					} else {
						result.removeHighPriority(i);
						i--;
					}
				} else {
					result.removeHighPriority(i);
					i--;
				}
			}

			rollback();
		}

		for (int i = 0; i < result.getLowPrioritySize(); i++) {
			Move move = result.getLowPriority(i);
			forceMove(move);

			if (!isLegalPosition()) {
				if (move.getAtomicExplosionInfo() != null) {
					int kingExplosionCount = 0;
					boolean wasOppositeKingExploded = false;
					for (AtomicExplosionInfo info : move
							.getAtomicExplosionInfo()) {
						if (info.piece == KING) {
							kingExplosionCount++;
							if (info.color == GameUtils
									.getOppositeColor(getColorToMove())) {
								wasOppositeKingExploded = true;
							}
						}
					}

					if (wasOppositeKingExploded && kingExplosionCount == 1) {
					} else {
						result.removeLowPriority(i);
						i--;
					}
				} else {
					result.removeLowPriority(i);
					i--;
				}
			}

			rollback();
		}

		return result;
	}

	/**
	 * Kings are allowed to touch. {@inheritDoc}
	 */
	@Override
	public boolean isInCheck(int color, long pieceBB) {
		long kingBB = pieceBB;
		if (kingBB == 0) {
			// If the king was just exploded he is not in check.
			return false;
		}
		int kingSquare = bitscanForward(kingBB);
		int oppositeColor = getOppositeColor(color);

		boolean result = !(pawnCapture(oppositeColor, getPieceBB(oppositeColor,
				PAWN), kingBB) == 0L
				&& (orthogonalMove(kingSquare, getEmptyBB(), getOccupiedBB()) & (getPieceBB(
						oppositeColor, ROOK) | getPieceBB(oppositeColor, QUEEN))) == 0L
				&& (diagonalMove(kingSquare, getEmptyBB(), getOccupiedBB()) & (getPieceBB(
						oppositeColor, BISHOP) | getPieceBB(oppositeColor,
						QUEEN))) == 0L && (knightMove(kingSquare) & getPieceBB(
				oppositeColor, KNIGHT)) == 0L);

		// You are not in check if the move would explode the opponents king.
		if (result) {
			// from square,piece,and color are irrelevant since color is -1.
			AtomicExplosionInfo[] infos = getAtomicExplosionInfo(kingSquare,
					-1, KING, color);
			for (AtomicExplosionInfo info : infos) {
				if (info.piece == KING) {
					result = false;
					break;
				}
			}
		}

		return result;

	}

	/**
	 * Overridden to allow a checkmate where a king explodes. {@inheritDoc}
	 */
	@Override
	public boolean isLegalPosition() {
		return !isInCheck(getOppositeColor(getColorToMove()));
	}

	/**
	 * Overridden to allow for moves which explode the enemy king. If you are in
	 * check yet you can explode the enemy king, then exploding the enemy king
	 * is a legal move even if you are in check afterwards. {@inheritDoc}
	 */
	@Override
	public boolean move(Move move) {
		// first make the move.
		forceMove(move);
		if (!isLegalPosition()) {
			if (move.getAtomicExplosionInfo() != null) {
				int kingExplosionCount = 0;
				boolean wasOppositeKingExploded = false;
				for (AtomicExplosionInfo info : move.getAtomicExplosionInfo()) {
					if (info.piece == KING) {
						kingExplosionCount++;
						if (info.color == getColorToMove()) {
							wasOppositeKingExploded = true;
						}
					}
				}

				if (wasOppositeKingExploded && kingExplosionCount == 1) {
				} else {
					rollback();
					return false;
				}
			} else {
				rollback();
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds the atomic move including the explosion info if its a capture.
	 * 
	 * @param fromSquare
	 *            THe from square.
	 * @param toSquare
	 *            The top square
	 * @param colorToMove
	 *            The color moving.
	 * @param contents
	 *            The captured piece.
	 * @param moves
	 */
	protected void addAtomicMove(int fromSquare, int toSquare, int colorToMove,
			int contents, PriorityMoveList moves) {
		if (contents == EMPTY) {
			addMove(new Move(fromSquare, toSquare,
					getPieceWithPromoteMask(fromSquare), getColorToMove(),
					contents), moves);
		} else {
			Move move = new Move(fromSquare, toSquare,
					getPieceWithPromoteMask(fromSquare), getColorToMove(),
					contents);
			move.setAtomicExplosionInfo(getAtomicExplosionInfo(toSquare,
					fromSquare, getPieceWithPromoteMask(fromSquare),
					getColorToMove()));
			addMove(move, moves);
		}
	}

	/**
	 * Adds the atomic move including the explosion info if its a capture.
	 * 
	 * @param fromSquare
	 *            THe from square.
	 * @param toSquare
	 *            The top square
	 * @param colorToMove
	 *            The color moving.
	 * @param contents
	 *            The captured piece.
	 * @param moves
	 */
	protected void addAtomicPromotionMove(int fromSquare, int toSquare,
			int colorToMove, int contents, int promotedPiece,
			PriorityMoveList moves) {
		if (contents == EMPTY) {
			addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
					getPieceWithPromoteMask(toSquare), promotedPiece,
					EMPTY_SQUARE, Move.PROMOTION_CHARACTERISTIC), moves);
		} else {
			Move move = new Move(fromSquare, toSquare, PAWN, getColorToMove(),
					getPieceWithPromoteMask(toSquare), promotedPiece,
					EMPTY_SQUARE, Move.PROMOTION_CHARACTERISTIC);
			move.setAtomicExplosionInfo(getAtomicExplosionInfo(toSquare,
					fromSquare, PAWN, getColorToMove()));
			addMove(move, moves);
		}
	}

	/**
	 * Adds the explosion info for a candidate square if its a valid square and
	 * contains a piece other than a pawn.
	 */
	protected void addExplosionInfo(int rank, int file, int fromSquare,
			List<AtomicExplosionInfo> explosionInfo) {
		if (GameUtils.isInBounds(rank, file)) {
			int square = GameUtils.getSquare(rank, file);
			if (square != fromSquare && board[square] != EMPTY
					&& board[square] != PAWN) {
				AtomicExplosionInfo info = new AtomicExplosionInfo();
				info.color = (getColorBB(WHITE) & getBitboard(square)) != 0 ? WHITE
						: BLACK;
				info.piece = board[square];
				info.square = square;
				explosionInfo.add(info);
			}
		}
	}

	/**
	 * Overridden to handle explosions.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void generatePseudoBishopMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), BISHOP);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = diagonalMove(fromSquare, getEmptyBB(), getOccupiedBB())
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);

				addAtomicMove(fromSquare, toSquare, getColorToMove(), contents,
						moves);

				toBB = bitscanClear(toBB);
			}
			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
	protected void generatePseudoKingMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), KING);
		int fromSquare = bitscanForward(fromBB);
		long toBB = kingMove(fromSquare) & getNotColorToMoveBB();

		generatePseudoKingCastlingMoves(fromBB, moves);

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);

			int contents = getPieceWithPromoteMask(toSquare);

			// Kings cant capture pieces in atomic.
			if (contents == EMPTY) {
				addMove(new Move(fromSquare, toSquare, KING, getColorToMove(),
						contents), moves);
			}
			toBB = bitscanClear(toBB);
			toSquare = bitscanForward(toBB);
		}
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
	protected void generatePseudoKnightMoves(PriorityMoveList moves) {

		long fromBB = getPieceBB(getColorToMove(), KNIGHT);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = knightMove(fromSquare) & getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);
				int contents = getPieceWithPromoteMask(toSquare);

				addAtomicMove(fromSquare, toSquare, getColorToMove(), contents,
						moves);

				toBB = bitscanClear(toBB);
				toSquare = bitscanForward(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
	protected void generatePseudoPawnCaptures(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {

		long toBB = pawnCapture(getColorToMove(), fromBB,
				getColorBB(oppositeColor));

		while (toBB != 0L) {
			int toSquare = bitscanForward(toBB);
			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addAtomicPromotionMove(fromSquare, toSquare, getColorToMove(),
						getPieceWithPromoteMask(toSquare), KNIGHT, moves);
				addAtomicPromotionMove(fromSquare, toSquare, getColorToMove(),
						getPieceWithPromoteMask(toSquare), BISHOP, moves);
				addAtomicPromotionMove(fromSquare, toSquare, getColorToMove(),
						getPieceWithPromoteMask(toSquare), QUEEN, moves);
				addAtomicPromotionMove(fromSquare, toSquare, getColorToMove(),
						getPieceWithPromoteMask(toSquare), ROOK, moves);
			} else {
				addAtomicMove(fromSquare, toSquare, getColorToMove(),
						getPieceWithPromoteMask(toSquare), moves);
			}
			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
	protected void generatePseudoPawnEPCaptures(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {
		if (getEpSquare() != EMPTY) {

			long toBB = pawnEpCapture(getColorToMove(), fromBB, getPieceBB(
					oppositeColor, PAWN), getBitboard(getEpSquare()));

			if (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				Move move = new Move(fromSquare, toSquare, PAWN,
						getColorToMove(), PAWN, EMPTY, EMPTY_SQUARE,
						Move.EN_PASSANT_CHARACTERISTIC);
				move.setAtomicExplosionInfo(getAtomicExplosionInfo(toSquare,
						fromSquare, PAWN, getColorToMove()));
				addMove(move, moves);
			}
		}
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
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
				addAtomicMove(fromSquare, toSquare, getColorToMove(), contents,
						moves);

				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
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
				addAtomicMove(fromSquare, toSquare, getColorToMove(), contents,
						moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Returns an array of AtomicExplosionInfo for the capture.
	 * 
	 * @param toSquare
	 *            The target capture square.
	 * @param fromSquare
	 *            The square the piece moved from. Can be -1 to ignore from
	 *            square (i.e. when using this method in isInCheck).
	 * @param pieceMoving
	 *            THe piece moving
	 * @param pieceMovingColor
	 *            THe color of the piece moving.
	 * @return An array of AtomicExplosionInfo detailing all of the exploded
	 *         pieces. THe piece being captured isn't exploded, but the piece
	 *         moving is.
	 */
	protected AtomicExplosionInfo[] getAtomicExplosionInfo(int toSquare,
			int fromSquare, int pieceMoving, int pieceMovingColor) {
		// This should be rewritten if its ever used for a BOT using a list is
		// not efficient.
		List<AtomicExplosionInfo> explosionInfo = new ArrayList<AtomicExplosionInfo>(
				8);

		// Check all 8 squares around to square.
		// If the piece is not a pawn explode it.
		int toSquareRank = GameUtils.getRank(toSquare);
		int toSquareFile = GameUtils.getFile(toSquare);

		addExplosionInfo(toSquareRank, toSquareFile - 1, fromSquare,
				explosionInfo);
		addExplosionInfo(toSquareRank, toSquareFile + 1, fromSquare,
				explosionInfo);
		addExplosionInfo(toSquareRank + 1, toSquareFile - 1, fromSquare,
				explosionInfo);
		addExplosionInfo(toSquareRank + 1, toSquareFile + 1, fromSquare,
				explosionInfo);
		addExplosionInfo(toSquareRank + 1, toSquareFile, fromSquare,
				explosionInfo);
		addExplosionInfo(toSquareRank + -1, toSquareFile - 1, fromSquare,
				explosionInfo);
		addExplosionInfo(toSquareRank + -1, toSquareFile + 1, fromSquare,
				explosionInfo);
		addExplosionInfo(toSquareRank + -1, toSquareFile, fromSquare,
				explosionInfo);

		if (fromSquare != -1) {
			// Explode the piece moving.
			AtomicExplosionInfo info = new AtomicExplosionInfo();
			info.color = pieceMovingColor;
			info.square = toSquare;
			info.piece = pieceMoving;
			explosionInfo.add(info);
		}
		return explosionInfo.toArray(new AtomicExplosionInfo[0]);
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
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
		setPiece(move.getTo(), EMPTY);
		setPiece(captureSquare, EMPTY);

		// Don't decrement/increment for piece captured. That will be done in
		// forceMove.
		if (move.getAtomicExplosionInfo() != null) {
			for (AtomicExplosionInfo info : move.getAtomicExplosionInfo()) {
				long squareBB = getBitboard(info.square);
				xor(info.color, info.piece & NOT_PROMOTED_MASK,
						getBitboard(info.square));
				xor(info.color, squareBB);
				setPiece(info.square, EMPTY);
				decrementPieceCount(info.color, info.piece);
				incrementDropCount(info.color, info.piece);

				setOccupiedBB(getOccupiedBB() ^ squareBB);
				setEmptyBB(getEmptyBB() ^ squareBB);
			}
		}

		updateZobristEP(move, captureSquare);
		setEpSquare(EMPTY_SQUARE);
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
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

			xor(move.getColor(), move.getPiece(), fromToBB);
			setPiece(move.getTo(), EMPTY);
			setPiece(move.getFrom(), EMPTY);

			// Don't decrement/increment for piece captured. That will be done
			// in
			// forceMove.
			if (move.getAtomicExplosionInfo() != null) {
				for (AtomicExplosionInfo info : move.getAtomicExplosionInfo()) {
					long squareBB = getBitboard(info.square);
					xor(info.color, info.piece & NOT_PROMOTED_MASK,
							getBitboard(info.square));
					xor(info.color, squareBB);
					setPiece(info.square, EMPTY);
					decrementPieceCount(info.color, info.piece);
					incrementDropCount(info.color, info.piece);

					setOccupiedBB(getOccupiedBB() ^ squareBB);
					setEmptyBB(getEmptyBB() ^ squareBB);
				}
			}

		} else {
			setOccupiedBB(getOccupiedBB() ^ fromToBB);
			setEmptyBB(getEmptyBB() ^ fromToBB);
			updateZobristPONoCapture(move, oppositeColor);
		}

		if (move.isPromotion() && !move.isCapture()) {
			if (move.isCapture()) {
				xor(move.getColor(), move.getPiece(), fromBB);
			}

			xor(move.getColor(), move.getPiecePromotedTo() & NOT_PROMOTED_MASK,
					toBB);

			setPiece(move.getTo(), move.getPiecePromotedTo() | PROMOTED_MASK);
			setPiece(move.getFrom(), EMPTY);

			// capture is handled in forceMove.
			// promoted piece never has a promote mask only captures do.
			// Promotes do not effect drop pieces.
			decrementPieceCount(getColorToMove(), PAWN);
			incrementPieceCount(getColorToMove(), move.getPiecePromotedTo());
		} else if (!move.isCapture()) {
			xor(move.getColor(), move.getPiece(), fromToBB);

			setPiece(move.getTo(), move.getPieceWithPromoteMask());
			setPiece(move.getFrom(), EMPTY);
		}

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

		setEpSquare(move.getEpSquare());
	}

	/**
	 * Overridden to handle rolling back explosions. {@inheritDoc}
	 */
	@Override
	protected void rollbackEpMove(Move move) {
		int oppositeColor = getOppositeColor(getColorToMove());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		// Don't decrement/increment for piece captured. That will be done in
		// forceMove.
		if (move.getAtomicExplosionInfo() != null) {
			for (AtomicExplosionInfo info : move.getAtomicExplosionInfo()) {
				long squareBB = getBitboard(info.square);
				xor(info.color, info.piece & NOT_PROMOTED_MASK,
						getBitboard(info.square));
				xor(info.color, squareBB);
				setPiece(info.square, info.piece);
				incrementPieceCount(info.color, info.piece);
				decrementDropCount(info.color, info.piece);

				setOccupiedBB(getOccupiedBB() ^ squareBB);
				setEmptyBB(getEmptyBB() ^ squareBB);
			}
		}

		long captureBB = oppositeColor == WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);
		int captureSquare = bitscanForward(captureBB);

		xor(oppositeColor, move.getPiece(), fromToBB);
		xor(oppositeColor, fromToBB);
		setOccupiedBB(getOccupiedBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ fromToBB);

		xor(getColorToMove(), move.getCapture(), captureBB);
		xor(getColorToMove(), captureBB);

		setPiece(move.getTo(), EMPTY);
		setPiece(move.getFrom(), PAWN);
		setPiece(captureSquare, PAWN);

		updateZobristEP(move, captureSquare);
		setEpSquareFromPreviousMove();
	}

	/**
	 * Overridden to handle rolling back explosions. {@inheritDoc}
	 */
	@Override
	protected void rollbackNonEpNonCastlingMove(Move move) {
		int oppositeColor = getOppositeColor(move.getColor());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		// Don't decrement/increment for piece captured. That will be done in
		// forceMove.
		if (move.getAtomicExplosionInfo() != null) {
			for (AtomicExplosionInfo info : move.getAtomicExplosionInfo()) {
				long squareBB = getBitboard(info.square);
				xor(info.color, info.piece & NOT_PROMOTED_MASK,
						getBitboard(info.square));
				xor(info.color, squareBB);
				setPiece(info.square, info.piece);
				incrementPieceCount(info.color, info.piece);
				decrementDropCount(info.color, info.piece);
				setOccupiedBB(getOccupiedBB() ^ squareBB);
				setEmptyBB(getEmptyBB() ^ squareBB);
			}
		}

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
}
