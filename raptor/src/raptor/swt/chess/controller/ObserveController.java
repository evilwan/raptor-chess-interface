package raptor.swt.chess.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.connector.Connector;
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
		public void gameInactive(Game game) {
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {

							board.redrawSquares();

							InactiveController inactiveController = new InactiveController(
									getGame());
							getBoard().setController(inactiveController);
							inactiveController.setBoard(board);

							board.clearCoolbar();
							getConnector().getGameService()
									.removeGameServiceListener(listener);

							inactiveController.init();
							inactiveController
									.setItemChangedListeners(itemChangedListeners);
							// Set the listeners to null so they wont get
							// cleared and disposed
							setItemChangedListeners(null);
							ObserveController.this.dispose();
							inactiveController.fireItemChanged();
						} catch (Throwable t) {
							getConnector().onError(
									"ExamineController.gameInactive", t);
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
							if (isNewMove) {
								adjustToGameMove();
							} else {
								adjustToGameChangeNotInvolvingMove();
							}
						} catch (Throwable t) {
							connector.onError(
									"ObserveController.gameStateChanged", t);
						}
					}
				});
			}
		}
	};

	protected Connector connector;

	public ObserveController(Game game, Connector connector) {
		super(game);
		this.connector = connector;
	}

	@Override
	protected void adjustCoolbarToInitial() {
		if (!isBeingReparented()) {
			board.addGameActionButtonsToCoolbar();
			board.addScripterCoolbar();
			board.packCoolbar();
		}
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isBeingReparented()) {
			board.getGameDescriptionLabel().setText(
					"Observing " + getGame().getEvent());
		}
	}

	@Override
	protected void adjustPremoveLabel() {
		if (!isBeingReparented()) {
			board.getCurrentPremovesLabel().setText("");
		}
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		return false;
	}

	@Override
	public void dispose() {
		connector.getGameService().removeGameServiceListener(listener);
		if (connector.isConnected() && getGame().isInState(Game.ACTIVE_STATE)) {
			connector.onUnobserve(getGame());
		}
		super.dispose();
	}

	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return "Observing(" + getGame().getId() + ")";
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
	protected void onPlayGameEndSound() {
		SoundService.getInstance().playSound("obsGameEnd");
	}

	@Override
	protected void onPlayGameStartSound() {
		SoundService.getInstance().playSound("gameStart");
	}

	@Override
	protected void onPlayMoveSound() {
		if (getPreferences().getBoolean(
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
