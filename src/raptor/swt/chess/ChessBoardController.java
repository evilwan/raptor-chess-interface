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
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
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
		this.game = game;
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
				PreferenceKeys.BOARD_PLAYER_NAME_COLOR);

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
				Move lastMove = getGame().getMoveList().get(
						getGame().getMoveList().getSize() - 1);
				int moveNumber = getGame().getFullMoveCount();

				board.getStatusLabel().setText(
						"Last Move: "
								+ moveNumber
								+ ") "
								+ (lastMove.isWhitesMove() ? "" : "... ")
								+ GameUtils.convertSanToUseUnicode(lastMove
										.toString(), !game.isWhitesMove()));

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
					getPreferences().getColor(PreferenceKeys.BOARD_LAG_COLOR));
		}

		if (lagTimes[BLACK] > 20000) {
			board.getBlackLagLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR));
		} else {
			board.getBlackLagLabel().setForeground(
					getPreferences().getColor(PreferenceKeys.BOARD_LAG_COLOR));
		}

		board.whiteLagLabel.setText(BoardUtils.lagToString(lagTimes[WHITE]));
		board.blackLagLabel.setText(BoardUtils.lagToString(lagTimes[BLACK]));
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
			whiteNameRating = whiteName + " (" + whiteRating + ")";
		}

		if (StringUtils.isBlank(blackName)) {
			blackNameRating = "Black";
		} else if (StringUtils.isBlank(blackRating)) {
			blackNameRating = blackName;
		} else {
			blackNameRating = blackName + " (" + blackRating + ")";
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

		String description = StringUtils.isBlank(eco) ? "" : eco + " ";
		description += StringUtils.isBlank(opening) ? "" : opening;
		board.getOpeningDescriptionLabel().setText(description);
	}

	/**
	 * Adjusts the contents of the piece jail based on the game state.
	 * 
	 * <pre>
	 * SETUP_STATE:
	 * The piece jail squares contain the game.PieceCounts for the respective piece.
	 * 
	 * DROPPABLE_STATE:
	 * The piece jail squares contain the game.DropCounts for the respective piece.
	 * 
	 * DEFAULT:
	 * The piece jail squares contain the captured piece counts of the respective piece.
	 * </pre>
	 * 
	 * *TO DO add adjustments when game is droppable.
	 */
	protected void adjustPieceJail() {
		if (isDisposed()) {
			return;
		}

		for (int i = 0; i < DROPPABLE_PIECES.length; i++) {
			int coloredPiece = DROPPABLE_PIECE_COLOR[i];
			int count = 0;
			LabeledChessSquare square = board.pieceJailSquares[DROPPABLE_PIECES[i]];

			if (game.isInState(Game.DROPPABLE_STATE)
					&& !game.isInState(Game.SETUP_STATE)) {
				count = getGame().getDropCount(coloredPiece,
						BoardUtils.pieceFromColoredPiece(DROPPABLE_PIECES[i]));
			} else {

				count = INITIAL_DROPPABLE_PIECE_COUNTS[i]
						- getGame()
								.getPieceCount(
										coloredPiece,
										BoardUtils
												.pieceFromColoredPiece(DROPPABLE_PIECES[i]));
			}

			if (game.isInState(Game.SETUP_STATE)) {
				if (count > 0) {
					square.setPiece(coloredPiece);
				} else {
					square.setPiece(EMPTY);
				}
			} else {
				if (count == 0) {
					square.setPiece(GameConstants.EMPTY);
				} else {
					square.setPiece(DROPPABLE_PIECES[i]);
				}
				square.setText(BoardUtils.pieceCountToString(count));
			}
			square.redraw();
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
	protected void adjustPremoveLabel() {
		if (isDisposed()) {
			return;
		}
		board.currentPremovesLabel.setText("");
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
		board = null;

		if (blackClockUpdater != null) {
			blackClockUpdater.stop();
			blackClockUpdater.dispose();
		}
		if (whiteClockUpdater != null) {
			whiteClockUpdater.stop();
			whiteClockUpdater.dispose();
		}

		if (itemChangedListeners != null) {
			itemChangedListeners.clear();
			itemChangedListeners = null;
		}

		game = null;

		LOG.debug("Disposed ChessBoardController");
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
	 * Should be overridden to show a suitable title for the game. The result
	 * should be short 10-12 chars max.
	 */
	public abstract String getTitle();

	public Control getToolbar(Composite parent) {
		return null;
	}

	public ToolItem getToolItem(ToolBarItemKey key) {
		return toolItemMap.get(key);
	}

	/**
	 * Initializes the ChessBoardController. A ChessBoard should be set on the
	 * controller prior to calling this method.
	 */
	public abstract void init();

	/**
	 * If the clock updaters have not been intiailized, they are initialized.
	 */
	protected void initClockUpdaters() {
		if (whiteClockUpdater == null) {
			whiteClockUpdater = new ClockLabelUpdater(board.whiteClockLabel,
					board);
			blackClockUpdater = new ClockLabelUpdater(board.blackClockLabel,
					board);
		}
	}

	/**
	 * Returns true if this controller is being used on a chess board, false
	 * otherwise.
	 */
	public boolean isDisposed() {
		boolean result = false;
		if (board == null) {
			result = true;
		}
		return result;
	}

	/**
	 * Returns true if the specified tool item is selected. Returns false if the
	 * tool item is noexistant or not selected.
	 * 
	 * @param key
	 *            They tool items key.
	 */
	public boolean isToolItemSelected(ToolBarItemKey key) {
		boolean result = false;
		ToolItem item = getToolItem(key);
		if (item != null) {
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
	 * Flips the ChessBoard object.
	 */
	public void onFlip() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("onFlip");
		}
		board.setWhiteOnTop(!board.isWhiteOnTop());
		board.setWhitePieceJailOnTop(!board.isWhitePieceJailOnTop());
		board.redrawSquares();
		board.getBoardComposite().layout(true);
		board.getControl().redraw();
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
	 * Invoked when a toolbar button is pressed.
	 */
	public abstract void onToolbarButtonAction(ToolBarItemKey key,
			String... args);

	/**
	 * Adjusts all of the ChessBoard to the state of the board object. The
	 * clocks are refreshed from the game.
	 */
	public void refresh() {
		if (isDisposed()) {
			return;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("refresh " + getGame().getId() + " ...");
		}
		long startTime = System.currentTimeMillis();

		adjustGameDescriptionLabel();
		adjustPremoveLabel();
		adjustGameStatusLabel();
		adjustOpeningDescriptionLabel();
		adjustNameRatingLabels();
		adjustLagLabels();
		refreshClocks(true);
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
			initClockUpdaters();
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

		if (move.isEnPassant()) {
			board.getSquare(move.getFrom()).highlight();
			board.getSquare(move.getEpSquare()).highlight();
			board.getSquare(move.getTo()).setPiece(fromPiece);
			board.getSquare(move.getEpSquare()).setPiece(EMPTY);
			board.getSquare(move.getFrom()).setPiece(EMPTY);
		} else if (move.isDrop()) {
			board.getSquare(move.getFrom()).highlight();
			board.getSquare(move.getTo()).highlight();
			board.getSquare(move.getTo()).setPiece(fromPiece);
		} else {
			board.getSquare(move.getFrom()).highlight();
			board.getSquare(move.getTo()).highlight();
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

	protected void setGame(Game game) {
		this.game = game;
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

	/**
	 * Invoked when the user cancels a move.
	 */
	public abstract void userCancelledMove(int fromSquare, boolean isDnd);

	/**
	 * Invoked when the move list is clicked on. THe halfMoveNumber is the move
	 * selected.
	 * 
	 * The default implementation does nothing. It can be overridden to provide
	 * functionality.
	 */
	public void userClickedOnMove(int halfMoveNumber) {

	}

	/**
	 * Invoked when the user initiates a move.
	 * 
	 * @param square
	 *            The square the move starts from.
	 * @param isDnd
	 *            True if this is a drag and drop move, false if its a click
	 *            click move.
	 */
	public abstract void userInitiatedMove(int square, boolean isDnd);

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
	 * Invoked when the user middle clicks on a square.
	 * 
	 * @param square
	 *            The square middle clicked on.
	 */
	public abstract void userMiddleClicked(int square);

	/**
	 * Invoked when the user right clicks on a square.
	 * 
	 * @param square
	 *            The square right clicked on.
	 */
	public abstract void userRightClicked(int square);
}
