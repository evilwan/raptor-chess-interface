package raptor.game.pgn;

import raptor.util.RaptorStringUtils;

public class Comment implements MoveAnnotation {
	static final long serialVersionUID = 1;
	private String text;

	public Comment(String text) {
		setText(text);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		this.text = RaptorStringUtils.replaceAll(this.text, "\r\n", " ");
		this.text = RaptorStringUtils.replaceAll(this.text, "\n\r", " ");
		this.text = RaptorStringUtils.replaceAll(this.text, "\n", " ");
		this.text = RaptorStringUtils.removeAll(this.text, '`');
		this.text = RaptorStringUtils.removeAll(this.text, '|');
		this.text = this.text.trim();
	}

	@Override
	public String toString() {
		return text;
	}
}
