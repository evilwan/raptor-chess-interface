package raptor.chess.pgn;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;

/**
 * These annotations take the form [%emt 1.438]
 */
public class TimeTakenForMove implements MoveAnnotation {
	private static final long serialVersionUID = 3398826247420411970L;
	private static final Log LOG = LogFactory.getLog(TimeTakenForMove.class);
	public String text;

	public TimeTakenForMove(long time) {
		// [%emt 1.438]
		setText(PgnUtils.timeToEMTFormat(time));
	}

	public TimeTakenForMove(String text) {
		setText(text);
	}

	public long getMilliseconds() {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(getText(),
				"[%emt ]", true);
		if (tok.hasMoreTokens()) {
			String elapsedTimeInSeconds = tok.nextToken();
			try {
				BigDecimal decimal = new BigDecimal(elapsedTimeInSeconds);
				decimal = decimal.multiply(new BigDecimal(1000));
				return decimal.longValue();
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

	@Override
	public String toString() {
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
}
