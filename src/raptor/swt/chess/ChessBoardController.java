package raptor.swt.chess;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.MoveListTraverser;
import raptor.game.util.GameUtils;

public abstract class ChessBoardController implements Constants, GameConstants {
	static final Log LOG = LogFactory.getLog(ChessBoardController.class);

	public static final String LAST_NAV = "last_nav";
	public static final String FORWARD_NAV = "forward_nav";
	public static final String NEXT_NAV = "next_nav";
	public static final String FIRST_NAV = "first_nav";
	public static final long TIMEZONE_OFFSET = -Calendar.getInstance().get(
			Calendar.ZONE_OFFSET);

	protected boolean autoDraw = false;
	protected ClockLabelUpdater blackClockUpdater;
	protected ChessBoard board;

	protected long currentBlackTime;
	protected long currentWhiteTime;
	protected MoveListTraverser traverser;
	protected int promoteType = QUEEN;

	protected ClockLabelUpdater whiteClockUpdater;
	
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
	}

	public ChessBoardController(ChessBoard board) {
		this.board = board;
		traverser = new MoveListTraverser(board.game);
	}

	protected void initClockUpdaters() {
		if (whiteClockUpdater == null) {
			whiteClockUpdater = new ClockLabelUpdater(board.whiteClockLabel,
					this.board);
			blackClockUpdater = new ClockLabelUpdater(board.blackClockLabel,
					this.board);
		}
	}

	protected void adjustBoardToGame(Game game) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.getSquare(i, j).setPiece(
						Utils.getSetPieceFromGamePiece(GameUtils
								.rankFileToSquare(i, j), game));
			}
		}
	}

	protected abstract void adjustCoolbarToInitial();

	protected void adjustFromNavigationChange() {
		adjustPieceJailFromGame(traverser.getAdjustedGame());
		adjustBoardToGame(traverser.getAdjustedGame());
		setNavButtonsEnablbed();
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

	protected void adjustPieceJailFromGame(Game game) {
		for (int i = 0; i < DROPPABLE_PIECES.length; i++) {
			int color = DROPPABLE_PIECE_COLOR[i];
			int count = INITIAL_DROPPABLE_PIECE_COUNTS[i]
					- board.game.getPieceCount(color, Utils
							.setPieceFromColoredPiece(DROPPABLE_PIECES[i]));

			if (count == 0) {
				board.pieceJailSquares[DROPPABLE_PIECES[i]]
						.setPiece(Constants.EMPTY);
			} else {
				board.pieceJailSquares[DROPPABLE_PIECES[i]]
						.setPiece(DROPPABLE_PIECES[i]);
			}

			board.pieceJailSquares[DROPPABLE_PIECES[i]]
					.setText(pieceCountToString(count));
			board.pieceJailSquares[DROPPABLE_PIECES[i]].redraw();
		}
	}

	protected void stopClocks() {
		whiteClockUpdater.stop();
		blackClockUpdater.stop();
	}

	protected boolean isGameActive() {
		return isGameInState(Game.ACTIVE_STATE);
	}

	protected void adjustClocks() {
		board.whiteClockLabel.setText(timeToString(board.game
				.getWhiteRemainingTimeMillis()));
		board.blackClockLabel.setText(timeToString(board.game
				.getBlackRemainingTimeMillis()));

		whiteClockUpdater.setRemainingTimeMillis(board.game
				.getWhiteRemainingTimeMillis());
		blackClockUpdater.setRemainingTimeMillis(board.game
				.getBlackRemainingTimeMillis());
	}

	protected boolean isGameInState(int state) {
		return (board.game.getState() & state) != 0;
	}

	protected void startClocks() {
		if (isGameActive() && !isGameInState(Game.UNTIMED_STATE)
				&& isGameInState(Game.IS_CLOCK_TICKING_STATE)) {
			initClockUpdaters();
			if (board.game.getColorToMove() == WHITE) {
				whiteClockUpdater.start();
			} else {
				blackClockUpdater.start();
			}
		}
	}

	protected void setNavButtonsEnablbed() {
		if (isMoveListTraversable()) {
			board.setButtonEnabled(traverser.hasFirst(), ChessBoard.FIRST_NAV);
			board.setButtonEnabled(traverser.hasLast(), ChessBoard.LAST_NAV);
			board.setButtonEnabled(traverser.hasNext(), ChessBoard.NEXT_NAV);
			board.setButtonEnabled(traverser.hasBack(), ChessBoard.BACK_NAV);
		} else {
			board.setButtonEnabled(false, ChessBoard.FIRST_NAV);
			board.setButtonEnabled(false, ChessBoard.LAST_NAV);
			board.setButtonEnabled(false, ChessBoard.NEXT_NAV);
			board.setButtonEnabled(false, ChessBoard.BACK_NAV);
		}
	}

	protected void adjustLagLabels() {
		board.whiteLagLabel
				.setText(lagToString(board.game.getWhiteLagMillis()));
		board.blackLagLabel.setText(lagToString(board.game
				.getBlackRemainingTimeMillis()));
	}

	protected void adjustPremoveLabel() {
		board.currentPremovesLabel.setText("Premoves: EMPTY");
	}

	protected void adjustStatusLabel() {
		if (isGameActive()) {
			System.out.println("In adjust status label game is active.");
			if (board.getGame().getHalfMoveCount() > 0) {
				Move move = board.game.getMoves().get(
						board.getGame().getHalfMoveCount() - 1);
				String moveDescription = move.getSan();

				if (StringUtils.isBlank(moveDescription)) {
					moveDescription = move.getLan();
				}
				System.out.println("Set status label.");
				board.statusLabel.setText("Last move: "
						+ Utils.halfMoveIndexToDescription(board.game
								.getHalfMoveCount(), GameUtils
								.getOppositeColor(board.game.getColorToMove()))
						+ moveDescription);
			} else {
				board.statusLabel.setText("");
			}
		} else {
			System.out.println("In adjust status label game is not active.");
			board.statusLabel
					.setText(StringUtils
							.defaultString(board.game.getResultDescription(),
									"Game is not active and no result string is set. This is a bug"));
		}
	}

	protected void adjustOpeningDescriptionLabel() {
		board.openingDescriptionLabel
				.setText("This will show opening description in the future.");
	}

	protected void adjustToGameChange() {
		LOG.info("adjustToGameChange " + board.game.getId() + " ...");
		long startTime = System.currentTimeMillis();

		adjustClocks();
		startClocks();
		traverser.adjustHalfMoveIndex();
		setNavButtonsEnablbed();
		adjustLagLabels();
		adjustPremoveLabel();
		adjustStatusLabel();
		adjustOpeningDescriptionLabel();
		adjustPieceJailFromGame(board.game);
		adjustBoardToGame(board.game);
		adjustGameStatusLabel();

		board.forceUpdate();

		LOG.info("adjustToGameChange " + board.game.getId() + "  n "
				+ (System.currentTimeMillis() - startTime));
	}

	protected void adjustNameRatingLabels() {
		board.blackNameRatingLabel.setText(board.game.getBlackName() + " ("
				+ board.game.getBlackRating() + ")");
		board.whiteNameRatingLabel.setText(board.game.getWhiteName() + " ("
				+ board.game.getWhiteRating() + ")");
	}

	public void adjustToGameInitial() {
		LOG.info("adjustToGame " + board.game.getId() + " ...");
		long startTime = System.currentTimeMillis();
		initClockUpdaters();
		adjustCoolbarToInitial();
		adjustNameRatingLabels();
		adjustToGameChange();
		adjustGameDescriptionLabel();

		LOG.info("adjustToGame in " + board.game.getId() + "  "
				+ (System.currentTimeMillis() - startTime));
	}

	public void onNavBack() {
		if (traverser.hasBack()) {
			traverser.back();
			adjustFromNavigationChange();
		} else {
			LOG.error("Traverser did not have previous so ignoring action");
		}
	}

	public abstract boolean canUserInitiateMoveFrom(int squareId);

	public void clearPremoves() {
		board.currentPremovesLabel.setText("");
	}

	protected abstract void decorateCoolbar();

	

	public void onNavFirst() {
		if (traverser.hasFirst()) {
			traverser.first();
			adjustFromNavigationChange();
		} else {
			LOG.error("Traverser did not have first so ignoring action");
		}

	}

	public void onFlip() {
		LOG.debug("onFlip");
		board.setWhiteOnTop(!board.isWhiteOnTop());
		board.setWhitePieceJailOnTop(!board.isWhitePieceJailOnTop());
		board.forceUpdate();
		LOG.debug("isWhiteOnTop = " + board.isWhiteOnTop);
	}

	public void onNavForward() {
		if (traverser.hasNext()) {
			traverser.next();
			adjustFromNavigationChange();
		}
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

	public abstract boolean isAbortable();

	public abstract boolean isAdjournable();

	public abstract boolean isAutoDrawable();

	public abstract boolean isDrawable();

	public abstract boolean isExaminable();

	public abstract boolean isMoveListTraversable();

	public abstract boolean isPausable();

	public abstract boolean isRematchable();

	public abstract boolean isResignable();

	public abstract boolean isRevertable();

	public abstract boolean isCommitable();

	public boolean isSureDraw() {
		// TO DO.
		return false;
	}

	public String lagToString(long lag) {

		if (lag < 0) {
			lag = 0;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(board.preferences
				.getString(BOARD_CLOCK_LAG_FORMAT));
		return dateFormat.format(new Date(lag + TIMEZONE_OFFSET));
	}

	public void onNavCommit() {
		// TO DO
	}

	public void onNavRevert() {
		// TO DO
	}

	public void onNavLast() {
		if (traverser.hasLast()) {
			traverser.last();
			adjustFromNavigationChange();
		}
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

	public void setCurrentBlackTime(long currentBlackTime) {
		this.currentBlackTime = currentBlackTime;
	}

	public void setCurrentWhiteTime(long currentWhiteTime) {
		this.currentWhiteTime = currentWhiteTime;
	}

	public void setPromoteType(int promoteType) {
		this.promoteType = promoteType;
	}

	public void stopTimers() {
		whiteClockUpdater.stop();
		blackClockUpdater.stop();
	}

	public void adjustGameDescriptionLabel() {
		board.gameDescriptionLabel.setText(board.game.getGameDescription());
		System.err.println("Set game description to "
				+ board.getGameDescriptionLabel().getText());
	}

	public void adjustGameStatusLabel() {
		if ((board.game.getState() & Game.ACTIVE_STATE) != 0) {
			if (board.game.getMoves().getSize() > 0) {
				Move lastMove = board.game.getMoves().get(
						board.game.getMoves().getSize() - 1);
				int moveNumber = (board.getGame().getHalfMoveCount() / 2) + 1;

				board.getStatusLabel().setText(
						"Last Move: " + moveNumber + ") "
								+ (lastMove.isWhitesMove() ? "" : "...")
								+ lastMove.toString());

			} else {
				System.err.println("no moves");
				board.getStatusLabel().setText("");
			}
		} else {
			System.err.println("inactive");
			board.getStatusLabel().setText(board.game.getResultDescription());
		}
		System.err.println("Set game status to "
				+ board.getStatusLabel().getText());
	}

	public String timeToString(long timeRemaining) {

		long timeLeft = timeRemaining;

		if (timeLeft < 0) {
			timeLeft = 0;
		}

		if (timeLeft >= board.preferences
				.getLong(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					board.preferences.getString(BOARD_CLOCK_FORMAT));
			return dateFormat.format(new Date(timeLeft + TIMEZONE_OFFSET));

		} else if (timeLeft >= board.preferences
				.getLong(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					board.preferences.getString(BOARD_CLOCK_SECONDS_FORMAT));
			return dateFormat.format(new Date(timeLeft + TIMEZONE_OFFSET));

		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					board.preferences.getString(BOARD_CLOCK_MILLIS_FORMAT));
			return dateFormat.format(new Date(timeLeft + TIMEZONE_OFFSET));
		}
	}

	public abstract void userMadeMove(int fromSquare, int toSquare);

	public abstract void userCancelledMove(int fromSquare, boolean isDnd);

	public abstract void userInitiatedMove(int square, boolean isDnd);

	public abstract void userMiddleClicked(int square);

	public abstract void userRightClicked(int square);
}
