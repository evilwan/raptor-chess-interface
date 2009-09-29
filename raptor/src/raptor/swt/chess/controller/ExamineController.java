package raptor.swt.chess.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.connector.Connector;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.BoardUtils;
import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessBoardController;

public class ExamineController extends ChessBoardController {
	static final Log LOG = LogFactory.getLog(ExamineController.class);
	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void gameInactive(Game game) {
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							InactiveController inactiveController = new InactiveController(
									getGame());
							getBoard().setController(inactiveController);
							inactiveController.setBoard(board);

							board.clearCoolbar();
							connector.getGameService()
									.removeGameServiceListener(listener);

							inactiveController.init();

							inactiveController
									.setItemChangedListeners(itemChangedListeners);
							setItemChangedListeners(null);
							ExamineController.this.dispose();
							inactiveController.fireItemChanged();
						} catch (Throwable t) {
							connector.onError("ExamineController.gameInactive",
									t);
						}
					}
				});
			}
		}

		@Override
		public void gameStateChanged(Game game, final boolean isNewMove) {
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							examinePositionUpdate();
						} catch (Throwable t) {
							connector.onError(
									"ExamineController.gameStateChanged", t);
						}
					}
				});
			}
		}

		@Override
		public void illegalMove(Game game, final String move) {
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							examineOnIllegalMove(move);
						} catch (Throwable t) {
							connector.onError("ExamineController.illegalMove",
									t);
						}
					}
				});
			}
		}

	};

	protected Connector connector;

	public ExamineController(Game game, Connector connector) {
		super(game);
		this.connector = connector;
	}

	@Override
	protected void adjustClockColors() {
		if (!isBeingReparented()) {
			if (getGame().getColorToMove() == WHITE) {
				board.getWhiteClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
				board.getBlackClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
			} else {
				board.getBlackClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
				board.getWhiteClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
			}
		}
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
			board.getGameDescriptionLabel().setText("Examining a game");
		}
	}

	@Override
	protected void adjustNavButtonEnabledState() {
		if (!isBeingReparented()) {
			if (getGame().isInState(Game.ACTIVE_STATE)) {
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
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (!isBeingReparented()) {
			if (BoardUtils.isPieceJailSquare(squareId)) {
				return false;
			} else {
				int piece = BoardUtils.getColoredPiece(squareId, getGame());
				return BoardUtils.isWhitePiece(piece)
						&& getGame().isWhitesMove()
						|| BoardUtils.isBlackPiece(piece)
						&& !getGame().isWhitesMove();
			}
		}
		return false;
	}

	@Override
	public void dispose() {
		connector.getGameService().removeGameServiceListener(listener);
		if (connector.isConnected() && getGame().isInState(Game.ACTIVE_STATE)) {
			connector.onUnexamine(getGame());
		}
		super.dispose();
	}

	public void examineOnIllegalMove(String move) {
		if (!isBeingReparented()) {
			LOG.info("examineOnIllegalMove " + getGame().getId() + " ...");
			long startTime = System.currentTimeMillis();
			SoundService.getInstance().playSound("illegalMove");
			board.getStatusLabel().setText("Illegal Move: " + move);
			board.unhighlightAllSquares();
			LOG.info("examineOnIllegalMove in " + getGame().getId() + "  "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public void examinePositionUpdate() {
		if (!isBeingReparented()) {
			LOG.info("examinePositionUpdate " + getGame().getId() + " ...");
			long startTime = System.currentTimeMillis();

			stopClocks();
			adjustClockColors();
			if (getGame().isInState(Game.IS_CLOCK_TICKING_STATE)
					&& getGame().isInState(Game.ACTIVE_STATE)) {
				adjustClockLabelsAndUpdaters();
				startClocks();
			}

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
	}

	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return "Examining(" + getGame().getId() + ")";
	}

	@Override
	public void init() {
		super.init();
		connector.getGameService().addGameServiceListener(listener);
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
		return true;
	}

	@Override
	public void onNavBack() {
		connector.onExamineModeBack(getGame());
	}

	@Override
	public void onNavCommit() {
		connector.onExamineModeCommit(getGame());
	}

	@Override
	public void onNavFirst() {
		connector.onExamineModeFirst(getGame());
	}

	@Override
	public void onNavForward() {
		connector.onExamineModeForward(getGame());
	}

	@Override
	public void onNavLast() {
		connector.onExamineModeLast(getGame());
	}

	@Override
	public void onNavRevert() {
		connector.onExamineModeRevert(getGame());
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
		if (!isBeingReparented()) {
			board.unhighlightAllSquares();
			adjustToGameMove();
		}
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		if (!isBeingReparented()) {
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

			if (BoardUtils.isPieceJailSquare(toSquare)) {
				SoundService.getInstance().playSound("illegalMove");
				return;
			}

			Game game = getGame();
			Move move = null;
			if (GameUtils.isPromotion(getGame(), fromSquare, toSquare)) {
				move = BoardUtils.createMove(getGame(), fromSquare, toSquare,
						board.getAutoPromoteSelection());
			} else {
				move = BoardUtils.createMove(getGame(), fromSquare, toSquare);
			}

			board.getSquare(fromSquare).highlight();
			board.getSquare(toSquare).highlight();
			connector.makeMove(game, move);
		}
	}

	@Override
	public void userMiddleClicked(int square) {
	}

	@Override
	public void userRightClicked(int square) {
	}
}
