package raptor.swt.chess;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.Game.Result;
import raptor.game.util.ECOParser;
import raptor.game.util.GameUtils;
import raptor.game.util.MoveListTraverser;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.ItemChangedListener;

/**
 * A chess board controller manages user adjustments to the ChessBoard and also
 * manages the Game its constructed with.
 * 
 * It manipulates both the game object and the ChessBoard object it is
 * controlling.
 * 
 * 
 * This is the base controller class providing method implementations to do many
 * things. The idea is to override a method if you want to change the
 * functionality.
 */
public abstract class ChessBoardController implements BoardConstants,
		GameConstants {
	static final Log LOG = LogFactory.getLog(ChessBoardController.class);

	protected ClockLabelUpdater blackClockUpdater;
	protected ChessBoard board;
	protected MoveListTraverser traverser;
	protected ClockLabelUpdater whiteClockUpdater;
	protected Game game;
	protected boolean storedIsWhiteOnTop;
	protected boolean storedIsWhitePieceJailOnTop;
	protected List<ItemChangedListener> itemChangedListeners = new ArrayList<ItemChangedListener>(
			5);

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
		traverser = new MoveListTraverser(game);
	}

	/**
	 * Adds an item changed listener. This listener should be invoked whenever
	 * the meta information about the game changes e.g. if it is closable, its
	 * title, its icon, etc.
	 */
	public void addItemChangedListener(ItemChangedListener listener) {
		itemChangedListeners.add(listener);
	}

	/**
	 * Adjusts only the ChessBoard squares to match the squares in the game.
	 */
	protected void adjustBoardToGame(Game game) {
		if (isBeingReparented()) {
			return;
		}

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.getSquare(i, j).setPiece(
						BoardUtils.getColoredPiece(GameUtils.rankFileToSquare(
								i, j), game));
			}
		}
	}

	/**
	 * Adjusts only the colors of the chess clocks. If the game is in an
	 * IS_CLOCK_TICKING_STATE the colors are set to inactive/active based on
	 * whose move it is in the game.
	 * 
	 * Otherwise they are always set to the inactive color.
	 */
	protected void adjustClockColors() {
		if (isBeingReparented()) {
			return;
		}

		if (getGame().isInState(Game.IS_CLOCK_TICKING_STATE)) {
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
		} else {
			board.getWhiteClockLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
			board.getBlackClockLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
		}
	}

	/**
	 * Sets the times on the clock to the times in the game.
	 * 
	 * Sets the times in the whiteClockUpdater and blackClockUpdater to the
	 * times of the game.
	 * 
	 * Does NOT start or stop the clocks.
	 */
	protected void adjustClockLabelsAndUpdaters() {
		if (isBeingReparented()) {
			return;
		}

		board.whiteClockLabel.setText(BoardUtils.timeToString(getGame()
				.getWhiteRemainingTimeMillis()));
		board.blackClockLabel.setText(BoardUtils.timeToString(getGame()
				.getBlackRemainingTimeMillis()));

		whiteClockUpdater.setRemainingTimeMillis(getGame()
				.getWhiteRemainingTimeMillis());
		blackClockUpdater.setRemainingTimeMillis(getGame()
				.getBlackRemainingTimeMillis());
	}

	/**
	 * This method should be overridden to add items to the ChessBoards coolbar.
	 */
	protected abstract void adjustCoolbarToInitial();

	/**
	 * This class provides functionality for handling a nav button change.
	 * 
	 * Nav buttons adjust the traverser.
	 * 
	 * This method change the state of the ChessBoard to that of the traverser.
	 */
	protected void adjustFromNavigationChange() {
		if (isBeingReparented()) {
			return;
		}

		adjustPieceJailFromGame(traverser.getAdjustedGame());
		adjustBoardToGame(traverser.getAdjustedGame());
		adjustNavButtonEnabledState();
		if (traverser.getTraverserHalfMoveIndex() > 0) {
			Move move = traverser.getAdjustedGame().getMoveList().get(
					traverser.getTraverserHalfMoveIndex() - 1);
			String moveDescription = move.getSan();

			if (StringUtils.isBlank(moveDescription)) {
				moveDescription = move.getLan();
			}
			board.statusLabel.setText("Position after move "
					+ BoardUtils.halfMoveIndexToDescription(traverser
							.getTraverserHalfMoveIndex(), GameUtils
							.getOppositeColor(traverser.getAdjustedGame()
									.getColorToMove())) + moveDescription);
		} else {
			board.statusLabel.setText("");
		}
		board.forceUpdate();
	}

	/**
	 * Adjusts the games description label. Should be overridden to provide a
	 * description of the game.
	 */
	public abstract void adjustGameDescriptionLabel();

	/**
	 * Adjusts the game status label. The status label should contain
	 * information such as the last move made, and a detailed result of the game
	 * if it is over.
	 */
	public void adjustGameStatusLabel() {
		if (isBeingReparented()) {
			return;
		}

		if (getGame().isInState(Game.ACTIVE_STATE)) {
			if (getGame().getMoveList().getSize() > 0) {
				Move lastMove = getGame().getMoveList().get(
						getGame().getMoveList().getSize() - 1);
				int moveNumber = (getGame().getHalfMoveCount() / 2) + 1;

				board.getStatusLabel().setText(
						"Last Move: " + moveNumber + ") "
								+ (lastMove.isWhitesMove() ? "" : "...")
								+ lastMove.toString());

			} else {
				board.getStatusLabel().setText("");
			}
		} else {
			String result = getGame().getResultDescription();
			if (result != null) {
				board.getStatusLabel()
						.setText(getGame().getResultDescription());
			}
		}
	}

	/**
	 * Adjusts the colors of the lag labels based on the white lag and black lag
	 * in the game object. If lag is over 20 seconds the color changes based on
	 * the BOARD_LAG_OVER_20_SEC_COLOR preference.
	 */
	public void adjustLagColors() {
		if (isBeingReparented()) {
			return;
		}
		if (getGame().getWhiteLagMillis() > 20000) {
			board.getWhiteLagLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR));
		} else {
			board.getWhiteLagLabel().setForeground(
					getPreferences().getColor(PreferenceKeys.BOARD_LAG_COLOR));
		}

		if (getGame().getBlackLagMillis() > 20000) {
			board.getBlackLagLabel().setForeground(
					getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR));
		} else {
			board.getBlackLagLabel().setForeground(
					getPreferences().getColor(PreferenceKeys.BOARD_LAG_COLOR));
		}
	}

	/**
	 * Sets the values of the lag labels to match those of the game object.
	 */
	protected void adjustLagLabels() {
		if (isBeingReparented()) {
			return;
		}
		board.whiteLagLabel.setText(BoardUtils.lagToString(getGame()
				.getWhiteLagMillis()));
		board.blackLagLabel.setText(BoardUtils.lagToString(getGame()
				.getBlackLagMillis()));
	}

	/**
	 * Sets the values of the name and rating labels on the board to those of
	 * the game object.
	 */
	protected void adjustNameRatingLabels() {
		if (isBeingReparented()) {
			return;
		}
		String blackName = getGame().getBlackName();
		String blackRating = getGame().getBlackRating();
		String whiteName = getGame().getWhiteName();
		String whiteRating = getGame().getWhiteRating();

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
	 * Adjusts the state of the nav buttons. The default implementation uses the
	 * traverser and enables/disables the buttons based on there being moves
	 * available in the traverser (e.g. if the user is at the end of the game
	 * LAST will no longer be available).
	 */
	protected void adjustNavButtonEnabledState() {
		if (isBeingReparented()) {
			return;
		}
		if (isMoveListTraversable()) {
			board.setCoolBarButtonEnabled(traverser.hasFirst(),
					ChessBoard.FIRST_NAV);
			board.setCoolBarButtonEnabled(traverser.hasLast(),
					ChessBoard.LAST_NAV);
			board.setCoolBarButtonEnabled(traverser.hasNext(),
					ChessBoard.NEXT_NAV);
			board.setCoolBarButtonEnabled(traverser.hasBack(),
					ChessBoard.BACK_NAV);
		} else {
			board.setCoolBarButtonEnabled(false, ChessBoard.FIRST_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.LAST_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.NEXT_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.BACK_NAV);
		}
	}

	/**
	 * Adjusts the opening description label on the chess board. The default
	 * implementation uses the ECOParser to populate this field based on the
	 * state of the game object.
	 */
	protected void adjustOpeningDescriptionLabel() {
		if (isBeingReparented()) {
			return;
		}
		LOG.info("adjustOpeningDescriptionLabel()");
		// if (board.game.getState() == Game.EXAMINING_STATE) return;

		// CDay tells you: you can do
		// board.getOpeningDescriptionLabel().setText(your opening
		// description);
		// CDay tells you: so you can test it from the gui

		ECOParser p = ECOParser.getECOParser(getGame());
		if (p != null) {
			LOG.info("ECOParser.getECOParser(getGame()) = " + p.toString());
			board.getOpeningDescriptionLabel().setText(p.toString());
		}
	}

	/**
	 * Adjusts the contents of the piece jail based on the game.
	 * 
	 * If the game is not droppable the piece jail contains the pieces the
	 * player has captured during the game.
	 * 
	 * *TO DO add adjustments when game is droppable.
	 */
	protected void adjustPieceJailFromGame(Game game) {
		if (isBeingReparented()) {
			return;
		}
		for (int i = 0; i < DROPPABLE_PIECES.length; i++) {
			int color = DROPPABLE_PIECE_COLOR[i];
			int count = INITIAL_DROPPABLE_PIECE_COUNTS[i]
					- getGame()
							.getPieceCount(
									color,
									BoardUtils
											.pieceFromColoredPiece(DROPPABLE_PIECES[i]));

			if (count == 0) {
				board.pieceJailSquares[DROPPABLE_PIECES[i]]
						.setPiece(GameConstants.EMPTY);
			} else {
				board.pieceJailSquares[DROPPABLE_PIECES[i]]
						.setPiece(DROPPABLE_PIECES[i]);
			}

			board.pieceJailSquares[DROPPABLE_PIECES[i]].setText(BoardUtils
					.pieceCountToString(count));
			board.pieceJailSquares[DROPPABLE_PIECES[i]].redraw();
		}
	}

	/**
	 * Adjusts the boards premove label. The default implementation shows an
	 * empty string. This method should be overridden to provide premove
	 * support.
	 */
	protected void adjustPremoveLabel() {
		if (isBeingReparented()) {
			return;
		}
		board.currentPremovesLabel.setText("");
	}

	/**
	 * Adjusts the ChessBoard to a game change without modifying either the
	 * boards position or the piece jails.
	 */
	protected void adjustToGameChangeNotInvolvingMove() {
		if (isBeingReparented()) {
			return;
		}
		// Adjust the clocks.
		stopClocks();
		adjustClockLabelsAndUpdaters();
		adjustClockColors();
		startClocks();

		adjustToMoveIndicatorLabel();

		if (getGame().getResult() != Result.IN_PROGRESS) {
			onPlayGameEndSound();
		}
	}

	/**
	 * Adjusts all of the ChessBoard to the state of the board object.
	 */
	public void adjustToGameInitial() {
		if (isBeingReparented()) {
			return;
		}

		LOG.info("adjustToGameInitial " + getGame().getId() + " ...");
		long startTime = System.currentTimeMillis();
		initClockUpdaters();
		adjustNameRatingLabels();
		adjustGameDescriptionLabel();

		adjustToGameChangeNotInvolvingMove();

		adjustBoardToGame(getGame());
		adjustPieceJailFromGame(getGame());

		traverser.adjustHalfMoveIndex();
		adjustNavButtonEnabledState();

		adjustLagLabels();
		adjustLagColors();

		adjustPremoveLabel();

		adjustOpeningDescriptionLabel();
		adjustGameStatusLabel();

		board.forceUpdate();

		LOG.info("Completed adjustToGameInitial in " + getGame().getId() + "  "
				+ (System.currentTimeMillis() - startTime));

	}

	/**
	 * Adjusts only the fields of the ChessBoard that change during a move. e.g.
	 * the clocks/lag,board,piece jail,opening description,game status,nav
	 * button state
	 */
	protected void adjustToGameMove() {
		if (isBeingReparented()) {
			return;
		}

		LOG.info("adjustToGameChange " + getGame().getId() + " ...");
		long startTime = System.currentTimeMillis();

		adjustToGameChangeNotInvolvingMove();

		adjustBoardToGame(getGame());
		adjustPieceJailFromGame(getGame());

		traverser.adjustHalfMoveIndex();
		adjustNavButtonEnabledState();

		adjustLagLabels();
		adjustLagColors();

		adjustPremoveLabel();

		adjustOpeningDescriptionLabel();
		adjustGameStatusLabel();

		if (getGame().getResult() == Result.IN_PROGRESS) {
			onPlayMoveSound();
		}

		board.forceUpdate();

		LOG.info("adjustToGameChange " + getGame().getId() + "  n "
				+ (System.currentTimeMillis() - startTime));

	}

	/**
	 * Adjusts the to move indicator based on whose move it is in the game.
	 */
	public void adjustToMoveIndicatorLabel() {
		if (isBeingReparented()) {
			return;
		}

		if (getGame().getColorToMove() == WHITE) {
			board.getWhiteToMoveIndicatorLabel().setImage(
					Raptor.getInstance().getIcon("circle_green30x30"));
			board.getBlackToMoveIndicatorLabel().setImage(
					Raptor.getInstance().getIcon("circle_gray30x30"));
		} else {
			board.getBlackToMoveIndicatorLabel().setImage(
					Raptor.getInstance().getIcon("circle_green30x30"));
			board.getWhiteToMoveIndicatorLabel().setImage(
					Raptor.getInstance().getIcon("circle_gray30x30"));
		}
	}

	/**
	 * Returns true if a user can begin making a move from the specified square.
	 */
	public abstract boolean canUserInitiateMoveFrom(int squareId);

	/**
	 * Invoked when the user desires to clear out all premoves. The default
	 * implementation is to set the text to "".
	 * 
	 * This method should be overridden if premove support is provided by the
	 * controller.
	 */
	public void clearPremoves() {
		if (!isBeingReparented()) {
			board.currentPremovesLabel.setText("");
		}
	}

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
		if (traverser != null) {
			traverser.dispose();
			traverser = null;
		}

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
	 * Returns the MoveListTraverser this controller uses to handle nav changes.
	 * 
	 * @return
	 */
	public MoveListTraverser getGameTraverser() {
		return traverser;
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
	 * Returns the preferences.
	 */
	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	/**
	 * Should be overridden to show a suitable title for the game. The result
	 * should be short 10-12 chars max.
	 */
	public abstract String getTitle();

	/**
	 * Initializes the ChessBoardController. A ChessBoard should be set on the
	 * controller prior to calling this method.
	 * 
	 * The default behavior is: invoke adjustCooolbarToInitial (To add all of
	 * the coolbar items) onPlayStartSound() (To play the sound associated with
	 * a game starting) adjustToGameInitial (To adjust the ChessBoard to the
	 * state of the game)
	 */
	public void init() {
		adjustCoolbarToInitial();
		onPlayGameStartSound();
		adjustToGameInitial();
	}

	/**
	 * If the clock updaters have not been intiailized, they are initialized.
	 */
	protected void initClockUpdaters() {
		if (whiteClockUpdater == null) {
			whiteClockUpdater = new ClockLabelUpdater(board.whiteClockLabel,
					this.board);
			blackClockUpdater = new ClockLabelUpdater(board.blackClockLabel,
					this.board);
		}
	}

	/**
	 * Should return true if the game is auto-drawable. This method is used by
	 * the ChessBoard class to determine if it will show the auto draw button.
	 */
	public abstract boolean isAutoDrawable();

	/**
	 * SWT does not allow moving around controls to different parents. To
	 * accomplish this the control actually has to be recreated. When this
	 * processes is occurring this method will return true.
	 */
	public boolean isBeingReparented() {
		boolean result = false;
		if (board == null) {
			LOG
					.debug(
							"isBeingTransfered was invoked while board was null",
							new Exception(
									"This exception is just so the stack trace gets logged."));
			result = true;
		}
		return result;
	}

	/**
	 * Returns true if this game is commitable. This method is used by the
	 * ChessBoard class to determine if it will show the commit button.
	 */
	public abstract boolean isCommitable();

	/**
	 * Returns true if this games move list is able to be traversed.
	 */
	public abstract boolean isMoveListTraversable();

	/**
	 * Returns true if the ChessBoard should show the navigation buttons.
	 */
	public abstract boolean isNavigatable();

	/**
	 * Returns true if this chess board controller handles premoves. The default
	 * implementation returns false.
	 */
	public boolean isPremoveable() {
		return false;
	}

	/**
	 * Returns true if the ChessBoard should show the revert button.
	 */
	public abstract boolean isRevertable();

	/**
	 * Invoked when the ChessBoard is being viewed. The default implementation
	 * does nothing.
	 */
	public void onActivate() {
	}

	/**
	 * Invoked whent he user wants to clear premoves. The default implementation
	 * does nothing.
	 */
	public void onClearPremoves() {
	}

	/**
	 * Flips the ChessBoard object.
	 */
	public void onFlip() {
		LOG.debug("onFlip");
		board.setWhiteOnTop(!board.isWhiteOnTop());
		board.setWhitePieceJailOnTop(!board.isWhitePieceJailOnTop());
		board.forceUpdate();
		LOG.debug("isWhiteOnTop = " + board.isWhiteOnTop);
	}

	/**
	 * Invoked when the back nav button is pressed. The default behavior is to
	 * adjust the traverser and invoke adjustFromNavChange.
	 */
	public void onNavBack() {
		if (traverser.hasBack()) {
			traverser.back();
			adjustFromNavigationChange();
		} else {
			LOG.error("Traverser did not have previous so ignoring action");
		}
	}

	/**
	 * Invoked when the user presses the commit nav button. The default behavior
	 * is to do nothing.
	 */
	public void onNavCommit() {
	}

	/**
	 * Invoked when the first nav button is pressed. The default behavior is to
	 * adjust the traverser and invoke adjustFromNavChange.
	 */
	public void onNavFirst() {
		if (traverser.hasFirst()) {
			traverser.first();
			adjustFromNavigationChange();
		} else {
			LOG.error("Traverser did not have first so ignoring action");
		}

	}

	/**
	 * Invoked when the forward nav button is pressed. The default behavior is
	 * to adjust the traverser and invoke adjustFromNavChange.
	 */
	public void onNavForward() {
		if (traverser.hasNext()) {
			traverser.next();
			adjustFromNavigationChange();
		}
	}

	/**
	 * Invoked when the last nav button is pressed. The default behavior is to
	 * adjust the traverser and invoke adjustFromNavChange.
	 */
	public void onNavLast() {
		if (traverser.hasLast()) {
			traverser.last();
			adjustFromNavigationChange();
		}
	}

	/**
	 * Invoked when the revert nav button is pressed. The default behavior is to
	 * do nothing.
	 */
	public void onNavRevert() {
	}

	/**
	 * Invoked when a chess board is no longer visible to a user, however can be
	 * viewed in the future. The default behavior is to do nothing.
	 */
	public void onPassivate() {
	}

	/**
	 * Invoked when a game has ended. Should be implemented to play an
	 * appropriate sound.
	 */
	protected abstract void onPlayGameEndSound();

	/**
	 * Invoked when a game has started. Should be implemented to play an
	 * appropriate sound.
	 */
	protected abstract void onPlayGameStartSound();

	/**
	 * Invoked when a move is made during a game. Should be implemented to play
	 * an appropriate sound.
	 */
	protected abstract void onPlayMoveSound();

	/**
	 * Invoked when the reparenting process is completed.
	 */
	public void onPostReparent() {
		board.setWhiteOnTop(storedIsWhiteOnTop);
		board.setWhitePieceJailOnTop(storedIsWhitePieceJailOnTop);
		storedIsWhiteOnTop = board.isWhiteOnTop();
		storedIsWhitePieceJailOnTop = board.isWhitePieceJailOnTop();
		adjustCoolbarToInitial();
		adjustToGameInitial();
	}

	/**
	 * In SWT controls can't be reprented easily. The control must be recreated.
	 * This method is invoked when the ChessBoard is in the process of being
	 * reprented. It is invoked before the chess board is destroyed. However
	 * during the reparenting process the ChessBoard might be set to null on the
	 * controller. This method prepres for the reparenting.
	 * 
	 * After reparenting is completed onPostReparent is invoked.
	 */
	public void onPreReparent() {
		storedIsWhiteOnTop = board.isWhiteOnTop();
		storedIsWhitePieceJailOnTop = board.isWhitePieceJailOnTop();
		board = null;
		blackClockUpdater.dispose();
		whiteClockUpdater.dispose();
		whiteClockUpdater = null;
		blackClockUpdater = null;
	}

	/**
	 * Invoked when the setup clear button is pressed. Default behavior is to do
	 * nothing.
	 */
	public void onSetupClear() {
	}

	/**
	 * Invoked when the setup done button is pressed. Default behavior is to do
	 * nothing.
	 */
	public void onSetupDone() {
	}

	/**
	 * Invoked when the setup fen button is pressed. Default behavior is to do
	 * nothing.
	 */
	public void onSetupFen(String fen) {
	}

	/**
	 * Invoked when the setup start button is pressed. Default behavior is to do
	 * nothing.
	 */
	public void onSetupStart() {
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

	/**
	 * Sets the list of ItemChangeListeners on this controller.
	 */
	public void setItemChangedListeners(
			List<ItemChangedListener> itemChangedListeners) {
		this.itemChangedListeners = itemChangedListeners;
	}

	/**
	 * Starts the chess clocks. This method does NOT adjust the times or colors
	 * of the clocks, nor does it adjust the clock updaters.
	 */
	protected void startClocks() {
		if (isBeingReparented()) {
			return;
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
	 * Stops the chess clocks. This method does NOT adjust the times or the
	 * colors on the clocks.
	 */
	protected void stopClocks() {
		if (isBeingReparented()) {
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
