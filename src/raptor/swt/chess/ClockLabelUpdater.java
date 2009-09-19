package raptor.swt.chess;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import raptor.pref.PreferenceKeys;

class ClockLabelUpdater implements Runnable,PreferenceKeys {
	Label clockLabel;
	long remainingTimeMillis;
	boolean isRunning;
	long valueToSubtractNextRun = 0;
	ChessBoard board;

	public ClockLabelUpdater(Label clockLabel,ChessBoard board) {
		this.clockLabel = clockLabel;
		this.board = board;
	}
	
	public void dispose() {
		board = null;
		clockLabel = null;
	}

	public long getRemainingTimeMillis() {
		return remainingTimeMillis;
	}

	public void run() {
		if (isRunning && board != null && clockLabel != null) {
			valueToSubtractNextRun -= valueToSubtractNextRun;
			clockLabel.setText(board.getController().timeToString(remainingTimeMillis));

			if (remainingTimeMillis > 0) {
				long nextUpdate = 1000L;

				if (remainingTimeMillis >= board.preferences
						.getLong(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
					nextUpdate = 60000L;
				} else if (remainingTimeMillis >= board.preferences
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

	public void setRemainingTimeMillis(long elapsedTimeMillis) {
		this.remainingTimeMillis = elapsedTimeMillis;
	}

	public void start() {
		isRunning = true;
		if (remainingTimeMillis > 0) {
			long nextUpdate = 1000L;

			if (remainingTimeMillis >= board.preferences
					.getLong(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
				nextUpdate = remainingTimeMillis % 60000L;
			} else if (remainingTimeMillis >= board.preferences
					.getLong(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
				nextUpdate = remainingTimeMillis % 1000L;
			} else {
				nextUpdate = remainingTimeMillis % 100L;
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