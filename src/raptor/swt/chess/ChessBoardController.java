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
import raptor.game.MoveListTraverser;
import raptor.game.Game.Result;
import raptor.game.util.ECOParser;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.ItemChangedListener;

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

	public ChessBoardController(Game game) {
		this.game = game;
		traverser = new MoveListTraverser(game);
	}

	public void addItemChangedListener(ItemChangedListener listener) {
		itemChangedListeners.add(listener);
	}

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

	protected abstract void adjustCoolbarToInitial();

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

	public abstract void adjustGameDescriptionLabel();

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

	protected void adjustLagLabels() {
		if (isBeingReparented()) {
			return;
		}
		board.whiteLagLabel.setText(BoardUtils.lagToString(getGame()
				.getWhiteLagMillis()));
		board.blackLagLabel.setText(BoardUtils.lagToString(getGame()
				.getBlackLagMillis()));
	}

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

	protected void adjustPremoveLabel() {
		if (isBeingReparented()) {
			return;
		}
		board.currentPremovesLabel.setText("Premoves: EMPTY");
	}

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

	public abstract boolean canUserInitiateMoveFrom(int squareId);

	public void clearPremoves() {
		if (!isBeingReparented()) {
			board.currentPremovesLabel.setText("");
		}
	}

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
	 * Should be invoked when the title or closeability changes.
	 */
	public void fireItemChanged() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Firing itemChanged");
		}
		for (ItemChangedListener listener : itemChangedListeners) {
			listener.itemStateChanged();
		}
	}

	public ChessBoard getBoard() {
		return board;
	}

	public Game getGame() {
		return game;
	}

	public MoveListTraverser getGameTraverser() {
		return traverser;
	}

	public List<ItemChangedListener> getItemChangedListeners() {
		return itemChangedListeners;
	}

	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	public abstract String getTitle();

	public void init() {
		adjustCoolbarToInitial();
		onPlayGameStartSound();
		adjustToGameInitial();
	}

	protected void initClockUpdaters() {
		if (whiteClockUpdater == null) {
			whiteClockUpdater = new ClockLabelUpdater(board.whiteClockLabel,
					this.board);
			blackClockUpdater = new ClockLabelUpdater(board.blackClockLabel,
					this.board);
		}
	}

	public abstract boolean isAbortable();

	public abstract boolean isAdjournable();

	public abstract boolean isAutoDrawable();

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
	 * Returns true if this game is closeable, false otherwise.
	 */
	public abstract boolean isCloseable();

	public abstract boolean isCommitable();

	public abstract boolean isDrawable();

	public abstract boolean isExaminable();

	public abstract boolean isMoveListTraversable();

	public abstract boolean isNavigatable();

	public abstract boolean isPausable();

	public abstract boolean isRematchable();

	public abstract boolean isResignable();

	public abstract boolean isRevertable();

	public boolean isSureDraw() {
		// TO DO.
		return false;
	}

	public void onActivate() {
	}

	public void onFlip() {
		LOG.debug("onFlip");
		board.setWhiteOnTop(!board.isWhiteOnTop());
		board.setWhitePieceJailOnTop(!board.isWhitePieceJailOnTop());
		board.forceUpdate();
		LOG.debug("isWhiteOnTop = " + board.isWhiteOnTop);
	}

	public void onNavBack() {
		if (traverser.hasBack()) {
			traverser.back();
			adjustFromNavigationChange();
		} else {
			LOG.error("Traverser did not have previous so ignoring action");
		}
	}

	public void onNavCommit() {
		// TO DO
	}

	public void onNavFirst() {
		if (traverser.hasFirst()) {
			traverser.first();
			adjustFromNavigationChange();
		} else {
			LOG.error("Traverser did not have first so ignoring action");
		}

	}

	public void onNavForward() {
		if (traverser.hasNext()) {
			traverser.next();
			adjustFromNavigationChange();
		}
	}

	public void onNavLast() {
		if (traverser.hasLast()) {
			traverser.last();
			adjustFromNavigationChange();
		}
	}

	public void onNavRevert() {
		// TO DO
	}

	public void onPassivate() {
	}

	protected abstract void onPlayGameEndSound();

	protected abstract void onPlayGameStartSound();

	protected abstract void onPlayMoveSound();

	public void onPostReparent() {
		board.setWhiteOnTop(storedIsWhiteOnTop);
		board.setWhitePieceJailOnTop(storedIsWhitePieceJailOnTop);
		storedIsWhiteOnTop = board.isWhiteOnTop();
		storedIsWhitePieceJailOnTop = board.isWhitePieceJailOnTop();
		adjustCoolbarToInitial();
		adjustToGameInitial();
	}

	public void onPreReparent() {
		storedIsWhiteOnTop = board.isWhiteOnTop();
		storedIsWhitePieceJailOnTop = board.isWhitePieceJailOnTop();
		board = null;
		blackClockUpdater.dispose();
		whiteClockUpdater.dispose();
		whiteClockUpdater = null;
		blackClockUpdater = null;
	}

	public void onSetupClear() {
	}

	public void onSetupDone() {
	}

	public void onSetupFen(String fen) {
	}

	public void onSetupStart() {
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		itemChangedListeners.remove(listener);
	}

	public void setBoard(ChessBoard board) {
		this.board = board;
	}

	public void setItemChangedListeners(
			List<ItemChangedListener> itemChangedListeners) {
		this.itemChangedListeners = itemChangedListeners;
	}

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

	protected void stopClocks() {
		if (isBeingReparented()) {
			return;
		}

		whiteClockUpdater.stop();
		blackClockUpdater.stop();
	}

	public abstract void userCancelledMove(int fromSquare, boolean isDnd);

	public abstract void userInitiatedMove(int square, boolean isDnd);

	public abstract void userMadeMove(int fromSquare, int toSquare);

	public abstract void userMiddleClicked(int square);

	public abstract void userRightClicked(int square);
}
