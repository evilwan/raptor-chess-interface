/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.chess.pgn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.GameCursor;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.pref.PreferenceKeys;
import raptor.util.RaptorStringUtils;

/**
 * A class containing PGN utils.
 */
public class PgnUtils {
	private static Date DEFAULT_PGN_DATE_HEADER = null;

	public static final DateFormat PGN_HEADER_DATE_FORMAT = new SimpleDateFormat(
			"yyyy.MM.dd");

	public static String DEFAULT_PGN_HEADER = "?";
	public static String DEFAULT_PGN_RESULT_HEADER = "*";
	private static final Log LOG = LogFactory.getLog(PgnUtils.class);

	public static final String PGN_MIME_TYPE = "application/x-chess-pgn";
	private static final Object PGN_APPEND_SYNCH = new Object();

	/**
	 * Prepends the game to the users game pgn file.
	 */
	public static void appendGameToFile(Game game) {
		if (Variant.isBughouse(game.getVariant())) {
			return;
		}
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_IS_LOGGING_GAMES)
				&& game.getMoveList().getSize() > 0) {

			// synchronized on PGN_APPEND_SYNCH so just one thread at a time
			// writes to the file.
			synchronized (PGN_APPEND_SYNCH) {

				if (game instanceof GameCursor) {
					game = ((GameCursor) game).getMasterGame();
				}

				String whiteRating = game.getHeader(PgnHeader.WhiteElo);
				String blackRating = game.getHeader(PgnHeader.BlackElo);

				whiteRating = StringUtils.remove(whiteRating, 'E');
				whiteRating = StringUtils.remove(whiteRating, 'P');
				blackRating = StringUtils.remove(blackRating, 'E');
				blackRating = StringUtils.remove(blackRating, 'P');

				if (!NumberUtils.isDigits(whiteRating)) {
					game.removeHeader(PgnHeader.WhiteElo);
				}
				if (!NumberUtils.isDigits(blackRating)) {
					game.removeHeader(PgnHeader.BlackElo);
				}

				String pgn = game.toPgn();
				File file = new File(Raptor.GAMES_PGN_FILE);
				FileWriter fileWriter = null;
				try {
					fileWriter = new FileWriter(file, true);
					fileWriter.append(pgn + "/n/n");
					fileWriter.flush();
				} catch (IOException ioe) {
					LOG.error("Error saving game", ioe);
				} finally {
					try {
						if (fileWriter != null) {
							fileWriter.close();
						}
					} catch (IOException ioe) {
					}
				}
			}
		}
	}

	/**
	 * Returns the approximate number of games in the specified file.
	 */
	public static int getApproximateGameCount(String file) {
		int result = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				if (StringUtils.startsWithIgnoreCase(currentLine, "[Event")) {
					result++;
				}
			}
		} catch (IOException ioe) {
			LOG.error("Error reading game count" + file, ioe);
		} finally {
			try {
				reader.close();
			} catch (IOException ioe) {
			}
		}
		return result;
	}

	/**
	 * Returns the PgnHeader line for the specified header name and value.
	 */
	public static void getHeaderLine(StringBuilder builder,
			String pgnHeaderName, String pgnHeaderValue) {
		builder
				.append("["
						+ pgnHeaderName
						+ " \""
						+ (pgnHeaderValue == null
								|| pgnHeaderValue.length() == 0 ? PgnHeader.UNKNOWN_VALUE
								: pgnHeaderValue) + "\"]");
	}

	/**
	 * Returns the string to use for the move in a Pgn file. This includes the
	 * move number and all annotations.
	 */
	public static boolean getMove(StringBuilder builder, Move move,
			boolean forceMoveNumber) {
		boolean result = false;

		if (forceMoveNumber || move.isWhitesMove()) {
			int moveNumber = move.getFullMoveCount();
			builder.append(moveNumber + (move.isWhitesMove() ? ". " : "... "));
		}
		builder.append(move.toString());

		// First get all of the sublines.
		for (SublineNode subline : move.getSublines()) {
			result = true;
			builder.append(" (");
			getSubline(builder, subline);
			builder.append(")");
		}

		for (Comment comment : move.getComments()) {
			builder.append(" {" + comment.getText() + "}");
		}

		for (Nag nag : move.getNags()) {
			builder.append(" " + nag.getNagString());
		}

		for (TimeTakenForMove timeTaken : move.getTimeTakenForMove()) {
			builder.append(" {" + timeTaken.getText() + "}");
			break;
		}

		return result;
	}

	/**
	 * Cuts off all information except for the position.
	 */
	public static String getPositionFromFen(String fen) {
		int spaceIndex = fen.indexOf(' ');
		return fen.substring(0, spaceIndex);
	}

	/**
	 * Returns the pgn representation of the specified subline including all
	 * annotations.
	 */
	public static void getSubline(StringBuilder builder, SublineNode subline) {
		boolean forceMoveNumber = getMove(builder, subline.getMove(), true);
		SublineNode current = subline.getReply();
		while (current != null) {
			builder.append(" ");
			forceMoveNumber = getMove(builder, current.getMove(),
					forceMoveNumber);
			current = current.getReply();
		}
	}

	/**
	 * [Date "2009.10.07"]
	 */
	public static String longToPgnDate(long time) {
		return PGN_HEADER_DATE_FORMAT.format(new Date(time));
	}

	/**
	 * Converts a PgnHeader.Date header into a date object.
	 */
	public static Date pgnDateHeaderToDate(String pgnDateValue) {
		Date result = null;
		initPgnDateHeader();

		if (pgnDateValue.length() != 10) {
			LOG.error("Invalid pgn header date format: " + pgnDateValue
					+ " setting to default.");
			result = DEFAULT_PGN_DATE_HEADER;
		} else if (pgnDateValue.startsWith("????")) {
			result = DEFAULT_PGN_DATE_HEADER;
		} else {
			String year = pgnDateValue.substring(0, 4);
			String month = pgnDateValue.substring(5, 7);
			String day = pgnDateValue.substring(8, 10);

			if (month.equals("??")) {
				month = "01";
			}
			if (day.equals("??")) {
				day = "01";
			}

			try {
				result = PGN_HEADER_DATE_FORMAT.parse(year + "." + month + "."
						+ day);
			} catch (ParseException pe) {
				LOG.error("Invalid pgn header date format: " + pgnDateValue
						+ " " + year + "." + month + "." + day
						+ " setting to default.");
				result = DEFAULT_PGN_DATE_HEADER;
			}
		}
		return result;
	}

	/**
	 * [TimeControl "60+0"]
	 */
	public static String timeIncMillisToTimeControl(long startTimeMillis,
			long startIncMillis) {
		String minutes = "" + startTimeMillis / 1000;
		String inc = "" + startIncMillis / 1000;

		return minutes + "+" + inc;
	}

	/**
	 * [WhiteClock "0:01:00.000"] [BlackClock "0:01:00.000"]
	 */
	public static String timeToClock(long timeMillis) {
		long timeLeft = timeMillis;

		if (timeLeft < 0) {
			timeLeft = 0;
		}

		int hour = (int) (timeLeft / (60000L * 60));
		timeLeft -= hour * 60 * 1000 * 60;
		int minute = (int) (timeLeft / 60000L);
		timeLeft -= minute * 60 * 1000;
		int seconds = (int) (timeLeft / 1000L);
		timeLeft -= seconds * 1000;
		int millis = (int) timeLeft;

		return RaptorStringUtils.defaultTimeString(hour, 2) + ":"
				+ RaptorStringUtils.defaultTimeString(minute, 2) + ":"
				+ RaptorStringUtils.defaultTimeString(seconds, 2) + "."
				+ RaptorStringUtils.defaultTimeString(millis, 1);
	}

	/**
	 * Chess base EMT format. 1. e4 {[%emt 0.0]} e6 {[%emt 0.0]} 2. Nc3 {[%emt
	 * 1.398]} Nf6 {[%emt 0.1]}
	 */
	public static String timeToEMTFormat(long elapsedTimeMillis) {
		double elapsedTimeInSeconds = elapsedTimeMillis / 1000.0;
		BigDecimal bigDecimal = new BigDecimal(elapsedTimeInSeconds);
		bigDecimal = bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP);
		return "[%emt " + bigDecimal.toString() + "]";
	}

	private static void initPgnDateHeader() {
		if (DEFAULT_PGN_DATE_HEADER == null) {
			try {
				DEFAULT_PGN_DATE_HEADER = PGN_HEADER_DATE_FORMAT
						.parse("1500.01.01");
			} catch (ParseException pe) {
				throw new RuntimeException(pe);
			}
		}
	}

}
