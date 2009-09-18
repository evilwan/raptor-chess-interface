package raptor.swt.chess;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.GameTraverser;
import raptor.game.Move;
import raptor.game.util.GameUtils;

public abstract class ChessBoardController implements Constants, GameConstants {

	public static final String LAST_NAV = "last_nav";
	public static final String FORWARD_NAV = "forward_nav";
	public static final String NEXT_NAV = "next_nav";
	public static final String FIRST_NAV = "first_nav";

	class ClockLabelUpdater implements Runnable {
		Label clockLabel;
		long elapsedTimeMillis;
		long initialTimeMillis;
		boolean isRunning;
		long valueToSubtractNextRun = 0;

		public ClockLabelUpdater(Label clockLabel) {
			this.clockLabel = clockLabel;
		}

		public long getElapsedTimeMillis() {
			return elapsedTimeMillis;
		}

		public long getInitialTimeMillis() {
			return initialTimeMillis;
		}

		public void run() {
			if (isRunning) {
				valueToSubtractNextRun -= valueToSubtractNextRun;
				clockLabel.setText(timeToString(initialTimeMillis,
						elapsedTimeMillis));

				if (elapsedTimeMillis > 0) {
					long nextUpdate = 1000L;

					if (elapsedTimeMillis >= board.preferences
							.getLong(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
						nextUpdate = 60000L;
					} else if (elapsedTimeMillis >= board.preferences
							.getLong(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
						nextUpdate = 1000L;
					} else {
						nextUpdate = 100L;
					}
					if (isRunning) {
						valueToSubtractNextRun = nextUpdate;
						Display.getCurrent().timerExec((int) nextUpdate, this);
					}
				}
			}
		}

		public void setElapsedTimeMillis(long elapsedTimeMillis) {
			this.elapsedTimeMillis = elapsedTimeMillis;
		}

		public void setInitialTimeMillis(long initialTimeMillis) {
			this.initialTimeMillis = initialTimeMillis;
		}

		public void start() {
			isRunning = true;
			if (elapsedTimeMillis > 0) {
				long nextUpdate = 1000L;

				if (elapsedTimeMillis >= board.preferences
						.getLong(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
					nextUpdate = elapsedTimeMillis % 60000L;
				} else if (elapsedTimeMillis >= board.preferences
						.getLong(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
					nextUpdate = elapsedTimeMillis % 1000L;
				} else {
					nextUpdate = elapsedTimeMillis % 100L;
				}
				valueToSubtractNextRun = nextUpdate;
				Display.getCurrent().timerExec((int) nextUpdate, this);
			}
		}

		public void stop() {
			isRunning = false;
			Display.getCurrent().timerExec(-1, this);
		}
	}

	protected boolean autoDraw = false;
	protected ClockLabelUpdater blackClockUpdater;
	protected ChessBoard board;

	protected long currentBlackTime;
	protected long currentWhiteTime;
	protected GameTraverser gameTraverser;

	protected boolean isActive = false;

	static final Log LOG = LogFactory.getLog(ChessBoardController.class);
	protected int promoteType = GameConstants.QUEEN;

	protected ClockLabelUpdater whiteClockUpdater;

	public ChessBoardController(ChessBoard board) {
		this.board = board;
		whiteClockUpdater = new ClockLabelUpdater(board.whiteClockLabel);
		blackClockUpdater = new ClockLabelUpdater(board.blackClockLabel);
		gameTraverser = new GameTraverser(board.game);
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
		adjustPieceJailFromGame(gameTraverser.getCurrnentGame());
		adjustBoardToGame(gameTraverser.getCurrnentGame());
		redraw();
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

	protected void adjustToGameChange() {
		LOG.info("adjustToGameChange " + board.game.getId() + " ...");
		long startTime = System.currentTimeMillis();

		whiteClockUpdater.stop();
		blackClockUpdater.stop();

		isActive = (board.game.getState() & Game.ACTIVE_STATE) > 0;

		board.whiteClockLabel.setText(timeToString(board.game
				.getInitialWhiteTimeMillis(), board.game.getWhiteTimeMillis()));
		board.blackClockLabel.setText(timeToString(board.game
				.getInitialBlackTimeMillis(), board.game.getBlackTimeMillis()));

		whiteClockUpdater.setElapsedTimeMillis(board.game.getWhiteTimeMillis());
		blackClockUpdater.setElapsedTimeMillis(board.game.getBlackTimeMillis());

		if ((board.game.getState() & Game.ACTIVE_STATE) != 0
				&& (board.game.getState() & Game.UNTIMED_STATE) != 0
				&& (board.game.getState() & Game.IS_CLOCK_TICKING_STATE) != 0) {
			whiteClockUpdater.start();
			blackClockUpdater.start();
		}

		if (isMoveListTraversable()) {
			gameTraverser.gotoHalfMove(board.game.getHalfMoveCount());
			board.setToolItemEnabled(gameTraverser.hasFirst(),
					ChessBoard.FIRST_NAV);
			board.setToolItemEnabled(gameTraverser.hasLast(),
					ChessBoard.LAST_NAV);
			board.setToolItemEnabled(gameTraverser.hasNext(),
					ChessBoard.FORWARD_NAV);
			board.setToolItemEnabled(gameTraverser.hasPrevious(),
					ChessBoard.BACK_NAV);
		} else {
			board.setToolItemEnabled(false, ChessBoard.FIRST_NAV);
			board.setToolItemEnabled(false, ChessBoard.LAST_NAV);
			board.setToolItemEnabled(false, ChessBoard.FORWARD_NAV);
			board.setToolItemEnabled(false, ChessBoard.BACK_NAV);
		}

		board.whiteLagLabel
				.setText(lagToString(board.game.getWhiteLagMillis()));
		board.blackLagLabel
				.setText(lagToString(board.game.getBlackTimeMillis()));

		board.currentPremovesLabel.setText("Premoves: EMPTY");
		board.statusLabel
				.setText(board.game.getMoves().getSize() > 0 ? board.game
						.getMoves().get(board.game.getMoves().getSize() - 1)
						.getLan() : "");
		board.openingDescriptionLabel
				.setText("This will show opening description in the future.");
		board.statusLabel.setText(!isActive ? board.game.getResultDescription()
				: "");

		adjustPieceJailFromGame(board.game);
		adjustBoardToGame(board.game);
		adjustGameStatusLabel();

		board.layout();

		LOG.info("adjustToGameChange " + board.game.getId() + "  n "
				+ (System.currentTimeMillis() - startTime));
	}

	public void adjustToGameInitial() {
		LOG.info("adjustToGame " + board.game.getId() + " ...");
		long startTime = System.currentTimeMillis();

		adjustCoolbarToInitial();

		board.blackNameRatingLabel.setText(board.game.getBlackName() + " ("
				+ board.game.getBlackRating() + ")");
		board.whiteNameRatingLabel.setText(board.game.getWhiteName() + " ("
				+ board.game.getWhiteRating() + ")");

		whiteClockUpdater.setInitialTimeMillis(board.game
				.getInitialWhiteTimeMillis());
		blackClockUpdater.setInitialTimeMillis(board.game
				.getInitialBlackTimeMillis());

		adjustToGameChange();
		adjustGameDescriptionLabel();

		LOG.info("adjustToGame in " + board.game.getId() + "  "
				+ (System.currentTimeMillis() - startTime));
	}

	public void onNavBack() {
		if (gameTraverser.hasPrevious()) {
			gameTraverser.previous();
			adjustFromNavigationChange();
		}
	}

	public abstract boolean canUserInitiateMoveFrom(int squareId);

	public void clearPremoves() {
		board.currentPremovesLabel.setText("");
	}

	protected abstract void decorateCoolbar();

	public void dispose() {
		if (gameTraverser != null) {
			gameTraverser.dispose();
		}
		board = null;
	}

	public void onNavFirst() {
		if (gameTraverser.hasFirst()) {
			gameTraverser.first();
			adjustFromNavigationChange();
		}
	}

	public void onFlip() {
		LOG.debug("onFlip");
		board.setWhiteOnTop(!board.isWhiteOnTop());
		board.setWhitePieceJailOnTop(!board.isWhitePieceJailOnTop());
		board.getBoardPanel().layout();
		board.getBoardPanel().redraw();
		LOG.debug("isWhiteOnTop = " + board.isWhiteOnTop);
	}

	public void onNavForward() {
		if (gameTraverser.hasNext()) {
			gameTraverser.next();
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

	public GameTraverser getGameTraverser() {
		return gameTraverser;
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
		return dateFormat.format(new Date(lag));
	}

	public void onNavCommit() {

	}

	public void onNavRevert() {

	}

	public void onNavLast() {
		if (gameTraverser.hasLast()) {
			gameTraverser.last();
			adjustFromNavigationChange();
		}
	}

	public void layout() {
		board.layout();
	}

	public String pieceCountToString(int count) {
		if (count < 2) {
			return "";
		} else {
			return "" + count;
		}
	}

	public void redraw() {
		board.redraw();
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

	public String timeToString(long initialTimeMillis, long timeUsedMillis) {

		long timeLeft = initialTimeMillis - timeUsedMillis;

		if (timeLeft < 0) {
			timeLeft = 0;
		}

		if (timeLeft >= board.preferences
				.getLong(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					board.preferences.getString(BOARD_CLOCK_FORMAT));
			return dateFormat.format(new Date(timeLeft));

		} else if (timeLeft >= board.preferences
				.getLong(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					board.preferences.getString(BOARD_CLOCK_SECONDS_FORMAT));
			return dateFormat.format(new Date(timeLeft));

		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					board.preferences.getString(BOARD_CLOCK_MILLIS_FORMAT));
			return dateFormat.format(new Date(timeLeft));
		}
	}

	public abstract void userMadeMove(int fromSquare, int toSquare);

	public abstract void userCancelledMove(int fromSquare, boolean isDnd);

	public abstract void userInitiatedMove(int square, boolean isDnd);

	public abstract void userMiddleClicked(int square);

	public abstract void userRightClicked(int square);
}
