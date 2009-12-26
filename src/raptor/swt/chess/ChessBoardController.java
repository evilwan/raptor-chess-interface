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
package raptor.swt.chess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Raptor;
import raptor.chess.BughouseGame;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.GameCursor;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.SoundService;
import raptor.swt.ItemChangedListener;
import raptor.swt.chess.controller.ToolBarItemKey;

/**
 * A chess board controller handles updating a ChessBoard to a Game. It also
 * handles user actions on a game. Subclasses will use a backing Connector to
 * send and receive moves, and adjust the underlying ChessBoard accordingly.
 * 
 * This is the base controller class providing method implementations to do many
 * things. The idea is to override a method an adjust* if you want to change the
 * functionality, and utilize the refresh methods to update all of the controls
 * on the ChessBoard.
 */
public abstract class ChessBoardController implements BoardConstants,
		GameConstants {
	static final Log LOG = LogFactory.getLog(ChessBoardController.class);

	protected ClockLabelUpdater blackClockUpdater;
	protected ChessBoard board;
	protected Game game;
	protected List<ItemChangedListener> itemChangedListeners = new ArrayList<ItemChangedListener>(
			5);
	protected boolean storedIsWhiteOnTop;
	protected boolean storedIsWhitePieceJailOnTop;
	protected Map<ToolBarItemKey, ToolItem> toolItemMap = new HashMap<ToolBarItemKey, ToolItem>();
	protected ClockLabelUpdater whiteClockUpdater;
	protected Connector connector;

	/**
	 * Constructs a ChessBoardController with the specified game.
	 * 
	 * It is important to note that upon construction a controller will not have
	 * a ChessBoard.
	 * 
	 * To set a chess board, construct a controller, then call setChessBoard and
	 * when the ChessBoard has had its controls created invoke init().
	 * 
	 * @param game
	 *            The game this controller manages.
	 */
	public ChessBoardController(Game game) {
		this(game, null);
	}

	/**
	 * Constructs a ChessBoardController with the specified game.
	 * 
	 * It is important to note that upon construction a controller will not have
	 * a ChessBoard.
	 * 
	 * To set a chess board, construct a controller, then call setChessBoard and
	 * when the ChessBoard has had its controls created invoke init().
	 * 
	 * @param game
	 *            The game this controller manages.
	 * @param connector
	 *            The backing connector, man be null.
	 */
	public ChessBoardController(Game game, Connector connector) {
		this.game = game;
		this.connector = connector;
	}

	public void speakResults(Game game) {
		String text = getGame().getHeader(PgnHeader.ResultDescription);
		if (text == null) {
			switch (game.getResult()) {
			case BLACK_WON: {
				text = "zero one";
				break;
			}
			case WHITE_WON: {
				text = "one zero";
				break;
			}
			case DRAW: {
				text = "one half one half";
				break;
			}
			case UNDETERMINED: {
				text = "Game ended.";
				break;
			}
			case ON_GOING: {
				text = "Game ended";
				break;
			}
			}
		}
		SoundService.getInstance().textToSpeech(text);
	}

	public void speakMove(Move move) {
		String text = "";

		switch (move.getPiece()) {
		case PAWN:
			text += "pawn ";
			break;
		case KNIGHT:
			text += "knight ";
			break;
		case BISHOP:
			text += "bishop ";
			break;
		case ROOK:
			text += "rook ";
			break;
		case QUEEN:
			text += "queen ";
			break;
		case KING:
			text += "king ";
			break;
		}

		if (move.isCapture()) {
			text += "takes ";
		} else {
			text += "to ";
		}

		text += GameUtils.getSan(move.getTo()) + " ";
		
		if (move.isPromotion()) {
			text += GameUtils.getSan(move.getTo()) + "equals ";
			switch (move.getPiecePromotedTo()) {
			case KNIGHT:
				text += "knight ";
				break;
			case BISHOP:
				text += "bishop ";
				break;
			case ROOK:
				text += "rook ";
				break;
			case QUEEN:
				text += "queen ";
				break;
			case KING:
				text += "king ";
				break;
			}
		}
		SoundService.getInstance().textToSpeech(text);
	}

	public void addDecorationsForLastMoveListMove() {
		removeAllMoveDecorations();
		Move lastMove = getGame().getLastMove();
		if (lastMove != null) {
			if (getPreferences().getBoolean(
					PreferenceKeys.HIGHLIGHT_SHOW_ON_MOVE_LIST_MOVES)) {
				board
						.getSquareHighlighter()
						.addHighlight(
								new Highlight(
										lastMove.getFrom(),
										lastMove.getTo(),
										getPreferences()
												.getColor(
														PreferenceKeys.HIGHLIGHT_OBS_COLOR),
										getPreferences()
												.getBoolean(
														PreferenceKeys.HIGHLIGHT_FADE_AWAY_MODE)));
			}

			if (getPreferences().getBoolean(
					PreferenceKeys.ARROW_SHOW_ON_MOVE_LIST_MOVES)) {
				board.getArrowDecorator().addArrow(
						new Arrow(lastMove.getFrom(), lastMove.getTo(),
								getPreferences().getColor(
										PreferenceKeys.ARROW_OBS_COLOR),
								getPreferences().getBoolean(
										PreferenceKeys.ARROW_FADE_AWAY_MODE)));
			}
		}
	}

	/**
	 * Adds an item changed listener. This listener should be invoked whenever
	 * the meta information about the game changes e.g. if it is closable, its
	 * title, its icon, etc.
	 */
	public void addItemChangedListener(ItemChangedListener listener) {
		itemChangedListeners.add(listener);
	}

	public void addToolItem(ToolBarItemKey key, ToolItem item) {
		toolItemMap.put(key, item);
	}

	/**
	 * Adjusts the games description label. Should be overridden to provide a
	 * description of the game.
	 */
	public abstract void adjustGameDescriptionLabel();

	/**
	 * Adjusts the game status label. If the game is not in an active state, the
	 * status label sets itself to getResultDescription in the game. If the game
	 * is in an active state, the status is set to the last move.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	public void adjustGameStatusLabel() {
		if (getGame().isInState(Game.ACTIVE_STATE)) {
			if (getGame().getMoveList().getSize() > 0) {
				Move lastMove = null;
				if (getGame() instanceof GameCursor) {
					lastMove = ((GameCursor) getGame()).getCursorGame()
							.getMoveList().get(
									getGame().getMoveList().getSize() - 1);
				} else {
					lastMove = getGame().getMoveList().get(
							getGame().getMoveList().getSize() - 1);
				}

				if (lastMove != null) {
					int moveNumber = lastMove.getFullMoveCount();

					board.getStatusLabel().setText(
							"Last Move: "
									+ moveNumber
									+ ") "
									+ (lastMove.isWhitesMove() ? "" : "... ")
									+ GameUtils.convertSanToUseUnicode(lastMove
											.toString(), lastMove
											.isWhitesMove()));
				} else {
					board.getStatusLabel().setText("");
				}

			} else {
				board.getStatusLabel().setText("");
			}
		} else {
			String result = getGame().getHeader(PgnHeader.ResultDescription);
			if (result != null) {
				board.getStatusLabel().setText(
						getGame().getHeader(PgnHeader.ResultDescription));
			}
		}
	}

	/**
	 * Adjusts the time up label if the game is bughouse.
	 */
	public void adjustTimeUpLabel() {
		if (getPreferences().getBoolean(
				PreferenceKeys.BOARD_SHOW_BUGHOUSE_SIDE_UP_TIME)
				&& getGame().getVariant() == Variant.bughouse) {
			BughouseGame bugGame = getGame() instanceof GameCursor ? (BughouseGame) ((GameCursor) getGame())
					.getMasterGame()
					: (BughouseGame) getGame();

			if (bugGame.getOtherBoard() != null) {
				long teamOneWhite = Long.parseLong(bugGame
						.getHeader(PgnHeader.WhiteRemainingMillis));
				long teamOneBlack = Long.parseLong(bugGame
						.getHeader(PgnHeader.BlackRemainingMillis));

				long teamTwoWhite = Long.parseLong(bugGame.getOtherBoard()
						.getHeader(PgnHeader.WhiteRemainingMillis));
				long teamTwoBlack = Long.parseLong(bugGame.getOtherBoard()
						.getHeader(PgnHeader.BlackRemainingMillis));

				if (teamOneWhite > teamTwoWhite) {
					getBoard().getWhiteLagLabel().setImage(
							Raptor.getInstance().getIcon("up"));
				} else {
					getBoard().getWhiteLagLabel().setImage(null);
				}
				if (teamOneBlack > teamTwoBlack) {
					getBoard().getBlackLagLabel().setImage(
							Raptor.getInstance().getIcon("up"));
				} else {
					getBoard().getBlackLagLabel().setImage(null);
				}
			}
		} else {
			getBoard().getWhiteLagLabel().setImage(null);
			getBoard().getBlackLagLabel().setImage(null);
		}
	}

	/**
	 * Returns true if a user can begin making a move from the specified square.
	 */
	public abstract boolean canUserInitiateMoveFrom(int squareId);

	/**
	 * Confirms closing of the window the game is being viewed in. Default is to
	 * always return true indicating to go ahead and close it. Can be overridden
	 * to provide other behavior.
	 */
	public boolean confirmClose() {
		return true;
	}

	/**
	 * Disposes this controller. The ChessBoard this controller is managing
	 * should NEVER be disposed. However, all other resources such as the
	 * traverser,and clock updaters must be disposed.
	 * 
	 * This method can also be overridden to remove listeners when a controller
	 * is no longer being used.
	 */
	public void dispose() {
		// Don't dispose the board
		// It is up to the caller to do that.

		if (blackClockUpdater != null) {
			blackClockUpdater.stop();
			blackClockUpdater.dispose();
			blackClockUpdater = null;
		}
		if (whiteClockUpdater != null) {
			whiteClockUpdater.stop();
			whiteClockUpdater.dispose();
			whiteClockUpdater = null;
		}

		if (itemChangedListeners != null) {
			itemChangedListeners.clear();
		}

		if (toolItemMap != null) {
			toolItemMap.clear();
		}
		board = null;

		if (LOG.isInfoEnabled()) {
			LOG.info("Disposed ChessBoardController");
		}
	}

	/**
	 * Should be invoked when meta information about a game changes. This
	 * information includes, whether or not the game is closable,its title, its
	 * icon.
	 */
	public void fireItemChanged() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Firing itemChanged");
		}
		for (ItemChangedListener listener : itemChangedListeners) {
			listener.itemStateChanged();
		}
	}

	/**
	 * Returns the non-colored promotion piece selected. EMPTY if none.
	 */
	public int getAutoPromoteSelection() {
		int result = QUEEN;

		if (isToolItemSelected(ToolBarItemKey.AUTO_KING)) {
			result = KING;
		} else if (isToolItemSelected(ToolBarItemKey.AUTO_QUEEN)) {
			result = QUEEN;
		} else if (isToolItemSelected(ToolBarItemKey.AUTO_KNIGHT)) {
			result = KNIGHT;
		} else if (isToolItemSelected(ToolBarItemKey.AUTO_BISHOP)) {
			result = BISHOP;
		} else if (isToolItemSelected(ToolBarItemKey.AUTO_ROOK)) {
			result = ROOK;
		}
		return result;
	}

	/**
	 * Returns the ChessBoard this controller is controlling.
	 */
	public ChessBoard getBoard() {
		return board;
	}

	/**
	 * Returns the connector. May return null.
	 */
	public Connector getConnector() {
		return connector;
	}

	/**
	 * Returns the game class this controller is controlling.
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * Returns the list of ItemChangedListeners added to this controller.
	 * 
	 * @return
	 */
	public List<ItemChangedListener> getItemChangedListeners() {
		return itemChangedListeners;
	}

	/**
	 * Should be overridden to show a suitable title for the game. The result
	 * should be short 10-12 chars max.
	 */
	public abstract String getTitle();

	/**
	 * Returns the toolbar for this controller. The default implementation
	 * returns null.
	 */
	public Control getToolbar(Composite parent) {
		return null;
	}

	/**
	 * Returns the tool item with the specified key.
	 */
	public ToolItem getToolItem(ToolBarItemKey key) {
		return toolItemMap.get(key);
	}

	/**
	 * Initializes the ChessBoardController. A ChessBoard should be set on the
	 * controller prior to calling this method.
	 */
	public abstract void init();

	/**
	 * Returns true if this controller is being used on a chess board, false
	 * otherwise.
	 */
	public boolean isDisposed() {
		return getBoard() == null;
	}

	/**
	 * Returns true if the specified tool item is selected. Returns false if the
	 * tool item is does not exist or not selected.
	 * 
	 * @param key
	 *            They tool items key.
	 */
	public boolean isToolItemSelected(ToolBarItemKey key) {
		boolean result = false;
		ToolItem item = getToolItem(key);
		if (item != null && !item.isDisposed()) {
			return item.getSelection();
		}
		return result;
	}

	/**
	 * Invoked when the ChessBoard is being viewed. The default implementation
	 * does nothing.
	 */
	public void onActivate() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("In onActivate : " + game.getId() + " "
					+ game.getHeader(PgnHeader.Event));
		}
	}

	/**
	 * Handles the auto draw action. The default implementation does nothing.
	 */
	public void onAutoDraw() {

	}

	/**
	 * Handles the back action. The default implementation does nothing.
	 */
	public void onBack() {

	}

	/**
	 * Handles the commit action. The default implementation does nothing.
	 */
	public void onCommit() {

	}

	/**
	 * Handles the on move list action. The default implementation toggles the
	 * showing of the move list.
	 */
	public void onEngineAnalysis() {
		if (isToolItemSelected(ToolBarItemKey.TOGGLE_ANALYSIS_ENGINE)) {
			board.showEngineAnalysisWidget();
		} else {
			board.hideEngineAnalysisWidget();
		}
	}

	/**
	 * Handles the first action. The default implementation does nothing.
	 */
	public void onFirst() {

	}

	/**
	 * Flips the ChessBoard object. If the game is bughouse the other board is
	 * flipped too.
	 */
	public void onFlip() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("onFlip");
		}
		onFlipIgnoreBughouseOtherBoard();

		if (game.getVariant() == Variant.bughouse) {
			// Code to flip the other board if its bughouse.
			BughouseGame bughouseGame = null;

			if (game instanceof BughouseGame) {
				bughouseGame = (BughouseGame) game;
			} else if (game instanceof GameCursor) {
				GameCursor cursor = (GameCursor) game;
				if (cursor.getMasterGame() instanceof BughouseGame) {
					bughouseGame = (BughouseGame) cursor.getMasterGame();
				}
			}
			if (bughouseGame != null && bughouseGame.getOtherBoard() != null) {
				ChessBoardWindowItem otherBoardItem = Raptor.getInstance()
						.getWindow().getChessBoardWindowItem(
								bughouseGame.getOtherBoard().getId());
				if (otherBoardItem != null) {
					otherBoardItem.getController()
							.onFlipIgnoreBughouseOtherBoard();
				}
			}
		}
	}

	/**
	 * Flips the ChessBoard object. If the game is bughouse the other board is
	 * not flipped.
	 */
	public void onFlipIgnoreBughouseOtherBoard() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("onFlip");
		}
		board.setWhiteOnTop(!board.isWhiteOnTop());
		board.setWhitePieceJailOnTop(!board.isWhitePieceJailOnTop());
		board.redrawSquares();
		board.getControl().layout(true, true);
		board.getControl().redraw();
	}

	/**
	 * Handles the forward action. The default implementation does nothing.
	 */
	public void onForward() {

	}

	/**
	 * Handles the last action. The default implementation does nothing.
	 */
	public void onLast() {

	}

	/**
	 * Handles the on move list action. The default implementation toggles the
	 * showing of the move list.
	 */
	public void onMoveList() {
		if (isToolItemSelected(ToolBarItemKey.MOVE_LIST)) {
			board.showMoveList();
		} else {
			board.hideMoveList();
		}
	}

	/**
	 * Invoked when the ChessBoard is being hidden from the user. The default
	 * implementation does nothing.
	 */
	public void onPassivate() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("In onPassivate : " + game.getId() + " "
					+ game.getHeader(PgnHeader.Event));
		}
	}

	/**
	 * Handles the revert action. The default implementation does nothing.
	 */
	public void onRevert() {

	}

	/**
	 * Adjusts all of the ChessBoard to the state of the board object. The
	 * clocks are refreshed from the game.
	 */
	public void refresh() {
		refresh(true);
	}

	/**
	 * The same as refresh, except you can control if the clocks are being
	 * updated or not.
	 * 
	 * @param isUpdatingClocks
	 *            True if the clocks should be updated, false otherwise.
	 */
	public void refresh(boolean isUpdatingClocks) {
		if (isDisposed()) {
			return;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("refresh " + getGame().getId() + " ...");
		}
		long startTime = System.currentTimeMillis();

		adjustGameDescriptionLabel();
		adjustPremoveLabelHighlightsAndArrows();
		adjustGameStatusLabel();
		adjustOpeningDescriptionLabel();
		adjustNameRatingLabels();
		adjustLagLabels();
		refreshClocks(isUpdatingClocks);
		adjustPieceJailVisibility();
		adjustBoard();
		adjustPieceJail();
		board.redrawSquares();

		if (LOG.isDebugEnabled()) {
			LOG
					.debug("Completed refresh of game " + getGame().getId()
							+ "  in "
							+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

	/**
	 * Removes an item change listener from this controller.
	 */
	public void removeItemChangedListener(ItemChangedListener listener) {
		itemChangedListeners.remove(listener);
	}

	/**
	 * Sets the ChessBoard this controller is controlling.
	 */
	public void setBoard(ChessBoard board) {
		this.board = board;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	/**
	 * Sets the list of ItemChangeListeners on this controller.
	 */
	public void setItemChangedListeners(
			List<ItemChangedListener> itemChangedListeners) {
		this.itemChangedListeners = itemChangedListeners;
	}

	/**
	 * Sets the enabled state of the tool item matching the specified key.
	 */
	public void setToolItemEnabled(ToolBarItemKey key, boolean isEnabled) {
		ToolItem item = getToolItem(key);
		if (item != null) {
			item.setEnabled(isEnabled);
		}
	}

	/**
	 * Sets the selection state of the tool item matching the specified key.
	 * This is useful for dealing with radio groups. The auto promotes are in a
	 * radio group.
	 */
	public void setToolItemSelected(ToolBarItemKey key, boolean isSelected) {
		ToolItem item = getToolItem(key);
		if (item != null) {
			item.setSelection(isSelected);
		}
	}

	/**
	 * Invoked when the user cancels a move.
	 */
	public abstract void userCancelledMove(int fromSquare);

	/**
	 * Invoked when the user initiates a move.
	 * 
	 * @param square
	 *            The square the move starts from.
	 * @param isDnd
	 *            True if this is a drag and drop move, false if its a click
	 *            click move.
	 */
	public abstract void userInitiatedMove(int square);

	/**
	 * Invoked when the user has made a move.
	 * 
	 * @param fromSquare
	 *            The from square.
	 * @param toSquare
	 *            The two square.
	 */
	public abstract void userMadeMove(int fromSquare, int toSquare);

	/**
	 * Invoked when the user mouse wheels. The count is the intensity of the
	 * wheel. A positive number is an up wheel. A negative number is a down
	 * wheel.
	 */
	public abstract void userMouseWheeled(int count);

	/**
	 * Invoked when the user presses a mouse button over a square.
	 * 
	 * @param button
	 *            The MouseButton clicked.
	 * @param square
	 *            THe square clicked on.
	 */
	public abstract void userPressedMouseButton(MouseButtonAction button,
			int square);

	/**
	 * Invoked when the move list is clicked on. THe halfMoveNumber is the move
	 * selected.
	 * 
	 * The default implementation does nothing. It can be overridden to provide
	 * functionality.
	 */
	public void userSelectedMoveListMove(int halfMoveNumber) {

	}

	/**
	 * Adds arrows and highlights based on preference settings and isUserMove.
	 * 
	 * @param move
	 * @param isUserMove
	 */
	protected void addDecorationsForMove(Move move, boolean isUserMove) {
		if (move == null) {
			return;
		}

		if (!isUserMove) {
			if (getPreferences().getBoolean(
					PreferenceKeys.HIGHLIGHT_SHOW_ON_OBS_AND_OPP_MOVES)) {
				board
						.getSquareHighlighter()
						.addHighlight(
								new Highlight(
										move.getFrom(),
										move.getTo(),
										getPreferences()
												.getColor(
														PreferenceKeys.HIGHLIGHT_OBS_OPP_COLOR),
										getPreferences()
												.getBoolean(
														PreferenceKeys.HIGHLIGHT_FADE_AWAY_MODE)));
			}

			if (getPreferences().getBoolean(
					PreferenceKeys.ARROW_SHOW_ON_OBS_AND_OPP_MOVES)) {
				board.getArrowDecorator().addArrow(
						new Arrow(move.getFrom(), move.getTo(),
								getPreferences().getColor(
										PreferenceKeys.ARROW_OBS_OPP_COLOR),
								getPreferences().getBoolean(
										PreferenceKeys.ARROW_FADE_AWAY_MODE)));
			}
		} else {
			if (getPreferences().getBoolean(HIGHLIGHT_SHOW_ON_MY_MOVES)) {
				board
						.getSquareHighlighter()
						.addHighlight(
								new Highlight(
										move.getFrom(),
										move.getTo(),
										getPreferences().getColor(
												HIGHLIGHT_MY_COLOR),
										getPreferences()
												.getBoolean(
														PreferenceKeys.HIGHLIGHT_FADE_AWAY_MODE)));
			}
			if (getPreferences().getBoolean(
					PreferenceKeys.ARROW_SHOW_ON_MY_MOVES)) {
				board.getArrowDecorator().addArrow(
						new Arrow(move.getFrom(), move.getTo(),
								getPreferences().getColor(
										PreferenceKeys.ARROW_MY_COLOR),
								getPreferences().getBoolean(
										PreferenceKeys.ARROW_FADE_AWAY_MODE)));
			}
		}
	}

	/**
	 * Adjusts only the ChessBoard squares to match the squares in the game.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	protected void adjustBoard() {
		if (isDisposed()) {
			return;
		}
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.getSquare(i, j).setPiece(
						GameUtils.getColoredPiece(GameUtils.getSquare(i, j),
								game));
			}
		}
	}

	/**
	 * Adjusts only the colors of the chess clocks based on whose move it is in
	 * the game.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	protected void adjustClockColors() {
		if (isDisposed()) {
			return;
		}

		Color activeColor = getPreferences().getColor(
				PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR);
		Color inactiveColor = getPreferences().getColor(
				PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR);
		Color nameLabelInactive = getPreferences().getColor(
				PreferenceKeys.BOARD_CONTROL_COLOR);

		boolean isWhitesMove = getGame().getColorToMove() == WHITE;

		board.getWhiteClockLabel().setForeground(
				isWhitesMove ? activeColor : inactiveColor);
		board.getWhiteNameRatingLabel().setForeground(
				isWhitesMove ? activeColor : nameLabelInactive);
		board.getBlackClockLabel().setForeground(
				!isWhitesMove ? activeColor : inactiveColor);
		board.getBlackNameRatingLabel().setForeground(
				!isWhitesMove ? activeColor : nameLabelInactive);
	}

	/**
	 * Sets the values of the lag labels to match those of the game object. The
	 * colors of the labels are changed if lag is greater than 20 seconds.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	protected void adjustLagLabels() {
		if (isDisposed()) {
			return;
		}

		long[] lagTimes = getLagTimes();
		if (lagTimes[WHITE] > 20000) {
			board.getWhiteLagLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR));
		} else {
			board.getWhiteLagLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_CONTROL_COLOR));
		}

		if (lagTimes[BLACK] > 20000) {
			board.getBlackLagLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR));
		} else {
			board.getBlackLagLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_CONTROL_COLOR));
		}

		board.whiteLagLabel.setText(ChessBoardUtils
				.lagToString(lagTimes[WHITE]));
		board.blackLagLabel.setText(ChessBoardUtils
				.lagToString(lagTimes[BLACK]));
	}

	/**
	 * Sets the values of the name and rating labels on the board to those of
	 * the game object.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	protected void adjustNameRatingLabels() {
		if (isDisposed()) {
			return;
		}
		String blackName = getGame().getHeader(PgnHeader.Black);
		String blackRating = getGame().getHeader(PgnHeader.BlackElo);
		String whiteName = getGame().getHeader(PgnHeader.White);
		String whiteRating = getGame().getHeader(PgnHeader.WhiteElo);

		String whiteNameRating = null;
		String blackNameRating = null;

		if (StringUtils.isBlank(whiteName)) {
			whiteNameRating = "White";
		} else if (StringUtils.isBlank(whiteRating)) {
			whiteNameRating = whiteName;
		} else {
			whiteNameRating = whiteName + " " + whiteRating;
		}

		if (StringUtils.isBlank(blackName)) {
			blackNameRating = "Black";
		} else if (StringUtils.isBlank(blackRating)) {
			blackNameRating = blackName;
		} else {
			blackNameRating = blackName + " " + blackRating;
		}

		board.blackNameRatingLabel.setText(blackNameRating);
		board.whiteNameRatingLabel.setText(whiteNameRating);
	}

	/**
	 * Adjusts the opening description label on the chess board. The default
	 * implementation uses the ECOParser to populate this field based on the
	 * state of the game object.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	protected void adjustOpeningDescriptionLabel() {
		if (isDisposed()) {
			return;
		}

		String eco = getGame().getHeader(PgnHeader.ECO);
		String opening = getGame().getHeader(PgnHeader.Opening);

		String description = StringUtils.isBlank(eco) ? "" : "(" + eco + ") ";
		description += StringUtils.isBlank(opening) ? "" : opening;
		board.getOpeningDescriptionLabel().setText(description);
	}

	/**
	 * Adjusts the contents of the piece jail based on the game state.
	 */
	protected void adjustPieceJail() {
		if (isDisposed()) {
			return;
		}

		if (game.isInState(Game.DROPPABLE_STATE)) {
			// Droppable piece games like crazyhouse and bughouse flow through
			// here.
			// The dropCount is used for the piece count in the jail.
			for (int i = 0; i < DROPPABLE_PIECES.length; i++) {
				int coloredPiece = DROPPABLE_PIECE_COLOR[i];
				int count = 0;
				PieceJailChessSquare square = board.pieceJailSquares[DROPPABLE_PIECES[i]];

				count = getGame().getDropCount(
						coloredPiece,
						ChessBoardUtils
								.pieceFromColoredPiece(DROPPABLE_PIECES[i]));

				if (count == 0) {
					square.setPiece(GameConstants.EMPTY);
				} else {
					square.setPiece(DROPPABLE_PIECES[i]);
				}
				square.setText(ChessBoardUtils.pieceCountToString(count));
				square.redraw();
			}
		} else {
			// Non-Droppable piece games like classic chess flow through here.
			// The result from game.getPieceJailCounts are used to populate the
			// piece jail.
			int[] whitePieceJailCounts = game.getPieceJailCounts(WHITE);
			int[] blackPieceJailCounts = game.getPieceJailCounts(BLACK);

			for (int i = 1; i < whitePieceJailCounts.length; i++) {
				int piece = i;
				int coloredPiece = GameUtils.getColoredPiece(piece, WHITE);
				int count = whitePieceJailCounts[i];
				updateDropPieceCount(count, coloredPiece);
			}
			for (int i = 1; i < blackPieceJailCounts.length; i++) {
				int piece = i;
				int coloredPiece = GameUtils.getColoredPiece(piece, BLACK);
				int count = blackPieceJailCounts[i];
				updateDropPieceCount(count, coloredPiece);
			}
		}
	}

	/**
	 * Adjusts the piece jail. The default behavior is to always show it if the
	 * game is in a DROPPABLE_STATE, otherwise only show it if the
	 * BOARD_IS_SHOWING_PIECE_JAIL preference is set to true.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	protected void adjustPieceJailVisibility() {
		if (isDisposed()) {
			return;
		}

		if (!getGame().isInState(Game.DROPPABLE_STATE)
				&& !getPreferences().getBoolean(
						PreferenceKeys.BOARD_IS_SHOWING_PIECE_JAIL)) {
			board.hidePieceJail();
		} else {
			board.showPieceJail();
		}
	}

	/**
	 * Adjusts the boards premove label. The default implementation shows an
	 * empty string. This method should be overridden to provide premove
	 * support.
	 */
	protected void adjustPremoveLabelHighlightsAndArrows() {
		if (isDisposed()) {
			return;
		}
		board.currentPremovesLabel.setText("");
	}

	/**
	 * Returns an array indexed by color containing the accumulated lag in
	 * milliseconds.
	 */
	protected long[] getLagTimes() {

		long whiteLag = 0;
		long blackLag = 0;

		try {
			whiteLag = Long.parseLong(game.getHeader(PgnHeader.WhiteLagMillis));
		} catch (NumberFormatException nfe) {
		} catch (NullPointerException npe) {
		}

		try {
			blackLag = Long.parseLong(game.getHeader(PgnHeader.BlackLagMillis));
		} catch (NumberFormatException nfe) {
		} catch (NullPointerException npe) {
		}
		return new long[] { whiteLag, blackLag };
	}

	/**
	 * Returns the preferences.
	 */
	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	/**
	 * Returns an array indexed by color containing the remaining time in
	 * milliseconds.
	 */
	protected long[] getRemainingTimes() {

		long whiteTime = 0;
		long blackTime = 0;

		try {
			whiteTime = Long.parseLong(game
					.getHeader(PgnHeader.WhiteRemainingMillis));
		} catch (NumberFormatException nfe) {
		} catch (NullPointerException npe) {
		}

		try {
			blackTime = Long.parseLong(game
					.getHeader(PgnHeader.BlackRemainingMillis));
		} catch (NumberFormatException nfe) {
		} catch (NullPointerException npe) {
		}
		return new long[] { whiteTime, blackTime };
	}

	/**
	 * If the clock updaters have not been intiailized, they are initialized.
	 */
	protected void initClockUpdaters() {
		if (whiteClockUpdater == null) {
			whiteClockUpdater = new ClockLabelUpdater(true, this, false);
			blackClockUpdater = new ClockLabelUpdater(false, this, false);
		}
	}

	/**
	 * Refreshes only the board and the piece jail.
	 */
	protected void refreshBoard() {
		adjustBoard();
		adjustPieceJail();
		board.redrawSquares();
	}

	/**
	 * Does a full refresh on the clocks adjusting the labels and colors.
	 * 
	 * @param updateClocksFromGame
	 *            If true is passed in the values of the clocks are set to the
	 *            values in the game. If false is passed in the values of the
	 *            clocks are set to the values of the internal clock updaters
	 *            managing the time remaining on each clock.
	 */
	protected void refreshClocks(boolean updateClocksFromGame) {
		if (isDisposed()) {
			return;
		}
		initClockUpdaters();
		whiteClockUpdater.stop();
		blackClockUpdater.stop();

		adjustClockColors();

		if (updateClocksFromGame) {

			long[] remainingTimes = getRemainingTimes();
			board.whiteClockLabel.setText(GameUtils.timeToString(
					remainingTimes[WHITE], false));
			board.blackClockLabel.setText(GameUtils.timeToString(
					remainingTimes[BLACK], false));

			whiteClockUpdater.setRemainingTimeMillis(remainingTimes[WHITE]);
			blackClockUpdater.setRemainingTimeMillis(remainingTimes[BLACK]);
		} else {
			board.whiteClockLabel.setText(GameUtils.timeToString(
					whiteClockUpdater.getRemainingTimeMillis(), false));
			board.blackClockLabel.setText(GameUtils.timeToString(
					blackClockUpdater.getRemainingTimeMillis(), false));
		}

		if (getGame().isInState(Game.IS_CLOCK_TICKING_STATE)) {
			if (getGame().getColorToMove() == WHITE) {
				whiteClockUpdater.start();
			} else {
				blackClockUpdater.start();
			}
		}
	}

	/**
	 * Refreshes the board just for this move. Does not update the game object,
	 * only refreshes the board squares.
	 */
	protected void refreshForMove(Move move) {
		int fromPiece = board.getSquare(move.getFrom()).getPiece();

		addDecorationsForMove(move, true);
		if (move.isEnPassant()) {
			int epSquare = move.isWhitesMove() ? move.getTo() - 8 : move
					.getTo() + 8;
			board.getSquare(move.getTo()).setPiece(fromPiece);
			board.getSquare(epSquare).setPiece(EMPTY);
			board.getSquare(move.getFrom()).setPiece(EMPTY);
		} else if (move.isDrop()) {
			board.getSquare(move.getTo()).setPiece(fromPiece);
		} else {
			board.getSquare(move.getFrom()).setPiece(EMPTY);
			board.getSquare(move.getTo()).setPiece(fromPiece);
		}
		board.redrawSquares();
	}

	/**
	 * Refreshes only the piece jail.
	 */
	protected void refreshPieceJail() {
		adjustPieceJail();
		board.redrawSquares();
	}

	protected void removeAllMoveDecorations() {
		board.getArrowDecorator().removeAllArrows();
		board.getSquareHighlighter().removeAllHighlights();
	}

	protected void setGame(Game game) {
		this.game = game;
	}

	/**
	 * Stops the chess clocks. This method does NOT adjust the times or the
	 * colors on the clocks.
	 */
	protected void stopClocks() {
		if (isDisposed()) {
			return;
		}

		whiteClockUpdater.stop();
		blackClockUpdater.stop();
	}

	protected void updateDropPieceCount(int count, int coloredPiece) {
		PieceJailChessSquare square = board.getPieceJailSquare(coloredPiece);
		if (count == 0) {
			square.setPiece(GameConstants.EMPTY);
		} else {
			square.setPiece(coloredPiece);
		}
		square.setText(ChessBoardUtils.pieceCountToString(count));
		square.redraw();
	}
}
