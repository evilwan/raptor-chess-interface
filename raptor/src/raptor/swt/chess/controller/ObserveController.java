package raptor.swt.chess.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.connector.Connector;
import raptor.game.Game;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.SWTUtils;
import raptor.swt.chess.BoardUtils;
import raptor.swt.chess.ChessBoardController;

/**
 * A controller used when observing a game.
 * 
 * The user isnt allowed to make any moves on a game being observed. However
 * they are allowed to use the nav buttons.
 */
public class ObserveController extends ChessBoardController {
	static final Log LOG = LogFactory.getLog(ObserveController.class);

	protected GameServiceListener listener = new GameServiceAdapter() {
		@Override
		public void gameInactive(Game game) {
			if (!isBeingUsed() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {

							board.redrawSquares();
							onPlayGameEndSound();

							InactiveController inactiveController = new InactiveController(
									getGame());
							getBoard().setController(inactiveController);
							inactiveController.setBoard(board);

							// board.clearCoolbar();
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
			if (!isBeingUsed() && game.getId().equals(getGame().getId())) {
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
	protected ToolBar toolbar;

	public ObserveController(Game game, Connector connector) {
		super(game);
		this.connector = connector;
	}

	// @Override
	// protected void adjustCoolbarToInitial() {
	// try {
	// LOG.error("Initing toolbar");
	// // if (!isBeingReparented()) {
	// // board.addGameActionButtonsToCoolbar();
	// //board.addScripterCoolbar();
	// // board.packCoolbar();
	// // }
	// } catch (Throwable t) {
	// LOG.error("Error initing toolbar", t);
	// }
	// }

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isBeingUsed()) {
			board.getGameDescriptionLabel().setText(
					"Observing " + getGame().getEvent());
		}
	}

	@Override
	protected void adjustPremoveLabel() {
		if (!isBeingUsed()) {
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
		if (toolbar != null) {
			toolbar.setVisible(false);
			SWTUtils.clearToolbar(toolbar);
			toolbar = null;
		}

		super.dispose();
	}

	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return connector.getShortName() + "(Obs " + getGame().getId() + ")";
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = new ToolBar(parent, SWT.FLAT);
			BoardUtils.addNavIconsToToolbar(this, toolbar);
			new ToolItem(toolbar, SWT.SEPARATOR);
		} else if (toolbar.getParent() != parent) {
			toolbar.setParent(parent);
		}
		return toolbar;
	}

	@Override
	public void init() {
		super.init();

		/**
		 * In Droppable games (bughouse/crazyhouse) you own your own piece jail
		 * since you can drop pieces from it.
		 * 
		 * In other games its just a collection of pieces yu have captured so
		 * your opponent owns your piece jail.
		 */
		if (getGame().isInState(Game.DROPPABLE_STATE)) {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? true : false);
		} else {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? false : true);
		}

		connector.getGameService().addGameServiceListener(listener);
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
