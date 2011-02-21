/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess.controller;

import raptor.util.RaptorLogger;
 
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;

import raptor.Raptor;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chess.Game;
import raptor.chess.GameCursor;
import raptor.chess.Move;
import raptor.chess.pgn.PgnHeader;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.MouseButtonAction;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringUtils;

/**
 * A controller used when observing a game.
 * 
 * The user isnt allowed to make any moves on a game being observed. However
 * they are allowed to use the nav buttons.
 */
public class ObserveController extends ChessBoardController {
	static final RaptorLogger LOG = RaptorLogger.getLog(ObserveController.class);

	protected GameCursor cursor = null;
	protected GameServiceListener listener = new GameServiceAdapter() {
		@Override
		public void droppablePiecesChanged(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}

								refreshBoard();
							}
						});
			}
		}

		@Override
		public void gameInactive(final Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}
								onMatchWinner();
								board.getResultDecorator()
										.setDecorationFromResult(
												getGame().getResult());
								board.redrawPiecesAndArtifacts(true);

								if (!handleSpeakResults(game)) {
									onPlayGameEndSound();
								}

								InactiveController inactiveController = new InactiveController(
										getGame());
								getBoard().setController(inactiveController);
								inactiveController.setBoard(board);

								getConnector().getGameService()
										.removeGameServiceListener(listener);
								inactiveController
										.setItemChangedListeners(itemChangedListeners);
								// Clear the cool bar and init the inactive
								// controller.
								ChessBoardUtils.clearCoolbar(getBoard());
								inactiveController.init();
								// Set the listeners to null so they wont get
								// cleared and disposed
								setItemChangedListeners(null);
								ObserveController.this.dispose();
							}
						});
			}
		}

		@Override
		public void gameMovesAdded(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}

								cursor.setCursorMasterLast();
								refresh();

							}
						});
			}
		}

		@Override
		public void gameStateChanged(final Game game, final boolean isNewMove) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}

								if (isNewMove) {
									if (!handleSpeakMove(game.getLastMove())) {
										onPlayMoveSound();
									}
								}

								if (isForceUpdate()) {

									cursor.setCursorMasterLast();

									board.getSquareHighlighter()
											.removeAllHighlights();
									board.getArrowDecorator().removeAllArrows();

									Move lastMove = getGame().getLastMove();

									if (lastMove != null) {
										addDecorationsForMove(lastMove, false);
									}
									refresh();
								}
							}
						});
			}

		}

		@Override
		public void observedGameBecameExamined(final Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}
								ExamineController examineController = new ExamineController(
										game, board.isWhiteOnTop(), connector);
								getBoard().setController(examineController);
								examineController
										.setItemChangedListeners(itemChangedListeners);
								examineController.setBoard(board);
								connector.getGameService()
										.removeGameServiceListener(listener);

								// Set the listeners to null so they wont
								// get
								// cleared and disposed
								setItemChangedListeners(null);

								// Clear the cool bar and init the
								// examineController
								// controller.
								ChessBoardUtils.clearCoolbar(getBoard());
								examineController.init();

								unobserveOnDispose = false;
								ObserveController.this.dispose();
							}
						});
			}
		}
	};
	protected ToolBar toolbar;
	protected boolean unobserveOnDispose = true;
	protected boolean isBughouseOtherBoard;

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 */
	public ObserveController(Game game, boolean isBughouseOtherBoard,
			Connector connector) {
		super(new GameCursor(game,
				GameCursor.Mode.MakeMovesOnMasterSetCursorToLast), connector);
		cursor = (GameCursor) getGame();
		this.isBughouseOtherBoard = isBughouseOtherBoard;
	}

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 */
	public ObserveController(Game game, Connector connector) {
		super(new GameCursor(game,
				GameCursor.Mode.MakeMovesOnMasterSetCursorToLast), connector);
		cursor = (GameCursor) getGame();
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isDisposed()) {
			board.getGameDescriptionLabel().setText(
					"Observing " + getGame().getHeader(PgnHeader.Event));
		}
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		return false;
	}

	@Override
	public void dispose() {
		try {
			getConnector().getGameService().removeGameServiceListener(listener);
			if (unobserveOnDispose && getConnector().isConnected()
					&& getGame().isInState(Game.ACTIVE_STATE)) {
				getConnector().onUnobserve(getGame());
			}
			if (toolbar != null && !toolbar.isDisposed()) {
				toolbar.setVisible(false);
				SWTUtils.clearToolbar(toolbar);
				toolbar = null;
			}
			super.dispose();
		} catch (Throwable t) {// Eat it its prob a disposed exception.
		}
	}

	public void enableDisableNavButtons() {
		setToolItemEnabled(ToolBarItemKey.NEXT_NAV, cursor.hasNext());
		setToolItemEnabled(ToolBarItemKey.BACK_NAV, cursor.hasPrevious());
		setToolItemEnabled(ToolBarItemKey.FIRST_NAV, cursor.hasFirst());
		setToolItemEnabled(ToolBarItemKey.LAST_NAV, cursor.hasLast());
	}

	public GameCursor getCursor() {
		return cursor;
	}

	@Override
	public String getTitle() {
		return getConnector().getShortName() + "(Obs " + getGame().getId()
				+ ")";
	}

	@Override
	public Control getToolbar(Composite parent) {
		boolean isCoolbarMode = getPreferences().getBoolean(
				PreferenceKeys.BOARD_COOLBAR_MODE);
		if (toolbar == null) {

			toolbar = SWTUtils.createToolbar(isCoolbarMode ? getBoard().getCoolbar()
					: parent);
			ChessBoardUtils.addActionsToToolbar(this,
					RaptorActionContainer.ObservingChessBoard, toolbar, false);

			setToolItemSelected(ToolBarItemKey.FORCE_UPDATE, true);
			enableDisableNavButtons();

			if (isCoolbarMode) {
				ChessBoardUtils.adjustCoolbar(getBoard(), toolbar);
			}
		} else {
			if (!isCoolbarMode) {
				toolbar.setParent(parent);
			}
		}

		if (isCoolbarMode) {
			return null;
		} else {
			return toolbar;
		}
	}

	@Override
	public void init() {
		board.getArrowDecorator().removeAllArrows();
		board.getSquareHighlighter().removeAllHighlights();

		board.setWhiteOnTop(RaptorStringUtils.getBooleanValue(game
				.getHeader(PgnHeader.WhiteOnTop)));

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

		if (getPreferences().getBoolean(PreferenceKeys.BOARD_COOLBAR_MODE)) {
			getToolbar(null);
		}

		cursor.setCursorMasterLast();
		refresh();

		onPlayGameStartSound();

		// Add the service listener last so there are no synch problems.
		// It is ok if we miss moves the GameService will update the game.
		connector.getGameService().addGameServiceListener(listener);
		fireItemChanged();
	}

	@Override
	public void onBack() {
		cursor.setCursorPrevious();
		refresh(false);
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void onFirst() {
		cursor.setCursorFirst();
		refresh(false);
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void onForward() {
		cursor.setCursorNext();
		boolean isLast = !cursor.hasNext();
		if (isLast) {
			cursor.setCursorMasterLast();
		}
		refresh(isLast);
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void onLast() {
		cursor.setCursorMasterLast();
		setToolItemEnabled(ToolBarItemKey.FORCE_UPDATE, true);
		refresh(true);
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void refresh(boolean isUpdatingClocks) {
		if (isDisposed()) {
			return;
		}

		adjustTimeUpLabel();

		board.getMoveList().updateToGame();
		board.getMoveList().select(cursor.getCursorPosition());
		enableDisableNavButtons();
		super.refresh(isUpdatingClocks);
		board.getEngineAnalysisWidget().updateToGame();
	}

	@Override
	public void userCancelledMove(int fromSquare) {
	}

	@Override
	public void userInitiatedMove(int square) {

	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
	}

	@Override
	public void userMouseWheeled(int count) {
		if (count < 0) {
			onForward();
		} else {
			onBack();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void userPressedMouseButton(MouseButtonAction button, int square) {
		ObservingMouseAction action = ObservingMouseAction
				.valueOf(getPreferences().getString(
						OBSERVING_CONTROLLER + button.getPreferenceSuffix()));

		if (action == null) {
			Raptor.getInstance().onError(
					"ObservingMouseAction was null. This should never happn. "
							+ button.toString() + " " + square);
			return;
		}

		switch (action) {
		case None:
			break;
		case MakePrimaryGame:
			connector.setPrimaryGame(getGame());
			break;
		case MatchWinner:
			onMatchWinner();
			break;
		case AddGameChatTab:
			ChatUtils.openGameChatTab(getConnector(), game.getId(), true);
			break;
		}
	}

	/**
	 * Invoked when the move list is clicked on. THe halfMoveNumber is the move
	 * selected.
	 * 
	 * The default implementation does nothing. It can be overridden to provide
	 * functionality.
	 */
	@Override
	public void userSelectedMoveListMove(int halfMoveNumber) {
		cursor.setCursor(halfMoveNumber);
		refresh(false);
		addDecorationsForLastMoveListMove();
	}

	protected boolean handleSpeakMove(Move move) {
		boolean result = false;
		if (SoundService.getInstance().isSpeechSetup()
				&& getPreferences().getBoolean(
						PreferenceKeys.BOARD_SPEAK_WHEN_OBSERVING)) {
			speakMove(move);
			result = true;
		}
		return result;
	}

	protected boolean handleSpeakResults(Game game) {
		boolean result = false;
		if (SoundService.getInstance().isSpeechSetup()
				&& getPreferences().getBoolean(
						PreferenceKeys.BOARD_SPEAK_RESULTS)) {
			speakResults(game);
			result = true;
		}
		return result;
	}

	protected boolean isForceUpdate() {
		if (getToolItem(ToolBarItemKey.FORCE_UPDATE) == null) {
			return true;
		} else {
			return isToolItemSelected(ToolBarItemKey.FORCE_UPDATE);
		}

	}

	protected void onMatchWinner() {
		if (isToolItemSelected(ToolBarItemKey.MATCH_WINNER)) {
			if (connector instanceof BughouseSuggestController) {
				return;
			}
			connector.matchWinner(cursor.getMasterGame());
		}
	}

	protected void onPlayGameEndSound() {
		SoundService.getInstance().playSound("obsGameEnd");
	}

	protected void onPlayGameStartSound() {
		if (!isBughouseOtherBoard) {
			SoundService.getInstance().playSound("gameStart");
		}
	}

	protected void onPlayMoveSound() {
		if (getPreferences().getBoolean(
				PreferenceKeys.BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING)) {
			SoundService.getInstance().playSound("obsMove");
		}
	}

}
