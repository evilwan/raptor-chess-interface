package raptor.chess.pgn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chess.Move;

public class PgnUtils {
	private static final Log LOG = LogFactory.getLog(PgnUtils.class);

	public static String DEFAULT_PGN_HEADER = "?";
	public static String DEFAULT_PGN_RESULT_HEADER = "*";
	public static final DateFormat PGN_HEADER_DATE_FORMAT = new SimpleDateFormat(
			"yyyy.MM.dd");
	public static Date DEFAULT_PGN_DATE_HEADER = null;
	public static final String PGN_MIME_TYPE = "application/x-chess-pgn";

	static {
		try {
			DEFAULT_PGN_DATE_HEADER = PGN_HEADER_DATE_FORMAT
					.parse("1500.01.01");
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
	}

	public static void buildHeader(StringBuilder builder, String name,
			String value) {
		builder
				.append("["
						+ name
						+ " \""
						+ (value == null || value.length() == 0 ? PgnHeader.UNKNOWN_VALUE
								: value) + "\"]");
	}

	/**
	 * Returns true if a subline was built. This is useful to know if you should
	 * force the next line number or not.
	 */
	public static boolean buildMoveInfo(StringBuilder builder, Move moveInfo,
			boolean forceMoveNumber) {
		boolean result = false;

		if (forceMoveNumber || moveInfo.isWhitesMove()) {
			int moveNumber = moveInfo.getFullMoveCount();
			builder.append(moveNumber
					+ (moveInfo.isWhitesMove() ? ". " : "... "));
		}
		builder.append(moveInfo.toString());

		// First get all of the sublines.
		for (SublineNode subline : moveInfo.getSublines()) {
			result = true;
			builder.append(" (");
			buildSubline(builder, subline);
			builder.append(")");
		}

		for (Comment comment : moveInfo.getComments()) {
			builder.append(" {" + comment.getText() + "}");
		}

		for (Nag nag : moveInfo.getNags()) {
			builder.append(" " + nag.getNagString());
		}
		return result;
	}

	/**
	 * Builds the subline to the specified builder.
	 */
	public static void buildSubline(StringBuilder builder, SublineNode subline) {
		boolean forceMoveNumber = buildMoveInfo(builder, subline.getMove(),
				true);
		SublineNode current = subline.getReply();
		while (current != null) {
			builder.append(" ");
			forceMoveNumber = buildMoveInfo(builder, current.getMove(),
					forceMoveNumber);
			current = current.getReply();
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
	 * Cuts off all information except for the position.
	 */
	public static String getPositionFromFen(String fen) {
		int spaceIndex = fen.indexOf(' ');
		return fen.substring(0, spaceIndex);
	}

	public static Date pgnDateHeaderToDate(String pgnDateHeader) {
		Date result = null;

		if (pgnDateHeader.length() != 10) {
			LOG.error("Invalid pgn header date format: " + pgnDateHeader
					+ " setting to default.");
			result = DEFAULT_PGN_DATE_HEADER;
		} else if (pgnDateHeader.startsWith("????")) {
			result = DEFAULT_PGN_DATE_HEADER;
		} else {
			String year = pgnDateHeader.substring(0, 4);
			String month = pgnDateHeader.substring(5, 7);
			String day = pgnDateHeader.substring(8, 10);

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
				LOG.error("Invalid pgn header date format: " + pgnDateHeader
						+ " " + year + "." + month + "." + day
						+ " setting to default.");
				result = DEFAULT_PGN_DATE_HEADER;
			}
		}
		return result;
	}

	public static String timeToEMTFormat(long elapsedTimeMillis) {
		double elapsedTimeInSeconds = elapsedTimeMillis / 1000.0;
		BigDecimal bigDecimal = new BigDecimal(elapsedTimeInSeconds);
		bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP);
		return "[%emt " + bigDecimal.toString() + "]";
	}

}
