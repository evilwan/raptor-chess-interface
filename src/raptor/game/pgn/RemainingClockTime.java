package raptor.game.pgn;

import raptor.util.RaptorStringUtils;

public class RemainingClockTime implements MoveAnnotation {
	static final long serialVersionUID = 1;
	public String text;

	public RemainingClockTime(String text) {
		setText(text);
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
