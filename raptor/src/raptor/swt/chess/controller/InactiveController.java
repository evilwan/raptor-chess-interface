package raptor.swt.chess.controller;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.service.SoundService;
import raptor.swt.chess.BoardConstants;
import raptor.swt.chess.BoardUtils;
import raptor.swt.chess.ChessBoardController;

/**
 * This controller is used when a game is no longer active. It allows the user
 * to play around with the position and traverser the move list. However it is
 * not backed by a connector, so the users actions do not do anything to a
 * connector.
 */
public class InactiveController extends ChessBoardController implements
		BoardConstants, GameConstants {
	static final Log LOG = LogFactory.getLog(ChessBoardController.class);
	Random random = new SecureRandom();

	public InactiveController(Game game) {
		super(game);
	}

	@Override
	protected void adjustCoolbarToInitial() {
		if (!isBeingReparented()) {
			board.addGameActionButtonsToCoolbar();
			board.addAutoPromoteRadioGroupToCoolbar();
			board.packCoolbar();
		}
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isBeingReparented()) {
			board.getGameDescriptionLabel().setText(
					"Inactive " + getGame().getEvent());
		}
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (!isBeingReparented()) {
			if (getGame().getPiece(squareId) == EMPTY) {
				return false;
			} else if (BoardUtils.isPieceJailSquare(squareId)) {
				if (getGame().isInState(Game.DROPPABLE_STATE)) {
					int pieceType = BoardUtils.pieceJailSquareToPiece(squareId);
					return (getGame().isWhitesMove()
							&& BoardUtils.isWhitePiece(pieceType) || (!getGame()
							.isWhitesMove() && BoardUtils
							.isBlackPiece(pieceType)));
				} else {
					return (getGame().isWhitesMove() && BoardUtils
							.isWhitePiece(board.getSquare(squareId).getPiece()))
							|| (!getGame().isWhitesMove() && BoardUtils
									.isBlackPiece(board.getSquare(squareId)
											.getPiece()));
				}
			}
			if (!BoardUtils.isPieceJailSquare(squareId)) {
				return board.getSquare(squareId).getPiece() != GameConstants.EMPTY;
			}
		}
		return false;
	}

	@Override
	public String getTitle() {
		return "Inactive";
	}

	@Override
	public void init() {
		super.init();
		if (getGame().isInState(Game.DROPPABLE_STATE)) {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? true : false);
		} else {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? false : true);
		}
	}

	@Override
	public boolean isAutoDrawable() {
		return false;
	}

	@Override
	public boolean isCommitable() {
		return false;
	}

	@Override
	public boolean isMoveListTraversable() {
		return true;
	}

	@Override
	public boolean isNavigatable() {
		return true;
	}

	@Override
	public boolean isRevertable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onPlayGameEndSound() {
	}

	@Override
	protected void onPlayGameStartSound() {
	}

	@Override
	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		if (!isBeingReparented()) {
			LOG.debug("moveCancelled" + getGame().getId() + " " + fromSquare
					+ " " + isDnd);
			board.unhighlightAllSquares();
			adjustToGameMove();
		}
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		if (!isBeingReparented()) {
			LOG.debug("moveInitiated" + getGame().getId() + " " + square + " "
					+ isDnd);

			board.unhighlightAllSquares();
			board.getSquare(square).highlight();
			if (isDnd && !BoardUtils.isPieceJailSquare(square)) {
				board.getSquare(square).setPiece(GameConstants.EMPTY);
			}
		}
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (!isBeingReparented()) {
			LOG.debug("Move made " + getGame().getId() + " " + fromSquare + " "
					+ toSquare);
			board.unhighlightAllSquares();

			if (fromSquare == toSquare
					|| BoardUtils.isPieceJailSquare(toSquare)) {
				board.unhighlightAllSquares();
				adjustToGameMove();
				SoundService.getInstance().playSound("illegalMove");
				return;
			}

			Game game = getGame();
			Move move = null;

			if (BoardUtils.isPieceJailSquare(fromSquare)) {
				if (game.isInState(Game.DROPPABLE_STATE)) {
					move = BoardUtils.createDropMove(fromSquare, toSquare);
				} else {
					board.unhighlightAllSquares();
					adjustToGameMove();
					SoundService.getInstance().playSound("illegalMove");
					return;
				}
			} else if (GameUtils.isPromotion(getGame(), fromSquare, toSquare)) {
				move = new Move(fromSquare, toSquare,
						game.getPiece(fromSquare), game.getColorToMove(), game
								.getPiece(toSquare), board
								.getAutoPromoteSelection(), EMPTY,
						Move.PROMOTION_CHARACTERISTIC);
			} else {
				move = new Move(fromSquare, toSquare,
						game.getPiece(fromSquare), game.getColorToMove(), game
								.getPiece(toSquare));
			}

			try {
				game.move(move);
				getBoard().getSquare(fromSquare).highlight();
				getBoard().getSquare(toSquare).highlight();
			} catch (IllegalArgumentException iae) {
				board.unhighlightAllSquares();
			} finally {
				adjustToGameMove();
			}
		}
	}

	@Override
	public void userMiddleClicked(int square) {
		LOG.debug("On middle click " + getGame().getId() + " " + square);
	}

	@Override
	public void userRightClicked(int square) {
		LOG.debug("On right click " + getGame().getId() + " " + square);
	}
}
