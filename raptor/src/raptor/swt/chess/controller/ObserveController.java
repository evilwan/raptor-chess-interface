package raptor.swt.chess.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.Game;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.ChessBoardController;

public class ObserveController extends ChessBoardController {
	static final Log LOG = LogFactory.getLog(ObserveController.class);

	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void gameCreated(Game game) {
		}

		@Override
		public void gameInactive(Game game) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							InactiveController inactiveController = new InactiveController();
							getBoard().setController(inactiveController);
							inactiveController.setBoard(board);

							board.clearCoolbar();
							board.getConnector().getGameService()
									.removeGameServiceListener(listener);

							inactiveController.init();

							getBoard().fireOnControllerStateChange();
							ObserveController.this.dispose();
						} catch (Throwable t) {
							board.getConnector().onError(
									"ExamineController.gameInactive", t);
						}
					}
				});
			}
		}

		@Override
		public void gameStateChanged(Game game, final boolean isNewMove) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							if (isNewMove) {
								adjustToGameMove();
							} else {
								adjustToGameChangeNotInvolvingMove();
							}
						} catch (Throwable t) {
							board.getConnector().onError(
									"ObserveController.gameStateChanged", t);
						}
					}
				});
			}
		}
	};

	@Override
	protected void adjustCoolbarToInitial() {
		board.addGameActionButtonsToCoolbar();
		board.addScripterCoolbar();
		board.packCoolbar();
	}

	@Override
	public void adjustGameDescriptionLabel() {
		board.getGameDescriptionLabel().setText(
				"Observing " + getGame().getEvent());
	}

	@Override
	protected void adjustPremoveLabel() {
		board.getCurrentPremovesLabel().setText("");
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		return false;
	}

	@Override
	public void dispose() {
		board.getConnector().getGameService().removeGameServiceListener(
				listener);
		super.dispose();
	}

	@Override
	public String getTitle() {
		return "Observing(" + getGame().getId() + ")";
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
		return false;
	}

	@Override
	public boolean onClose() {
		boolean result = true;
		if (board.getConnector().isConnected()
				&& board.getGame().isInState(Game.ACTIVE_STATE)) {
			board.getConnector().onUnobserve(board.getGame());
		}
		return result;
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
		if (board.getPreferences().getBoolean(
				PreferenceKeys.BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING)) {
			SoundService.getInstance().playSound("obsMove");
		}
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
	}

	@Override
	public void userMiddleClicked(int square) {
	}

	@Override
	public void userRightClicked(int square) {
	}
}
