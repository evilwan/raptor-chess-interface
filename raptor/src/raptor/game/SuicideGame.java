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
package raptor.game;

import static raptor.game.util.GameUtils.bitscanClear;
import static raptor.game.util.GameUtils.bitscanForward;
import static raptor.game.util.GameUtils.kingMove;
import static raptor.game.util.GameUtils.pawnCapture;
import static raptor.game.util.GameUtils.pawnSinglePush;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.util.GameUtils;
import raptor.game.util.SanUtil;
import raptor.game.util.SanUtil.SanValidations;

public class SuicideGame extends Game {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(SuicideGame.class);

	public SuicideGame() {
		setType(Type.SUICIDE);
	}

	/**
	 * @param ignoreHashes
	 *            Whether to include copying hash tables.
	 * @return An deep clone copy of this Game object.
	 */
	@Override
	public Game deepCopy(boolean ignoreHashes) {
		SuicideGame result = new SuicideGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * Castling is'nt permitted in suicide. This method is overridden to remove
	 * it.
	 * 
	 * @param moves
	 *            A move list.
	 */
	@Override
	public void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
	}

	/**
	 * There can be more than one king in suicide. So have to override
	 * generatePseudoKingMove to check all kings.
	 */
	@Override
	public void generatePseudoKingMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), KING);
		while (fromBB != 0) {
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
			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Pawns can promote to a king in suicide. This method is overridden to
	 * supply that functionality.
	 * 
	 * @param moves
	 *            A move list.
	 */
	@Override
	public void generatePseudoPawnCaptures(int fromSquare, long fromBB,
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
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), KING, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare)), moves);
			}
			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * Pawns can promote to a king in suicide. This method is overridden to
	 * supply that functionality.
	 * 
	 * @param moves
	 *            A move list.
	 */
	@Override
	public void generatePseudoPawnSinglePush(int fromSquare, long fromBB,
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
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, KING, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);

			} else {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY), moves);
			}
			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * In suicide you must make a capture if its possible. This method narrows
	 * down the list to only captures if there is one possible.
	 */
	@Override
	public PriorityMoveList getLegalMoves() {
		PriorityMoveList result = getPseudoLegalMoves();
		boolean hasCapture = false;
		for (int i = 0; i < result.getHighPrioritySize(); i++) {
			setSan(result.getHighPriority(i));
			if (result.getHighPriority(i).isCapture()) {
				hasCapture = true;
				break;
			}
		}
		if (!hasCapture) {
			for (int i = 0; i < result.getLowPrioritySize(); i++) {
				setSan(result.getLowPriority(i));
				if (result.getLowPriority(i).isCapture()) {
					hasCapture = true;
					break;
				}
			}
		}

		if (hasCapture) {
			PriorityMoveList onlyCaptures = new PriorityMoveList();
			for (int i = 0; i < result.getHighPrioritySize(); i++) {
				if (result.getHighPriority(i).isCapture()) {
					onlyCaptures.appendHighPriority(result.getHighPriority(i));
				}
			}
			for (int i = 0; i < result.getLowPrioritySize(); i++) {
				if (result.getLowPriority(i).isCapture()) {
					onlyCaptures.appendHighPriority(result.getHighPriority(i));
				}
			}
			return onlyCaptures;
		} else {
			return result;
		}
	}

	@Override
	public boolean isLegalPosition() {
		return true;
	}

	/**
	 * Needs to be overridden to support promotions to king.
	 */
	@Override
	public Move makeSanMoveOverride(String shortAlgebraic,
			SanValidations validations, Move[] pseudoLegals) {
		String san = validations.getStrictSan();

		Move result = null;

		if (san.charAt(san.length() - 1) == 'K') {
			MoveList matches = new MoveList(10);

			if (SanUtil.isValidSuicidePPromotionStrict(san)) {
				int toSquare = GameUtils.rankFileToSquare(RANK_FROM_SAN
						.indexOf(san.charAt(1)), FILE_FROM_SAN.indexOf(san
						.charAt(0)));

				for (Move move : pseudoLegals) {
					if (move.getTo() == toSquare && move.getPiece() == PAWN
							&& move.isPromotion()
							&& move.getPiecePromotedTo() == KING) {
						matches.append(move);
					}
				}
			} else if (SanUtil.isValidSuicidePxPromotionStrict(san)) {
				int fromFile = FILE_FROM_SAN.indexOf(san.charAt(0));
				int toFile = FILE_FROM_SAN.indexOf(san.charAt(1));

				for (Move move : pseudoLegals) {
					if (GameUtils.getFile(move.getTo()) == toFile
							&& GameUtils.getFile(move.getFrom()) == fromFile
							&& move.getPiece() == PAWN && move.isPromotion()
							&& move.getPiecePromotedTo() == KING) {
						matches.append(move);
					}
				}
			} else if (SanUtil.isValidSuicideAmbigPxPromotion(san)) {
				int fromFile = FILE_FROM_SAN.indexOf(san.charAt(0));
				int toSquare = GameUtils.rankFileToSquare(RANK_FROM_SAN
						.indexOf(san.charAt(2)), FILE_FROM_SAN.indexOf(san
						.charAt(1)));
				for (Move move : pseudoLegals) {
					if (move.getTo() == toSquare
							&& GameUtils.getFile(move.getFrom()) == fromFile
							&& move.getPiece() == PAWN && move.isPromotion()
							&& move.getPiecePromotedTo() == KING) {
						matches.append(move);
					}
				}
			}
			result = testForSanDisambiguationFromCheck(shortAlgebraic, matches);
			if (result == null) {
				throw new IllegalArgumentException("Illegal move: "
						+ shortAlgebraic);
			} else {
				result.setSan(shortAlgebraic);
			}
		}
		return result;
	}

	/**
	 *There is no disambiguation from check in suicide. So just throw an
	 * exception on more than 1 match.
	 */
	@Override
	protected Move testForSanDisambiguationFromCheck(String shortAlgebraic,
			MoveList matches) throws IllegalArgumentException {
		Move result = null;
		if (matches.getSize() == 0) {
			throw new IllegalArgumentException("Invalid move " + shortAlgebraic
					+ "\n" + toString());
		} else if (matches.getSize() == 1) {
			result = matches.get(0);
		} else {
			throw new IllegalArgumentException("Ambiguous move "
					+ shortAlgebraic + "\n" + toString());
		}
		return result;
	}
}
