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
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.Constants;
import raptor.swt.chess.Utils;

public class InactiveController extends ChessBoardController implements
		Constants, GameConstants {
	static final Log LOG = LogFactory.getLog(ChessBoardController.class);
	Random random = new SecureRandom();

	public InactiveController() {
	}

	@Override
	protected void adjustCoolbarToInitial() {
		board.addGameActionButtonsToCoolbar();
		board.addAutoPromoteRadioGroupToCoolbar();
		board.packCoolbar();
	}

	@Override
	public void adjustGameDescriptionLabel() {
		board.getGameDescriptionLabel().setText(
				"Inactive " + getGame().getEvent());
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (getGame().getPiece(squareId) == EMPTY) {
			return false;
		} else if (Utils.isPieceJailSquare(squareId)) {
			if (getGame().isInState(Game.DROPPABLE_STATE)) {
				int pieceType = Utils.pieceJailSquareToPiece(squareId);
				return (getGame().isWhitesMove()
						&& Utils.isWhitePiece(pieceType) || (!getGame()
						.isWhitesMove() && Utils.isBlackPiece(pieceType)));
			} else {
				return (getGame().isWhitesMove() && Utils.isWhitePiece(board
						.getSquare(squareId).getPiece()))
						|| (!getGame().isWhitesMove() && Utils
								.isBlackPiece(board.getSquare(squareId)
										.getPiece()));
			}
		}
		if (!Utils.isPieceJailSquare(squareId)) {
			return board.getSquare(squareId).getPiece() != GameConstants.EMPTY;
		}
		return false;
	}

	@Override
	public String getTitle() {
		return "Inactive(" + getGame().getId() + ")";
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
	public boolean isAbortable() {
		return false;
	}

	@Override
	public boolean isAdjournable() {
		return false;
	}

	@Override
	public boolean isAutoDrawable() {
		return false;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isCommitable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDrawable() {
		return false;
	}

	@Override
	public boolean isExaminable() {
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
	public boolean isPausable() {
		return false;
	}

	@Override
	public boolean isRematchable() {
		return false;
	}

	@Override
	public boolean isResignable() {
		return false;
	}

	@Override
	public boolean isRevertable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onClose() {
		return true;
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
		LOG.debug("moveCancelled" + board.getGame().getId() + " " + fromSquare
				+ " " + isDnd);
		board.unhighlightAllSquares();
		adjustToGameMove();
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		LOG.debug("moveInitiated" + board.getGame().getId() + " " + square
				+ " " + isDnd);

		board.unhighlightAllSquares();
		board.getSquare(square).highlight();
		if (isDnd && !Utils.isPieceJailSquare(square)) {
			board.getSquare(square).setPiece(GameConstants.EMPTY);
		}
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		LOG.debug("Move made " + board.getGame().getId() + " " + fromSquare
				+ " " + toSquare);
		board.unhighlightAllSquares();

		if (fromSquare == toSquare || Utils.isPieceJailSquare(toSquare)) {
			board.unhighlightAllSquares();
			adjustToGameMove();
			SoundService.getInstance().playSound("illegalMove");
			return;
		}

		Game game = board.getGame();
		Move move = null;

		if (Utils.isPieceJailSquare(fromSquare)) {
			if (game.isInState(Game.DROPPABLE_STATE)) {
				move = Utils.createDropMove(fromSquare, toSquare);
			} else {
				board.unhighlightAllSquares();
				adjustToGameMove();
				SoundService.getInstance().playSound("illegalMove");
				return;
			}
		} else if (GameUtils.isPromotion(board.getGame(), fromSquare, toSquare)) {
			move = new Move(fromSquare, toSquare, game.getPiece(fromSquare),
					game.getColorToMove(), game.getPiece(toSquare), board
							.getAutoPromoteSelection(), EMPTY,
					Move.PROMOTION_CHARACTERISTIC);
		} else {
			move = new Move(fromSquare, toSquare, game.getPiece(fromSquare),
					game.getColorToMove(), game.getPiece(toSquare));
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

	@Override
	public void userMiddleClicked(int square) {
		LOG.debug("On middle click " + board.getGame().getId() + " " + square);
		// Move[] moves = board.getGame().getLegalMoves().asArray();
		// List<Move> foundMoves = new ArrayList<Move>(5);
		// for (Move move : moves) {
		// if (move.getTo() == square) {
		// foundMoves.add(move);
		// }
		// }
		//
		// if (foundMoves.size() > 0) {
		// Move move = foundMoves.get(random.nextInt(foundMoves.size()));
		// board.getGame().move(move);
		// board.unhighlightAllSquares();
		// board.getSquare(move.getFrom()).highlight();
		// board.getSquare(move.getTo()).highlight();
		// adjustToGameMove();
		// }
	}

	@Override
	public void userRightClicked(int square) {
		LOG.debug("On right click " + board.getGame().getId() + " " + square);
	}
}
