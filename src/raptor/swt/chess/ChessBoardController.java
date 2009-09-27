package raptor.swt.chess;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.MoveListTraverser;
import raptor.game.util.ECOParser;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.util.RaptorStringUtils;

public abstract class ChessBoardController implements Constants, GameConstants {
	static final Log LOG = LogFactory.getLog(ChessBoardController.class);

	public static final String LAST_NAV = "last_nav";
	public static final String FORWARD_NAV = "forward_nav";
	public static final String NEXT_NAV = "next_nav";
	public static final String FIRST_NAV = "first_nav";

	protected boolean autoDraw = false;
	protected ClockLabelUpdater blackClockUpdater;
	protected ChessBoard board;

	protected long currentBlackTime;
	protected long currentWhiteTime;
	protected MoveListTraverser traverser;
	protected int promoteType = QUEEN;

	protected ClockLabelUpdater whiteClockUpdater;

	public ChessBoardController() {
	}

	protected void adjustBoardToGame(Game game) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.getSquare(i, j).setPiece(
						Utils.getColoredPiece(GameUtils.rankFileToSquare(i, j),
								game));
			}
		}
	}

	protected void adjustClockColors() {
		if (getGame().isInState(Game.IS_CLOCK_TICKING_STATE)) {
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
		} else {
			board.getWhiteClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
			board.getBlackClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
		}
	}

	protected void adjustClockLabelsAndUpdaters() {
		board.whiteClockLabel.setText(timeToString(getGame()
				.getWhiteRemainingTimeMillis()));
		board.blackClockLabel.setText(timeToString(getGame()
				.getBlackRemainingTimeMillis()));

		whiteClockUpdater.setRemainingTimeMillis(getGame()
				.getWhiteRemainingTimeMillis());
		blackClockUpdater.setRemainingTimeMillis(getGame()
				.getBlackRemainingTimeMillis());
	}

	protected abstract void adjustCoolbarToInitial();

	protected void adjustFromNavigationChange() {
		adjustPieceJailFromGame(traverser.getAdjustedGame());
		adjustBoardToGame(traverser.getAdjustedGame());
		adjustNavButtonEnabledState();
		if (traverser.getTraverserHalfMoveIndex() > 0) {
			Move move = traverser.getAdjustedGame().getMoves().get(
					traverser.getTraverserHalfMoveIndex() - 1);
			String moveDescription = move.getSan();

			if (StringUtils.isBlank(moveDescription)) {
				moveDescription = move.getLan();
			}
			board.statusLabel.setText("Position after move "
					+ Utils.halfMoveIndexToDescription(traverser
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
		if (getGame().isInState(Game.ACTIVE_STATE)) {
			if (getGame().getMoves().getSize() > 0) {
				Move lastMove = getGame().getMoves().get(
						getGame().getMoves().getSize() - 1);
				int moveNumber = (board.getGame().getHalfMoveCount() / 2) + 1;

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
		if (getGame().getWhiteLagMillis() > 20000) {
			board.getWhiteLagLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR));
		} else {
			board.getWhiteLagLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_COLOR));
		}

		if (getGame().getBlackLagMillis() > 20000) {
			board.getBlackLagLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR));
		} else {
			board.getBlackLagLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_LAG_COLOR));
		}
	}

	protected void adjustLagLabels() {
		board.whiteLagLabel.setText(lagToString(getGame().getWhiteLagMillis()));
		board.blackLagLabel.setText(lagToString(getGame().getBlackLagMillis()));
	}

	protected void adjustNameRatingLabels() {
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
		// CDay tells you: you can do
		// board.getOpeningDescriptionLabel().setText(your opening description);
		// CDay tells you: so you can test it from the gui
		ECOParser p = ECOParser.getECOParser(board.getGame());
		if (p != null)
			board.getOpeningDescriptionLabel().setText(p.toString());
	}

	protected void adjustPieceJailFromGame(Game game) {
		for (int i = 0; i < DROPPABLE_PIECES.length; i++) {
			int color = DROPPABLE_PIECE_COLOR[i];
			int count = INITIAL_DROPPABLE_PIECE_COUNTS[i]
					- getGame().getPieceCount(color,
							Utils.pieceFromColoredPiece(DROPPABLE_PIECES[i]));

			if (count == 0) {
				board.pieceJailSquares[DROPPABLE_PIECES[i]]
						.setPiece(GameConstants.EMPTY);
			} else {
				board.pieceJailSquares[DROPPABLE_PIECES[i]]
						.setPiece(DROPPABLE_PIECES[i]);
			}

			board.pieceJailSquares[DROPPABLE_PIECES[i]]
					.setText(pieceCountToString(count));
			board.pieceJailSquares[DROPPABLE_PIECES[i]].redraw();
		}
	}

	protected void adjustPremoveLabel() {
		board.currentPremovesLabel.setText("Premoves: EMPTY");
	}

	protected void adjustToGameChangeNotInvolvingMove() {
		// Adjust the clocks.
		stopClocks();
		adjustClockLabelsAndUpdaters();
		adjustClockColors();
		startClocks();

		adjustToMoveIndicatorLabel();

		if (board.getGame().getResult() != 0) {
			onPlayGameEndSound();
		}

	}

	public void adjustToGameInitial() {
		LOG.info("adjustToGame " + getGame().getId() + " ...");
		long startTime = System.currentTimeMillis();
		initClockUpdaters();
		onPlayGameStartSound();
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

		LOG.info("adjustToGame in " + getGame().getId() + "  "
				+ (System.currentTimeMillis() - startTime));
	}

	protected void adjustToGameMove() {
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

		if (board.getGame().getResult() == 0) {
			onPlayMoveSound();
		}

		board.forceUpdate();

		LOG.info("adjustToGameChange " + getGame().getId() + "  n "
				+ (System.currentTimeMillis() - startTime));
	}

	public void adjustToMoveIndicatorLabel() {
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
		board.currentPremovesLabel.setText("");
	}

	public void dispose() {
		if (traverser != null) {
			traverser.dispose();
		}
		board = null;
		if (blackClockUpdater != null) {
			blackClockUpdater.stop();
			blackClockUpdater.dispose();
		}
		if (whiteClockUpdater != null) {
			whiteClockUpdater.stop();
			whiteClockUpdater.dispose();
		}

		LOG.debug("Disposed ChessBoardController");
	}

	public ChessBoard getBoard() {
		return board;
	}

	public long getCurrentBlackTime() {
		return currentBlackTime;
	}

	public long getCurrentWhiteTime() {
		return currentWhiteTime;
	}

	public Game getGame() {
		return board.game;
	}

	public MoveListTraverser getGameTraverser() {
		return traverser;
	}

	public int getPromoteType() {
		return promoteType;
	}

	public abstract String getTitle();

	public void init() {
		traverser = new MoveListTraverser(getGame());
		adjustCoolbarToInitial();
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

	public String lagToString(long lag) {

		if (lag < 0) {
			lag = 0;
		}

		int seconds = (int) (lag / 1000L);
		int tenths = (int) (lag % 1000) / 100;

		return "Lag " + seconds + "." + tenths + " sec";
	}

	/**
	 * Return true to continue with the close operation. False if the close
	 * operation should be haulted.
	 */
	public abstract boolean onClose();

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

	protected abstract void onPlayGameEndSound();

	protected abstract void onPlayGameStartSound();

	protected abstract void onPlayMoveSound();

	public void onSetupClear() {
	}

	public void onSetupDone() {
	}

	public void onSetupFen(String fen) {
	}

	public void onSetupStart() {
	}

	public String pieceCountToString(int count) {
		if (count < 2) {
			return "";
		} else {
			return "" + count;
		}
	}

	public void setAutoDraw(boolean autoDraw) {
		this.autoDraw = autoDraw;
	}

	public void setAutoPromote(int gamePieceType) {
		promoteType = gamePieceType;
	}

	public void setBoard(ChessBoard board) {
		this.board = board;
	}

	public void setCurrentBlackTime(long currentBlackTime) {
		this.currentBlackTime = currentBlackTime;
	}

	public void setCurrentWhiteTime(long currentWhiteTime) {
		this.currentWhiteTime = currentWhiteTime;
	}

	public void setPromoteType(int promoteType) {
		this.promoteType = promoteType;
	}

	protected void startClocks() {
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
		whiteClockUpdater.stop();
		blackClockUpdater.stop();
	}

	public void stopTimers() {
		whiteClockUpdater.stop();
		blackClockUpdater.stop();
	}

	public String timeToString(long timeMillis) {

		long timeLeft = timeMillis;

		if (timeLeft < 0) {
			timeLeft = 0;
		}

		if (timeLeft >= board.preferences
				.getLong(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
			int hour = (int) (timeLeft / (60000L * 60));
			timeLeft -= hour * 60 * 1000 * 60;
			int minute = (int) (timeLeft / 60000L);
			return RaptorStringUtils.defaultTimeString(hour, 2) + ":"
					+ RaptorStringUtils.defaultTimeString(minute, 2);

		} else if (timeLeft >= board.preferences
				.getLong(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
			int hour = (int) (timeLeft / (60000L * 60));
			timeLeft -= hour * 60 * 1000 * 60;
			int minute = (int) (timeLeft / 60000L);
			timeLeft -= minute * 60 * 1000;
			int seconds = (int) (timeLeft / 1000L);
			return RaptorStringUtils.defaultTimeString(minute, 2) + ":"
					+ RaptorStringUtils.defaultTimeString(seconds, 2);

		} else {
			int hour = (int) (timeLeft / (60000L * 60));
			timeLeft -= hour * 60 * 1000 * 60;
			int minute = (int) (timeLeft / 60000L);
			timeLeft -= minute * 60 * 1000;
			int seconds = (int) (timeLeft / 1000L);
			timeLeft -= seconds * 1000;
			int tenths = (int) (timeLeft / 100L);
			return RaptorStringUtils.defaultTimeString(minute, 2) + ":"
					+ RaptorStringUtils.defaultTimeString(seconds, 2) + "."
					+ RaptorStringUtils.defaultTimeString(tenths, 1);
		}
	}

	public abstract void userCancelledMove(int fromSquare, boolean isDnd);

	public abstract void userInitiatedMove(int square, boolean isDnd);

	public abstract void userMadeMove(int fromSquare, int toSquare);

	public abstract void userMiddleClicked(int square);

	public abstract void userRightClicked(int square);
}
