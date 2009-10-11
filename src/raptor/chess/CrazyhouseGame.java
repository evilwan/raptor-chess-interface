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
package raptor.chess;

import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.chess.util.SanUtils;
import raptor.chess.util.SanUtils.SanValidations;

/**
 * Follows FICS crazyhosue rules.
 */
public class CrazyhouseGame extends ClassicGame {
	public CrazyhouseGame() {
		setHeader(PgnHeader.Variant, Variant.crazyhouse.name());
		addState(Game.DROPPABLE_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CrazyhouseGame deepCopy(boolean ignoreHashes) {
		CrazyhouseGame result = new CrazyhouseGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * Generates all of the pseudo legal drop moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoDropMoves(PriorityMoveList moves) {

		if (getDropCount(getColorToMove(), PAWN) > 0) {

			long emptyBB = getEmptyBB() & NOT_RANK1 & NOT_RANK8;
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, PAWN, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), KNIGHT) > 0) {

			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, KNIGHT, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), BISHOP) > 0) {

			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, BISHOP, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), ROOK) > 0) {

			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, ROOK, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), QUEEN) > 0) {

			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, QUEEN, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}
	}

	/**
	 * Overridden to invoke genDropMoves as well as super.getPseudoLegalMoves.
	 * 
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public PriorityMoveList getPseudoLegalMoves() {
		PriorityMoveList result = super.getPseudoLegalMoves();
		generatePseudoDropMoves(result);
		return result;
	}

	/**
	 * Overridden to add in drops and remove all drop moves from pseudoLegals.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected Move makeSanMoveOverride(String shortAlgebraic,
			SanValidations validations, Move[] pseudoLegals) {
		Move result = null;
		if (SanUtils.isValidDropStrict(validations.getStrictSan())) {
			for (Move move : getPseudoLegalMoves().asArray()) {
				if ((move.getMoveCharacteristic() & Move.DROP_CHARACTERISTIC) != 0
						&& move.getPiece() == SanUtils.sanToPiece(validations
								.getStrictSan().charAt(0))
						&& move.getTo() == GameUtils.getSquare(RANK_FROM_SAN
								.indexOf(validations.getStrictSan().charAt(3)),
								FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(2)))) {
					result = move;
					move.setSan(shortAlgebraic);
					break;
				}
			}
		} else {
			for (int i = 0; i < pseudoLegals.length; i++) {
				if (pseudoLegals[i].isDrop()) {
					pseudoLegals[i] = null;
				}
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return super.toString() + "\n" + getDropCountsString();
	}

}
