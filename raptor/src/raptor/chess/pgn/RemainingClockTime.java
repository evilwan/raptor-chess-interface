package raptor.chess.pgn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;

/**
 * These annotations take the form [%emt 1.438]
 */
public class RemainingClockTime implements MoveAnnotation {
	private static final Log LOG = LogFactory.getLog(RemainingClockTime.class);
	static final long serialVersionUID = 1;
	public String text;

	public RemainingClockTime(long time) {
		// [%emt 1.438]
		setText(PgnUtils.timeToEMTFormat(time));
	}

	public RemainingClockTime(String text) {
		setText(text);
	}

	public long getElapsedTimeForMoveMillis() {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(getText(),
				"[Z%emt ]", true);
		if (tok.hasMoreTokens()) {
			String elapsedTimeInSeconds = tok.nextToken();
			try {
				double asDouble = Double.parseDouble(elapsedTimeInSeconds);
				return (long) asDouble * 1000;
			} catch (NumberFormatException nfe) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Invalid Remaining Clock Time detected: "
							+ getText());
				}
			}
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Invalid Remaining Clock Time detected: " + getText());
			}
		}
		return 0;
	}

	public String getText() {
		return text;
	}

	private void setText(String text) {
		this.text = text;
		this.text = RaptorStringUtils.removeAll(this.text, '\r');
		this.text = RaptorStringUtils.removeAll(this.text, '\n');
		this.text = RaptorStringUtils.removeAll(this.text, '`');
		this.text = RaptorStringUtils.removeAll(this.text, '|');
		this.text = text.trim();
	}

	@Override
	public String toString() {
		return text;
	}
}
