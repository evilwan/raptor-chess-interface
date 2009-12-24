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

import raptor.Raptor;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.SoundService;
import raptor.swt.RaptorLabel;

public class ClockLabelUpdater implements Runnable, PreferenceKeys {
	ChessBoardController controller;
	ChessBoard board;
	boolean isWhite;
	RaptorLabel clockLabel;
	boolean isRunning;
	long lastSystemTime = 0;
	long remainingTimeMillis;
	int lastCountdownPlayed = -1;
	boolean isSpeakingCountdown;

	public ClockLabelUpdater(boolean isWhite, ChessBoardController controller,
			boolean isSpeakingCountdown) {
		this.isWhite = isWhite;
		board = controller.getBoard();
		clockLabel = isWhite ? board.getWhiteClockLabel() : board
				.getBlackClockLabel();
		this.controller = controller;
		this.isSpeakingCountdown = isSpeakingCountdown;
	}

	public long calculateNextUpdate() {
		// The previous approach of trying to update at less frequent intervals
		// depending on how the clock is setup didnt work so well.

		// For now make a check every 100 milliseconds if showing tenths then
		// every 50 ms.
		long result = 0L;
		if (remainingTimeMillis < 0) {
			// Just update every 100L to get the flashing behavior.
			result = 100L;
		} else {
			if (remainingTimeMillis >= getPreferences().getLong(
					BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
				result = 100L;
			} else if (remainingTimeMillis >= getPreferences().getLong(
					BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
				result = 100L;
			} else {
				result = remainingTimeMillis % 50L;
				if (result == 0L) {
					result = 50L;
				}
			}
		}
		return result;
	}

	public void dispose() {
		board = null;
		clockLabel = null;
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

			if (remainingTimeMillis < 10000 && isSpeakingCountdown) {
				playCountdownSound(remainingTimeMillis);
			}

			controller.getGame().setHeader(
					isWhite ? PgnHeader.WhiteRemainingMillis
							: PgnHeader.BlackRemainingMillis,
					"" + remainingTimeMillis);

			clockLabel.setText(GameUtils
					.timeToString(remainingTimeMillis, true));

			controller.adjustTimeUpLabel();

			// Continue running even if time has expired. This produces the
			// flashing behavior.
			long nextUpdate = calculateNextUpdate();
			if (isRunning) {
				Raptor.getInstance().getDisplay().timerExec((int) nextUpdate,
						this);
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
			Raptor.getInstance().getDisplay().timerExec((int) nextUpdate, this);
		}
	}

	public void stop() {
		isRunning = false;
		Raptor.getInstance().getDisplay().timerExec(-1, this);
	}

	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	protected void playCountdownSound(long currentTime) {
		if (currentTime >= 10000 && currentTime < 11000
				&& lastCountdownPlayed != 10) {
			lastCountdownPlayed = 10;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 9000 && currentTime < 10000
				&& lastCountdownPlayed != 9) {
			lastCountdownPlayed = 9;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 8000 && currentTime < 9000
				&& lastCountdownPlayed != 8) {
			lastCountdownPlayed = 8;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 7000 && currentTime < 8000
				&& lastCountdownPlayed != 7) {
			lastCountdownPlayed = 7;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 6000 && currentTime < 7000
				&& lastCountdownPlayed != 6) {
			lastCountdownPlayed = 6;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 5000 && currentTime < 6000
				&& lastCountdownPlayed != 5) {
			lastCountdownPlayed = 5;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 4000 && currentTime < 5000
				&& lastCountdownPlayed != 4) {
			lastCountdownPlayed = 4;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 3000 && currentTime < 4000
				&& lastCountdownPlayed != 3) {
			lastCountdownPlayed = 3;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 2000 && currentTime < 3000
				&& lastCountdownPlayed != 2) {
			lastCountdownPlayed = 2;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 1000 && currentTime < 2000
				&& lastCountdownPlayed != 1) {
			lastCountdownPlayed = 1;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		} else if (currentTime >= 0 && currentTime < 1000
				&& lastCountdownPlayed != 0) {
			lastCountdownPlayed = 0;
			SoundService.getInstance().playSound(
					"countdown" + lastCountdownPlayed);
		}
	}
}