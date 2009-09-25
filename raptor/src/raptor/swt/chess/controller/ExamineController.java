package raptor.swt.chess.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.Utils;

public class ExamineController extends ChessBoardController {
	static final Log LOG = LogFactory.getLog(ExamineController.class);
	protected GameServiceListener listener = new GameServiceAdapter() {

		public void gameInactive(Game game) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							examinePositionUpdate();
						} catch (Throwable t) {
							board.getConnector().onError(
									"ExamineController.onGameInactive", t);
						} finally {
							board.getConnector().getGameService()
									.removeGameServiceListener(listener);
						}
					}
				});
			}
		}

		public void gameStateChanged(Game game, final boolean isNewMove) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							examinePositionUpdate();
						} catch (Throwable t) {
							board.getConnector().onError(
									"ExamineController.gameStateChanged", t);
						}
					}
				});
			}
		}

		@Override
		public void illegalMove(Game game, final String move) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							examineOnIllegalMove(move);
						} catch (Throwable t) {
							board.getConnector().onError(
									"ExamineController.illegalMove", t);
						}
					}
				});
			}
		}

	};

	public ExamineController() {
	}

	@Override
	public void dispose() {
		board.getConnector().getGameService().removeGameServiceListener(
				listener);
		super.dispose();
	}

	public void examinePositionUpdate() {
		LOG.info("examinePositionUpdate " + getGame().getId() + " ...");
		long startTime = System.currentTimeMillis();
		adjustNameRatingLabels();
		adjustGameDescriptionLabel();
		adjustToGameChangeNotInvolvingMove();
		adjustBoardToGame(getGame());
		adjustPieceJailFromGame(getGame());
		adjustNavButtonEnabledState();
		board.forceUpdate();
		onPlayMoveSound();
		board.unhighlightAllSquares();
		LOG.info("examinePositionUpdate in " + getGame().getId() + "  "
				+ (System.currentTimeMillis() - startTime));
	}

	public void examineOnIllegalMove(String move) {
		LOG.info("examineOnIllegalMove " + getGame().getId() + " ...");
		long startTime = System.currentTimeMillis();
		SoundService.getInstance().playSound("illegalMove");
		board.getStatusLabel().setText("Illegal Move: " + move);
		board.unhighlightAllSquares();
		LOG.info("examineOnIllegalMove in " + getGame().getId() + "  "
				+ (System.currentTimeMillis() - startTime));
	}

	protected void adjustClockColors() {
		if (getGame().getColorToMove() == WHITE) {
			board.getWhiteClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
			board.getBlackClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
		} else {
			board.getBlackClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
			board.getWhiteClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
		}
	}

	@Override
	protected void adjustCoolbarToInitial() {
		board.addGameActionButtonsToCoolbar();
		board.addAutoPromoteRadioGroupToCoolbar();
		board.packCoolbar();
	}

	protected void adjustNavButtonEnabledState() {
		if (board.getGame().isInState(Game.ACTIVE_STATE)) {
			board.setCoolBarButtonEnabled(true, ChessBoard.FIRST_NAV);
			board.setCoolBarButtonEnabled(true, ChessBoard.LAST_NAV);
			board.setCoolBarButtonEnabled(true, ChessBoard.NEXT_NAV);
			board.setCoolBarButtonEnabled(true, ChessBoard.BACK_NAV);
			board.setCoolBarButtonEnabled(true, ChessBoard.COMMIT_NAV);
			board.setCoolBarButtonEnabled(true, ChessBoard.REVERT_NAV);
		} else {
			board.setCoolBarButtonEnabled(false, ChessBoard.FIRST_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.LAST_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.NEXT_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.BACK_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.COMMIT_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.REVERT_NAV);
		}
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (Utils.isPieceJailSquare(squareId)) {
			return false;
		} else {
			int piece = Utils.getColoredPiece(squareId, getGame());
			return Utils.isWhitePiece(piece) && getGame().isWhitesMove()
					|| Utils.isBlackPiece(piece) && !getGame().isWhitesMove();
		}
	}

	@Override
	public String getTitle() {
		return "(" + getGame().getId() + ") Examining";
	}

	@Override
	public void init() {
		super.init();
		board.getConnector().getGameService().addGameServiceListener(listener);
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
		return true;
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
		return true;
	}

	@Override
	public boolean onClose() {
		boolean result = true;
		if (board.getConnector().isConnected()
				&& board.getGame().isInState(Game.ACTIVE_STATE)) {
			board.getConnector().onUnexamine(board.getGame());
		}
		return result;
	}

	@Override
	public void onNavBack() {
		board.getConnector().onExamineModeBack(board.getGame());
	}

	@Override
	public void onNavCommit() {
		board.getConnector().onExamineModeCommit(board.getGame());
	}

	@Override
	public void onNavFirst() {
		board.getConnector().onExamineModeFirst(board.getGame());
	}

	@Override
	public void onNavForward() {
		board.getConnector().onExamineModeForward(board.getGame());
	}

	@Override
	public void onNavLast() {
		board.getConnector().onExamineModeLast(board.getGame());
	}

	@Override
	public void onNavRevert() {
		board.getConnector().onExamineModeRevert(board.getGame());
	}

	@Override
	protected void onPlayGameEndSound() {
		SoundService.getInstance().playSound("obsGameEnd");
	}

	@Override
	protected void onPlayGameStartSound() {
		SoundService.getInstance().playSound("gameStart");
	}

	@Override
	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		board.unhighlightAllSquares();
		adjustToGameMove();
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
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

		if (Utils.isPieceJailSquare(toSquare)) {
			SoundService.getInstance().playSound("illegalMove");
			return;
		}

		Game game = board.getGame();
		Move move = null;
		if (Utils.isPieceJailSquare(fromSquare)) {
			move = new Move(Utils.pieceFromColoredPiece(Utils
					.pieceJailSquareToPiece(fromSquare)), toSquare);
			board.getSquare(toSquare).setPiece(
					Utils.getColoredPiece(board.getAutoPromoteSelection(), game
							.isWhitesMove()));
		} else if (GameUtils.isPromotion(board.getGame(), fromSquare, toSquare)) {
			move = new Move(fromSquare, toSquare, game.getPiece(fromSquare),
					game.getColorToMove(), game.getPiece(toSquare), board
							.getAutoPromoteSelection(), EMPTY,
					Move.PROMOTION_CHARACTERISTIC);
			board.getSquare(toSquare).setPiece(
					Utils.getColoredPiece(board.getAutoPromoteSelection(), game
							.isWhitesMove()));
		} else {
			move = new Move(fromSquare, toSquare, game.getPiece(fromSquare),
					game.getColorToMove(), game.getPiece(toSquare));
			board.getSquare(toSquare).setPiece(
					Utils.getColoredPiece(game.getPiece(fromSquare), game
							.isWhitesMove()));
		}
		board.getSquare(fromSquare).highlight();
		board.getSquare(toSquare).highlight();
		board.getConnector().makeMove(game, move);

	}

	@Override
	public void userMiddleClicked(int square) {
	}

	@Override
	public void userRightClicked(int square) {
	}
}
