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

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Display;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;

class ClockLabelUpdater implements Runnable, PreferenceKeys {
	CLabel clockLabel;
	long remainingTimeMillis;
	boolean isRunning;
	long lastSystemTime = 0;
	ChessBoard board;

	public ClockLabelUpdater(CLabel clockLabel, ChessBoard board) {
		this.clockLabel = clockLabel;
		this.board = board;
	}

	public long calculateNextUpdate() {

		// The timer is not always perfect.
		// The following code adjusts the ideal durations in half to make up for
		// the differences.
		long result = 0L;
		if (remainingTimeMillis >= getPreferences().getLong(
				BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
			result = remainingTimeMillis % 30000L;
			if (result == 0L) {
				result = 30000L;
			}
		} else if (remainingTimeMillis >= getPreferences().getLong(
				BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
			result = remainingTimeMillis % 500L;
			if (result == 0L) {
				result = 500L;
			}
		} else {
			result = remainingTimeMillis % 50L;
			if (result == 0L) {
				result = 50L;
			}
		}
		return result;
	}

	public void dispose() {
		board = null;
		clockLabel = null;
	}

	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	public long getRemainingTimeMillis() {
		return remainingTimeMillis;
	}

	public void run() {
		if (isRunning && board != null && clockLabel != null
				&& !clockLabel.isDisposed()) {
			long currentTime = System.currentTimeMillis();
			remainingTimeMillis -= currentTime - lastSystemTime;
			lastSystemTime = currentTime;

			clockLabel.setText(BoardUtils.timeToString(remainingTimeMillis));

			if (remainingTimeMillis > 0) {
				long nextUpdate = calculateNextUpdate();
				if (isRunning) {
					Display.getCurrent().timerExec((int) nextUpdate, this);
				}
			}
		}
	}

	public void setRemainingTimeMillis(long elapsedTimeMillis) {
		remainingTimeMillis = elapsedTimeMillis;
	}

	public void start() {
		isRunning = true;
		if (remainingTimeMillis > 0) {
			lastSystemTime = System.currentTimeMillis();
			long nextUpdate = calculateNextUpdate();
			Display.getCurrent().timerExec((int) nextUpdate, this);
		}
	}

	public void stop() {
		isRunning = false;
		Display.getCurrent().timerExec(-1, this);
	}
}