package raptor.swt.chess.controller;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.Constants;
import raptor.swt.chess.Utils;

public class FreeFormController extends ChessBoardController implements
		Constants, GameConstants {
	static final Log LOG = LogFactory.getLog(ChessBoardController.class);
	Random random = new SecureRandom();

	public FreeFormController() {
	}

	@Override
	protected void adjustCoolbarToInitial() {
		board.addGameActionButtonsToCoolbar();
		board.addAutoPromoteRadioGroupToCoolbar();
		board.addScripterCoolbar();

		board.packCoolbar();
	}

	@Override
	public void adjustGameDescriptionLabel() {
		board.getGameDescriptionLabel().setText(
				"Freestyling " + getGame().getEvent());
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (!Utils.isPieceJailSquare(squareId)) {
			return board.getSquare(squareId).getPiece() != GameConstants.EMPTY;
		}
		return false;
	}

	@Override
	public String getTitle() {
		return "Inactive(" + getGame().getId() + " " + getGame().getWhiteName()
				+ " vs " + getGame().getBlackName() + ")";
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
		// TODO Auto-generated method stub
		return true;
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
		if (isDnd) {
			board.getSquare(square).setPiece(GameConstants.EMPTY);
		}

	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		LOG.debug("Move made " + board.getGame().getId() + " " + fromSquare
				+ " " + toSquare);
		board.unhighlightAllSquares();
		try {
			Move move = board.getGame().makeMove(fromSquare, toSquare);
			board.getSquare(move.getFrom()).highlight();
			board.getSquare(move.getTo()).highlight();
			adjustToGameMove();
		} catch (IllegalArgumentException iae) {
			LOG.info("Move was illegal " + fromSquare + " " + toSquare);
			board.unhighlightAllSquares();
			adjustToGameMove();
		}
	}

	@Override
	public void userMiddleClicked(int square) {
		LOG.debug("On middle click " + board.getGame().getId() + " " + square);
		Move[] moves = board.getGame().getLegalMoves().asArray();
		List<Move> foundMoves = new ArrayList<Move>(5);
		for (Move move : moves) {
			if (move.getTo() == square) {
				foundMoves.add(move);
			}
		}

		if (foundMoves.size() > 0) {
			Move move = foundMoves.get(random.nextInt(foundMoves.size()));
			board.getGame().move(move);
			board.unhighlightAllSquares();
			board.getSquare(move.getFrom()).highlight();
			board.getSquare(move.getTo()).highlight();
			adjustToGameMove();
		}
	}

	@Override
	public void userRightClicked(int square) {
		LOG.debug("On right click " + board.getGame().getId() + " " + square);
	}
}
