package raptor.chess.pgn;

import java.util.ArrayList;
import java.util.List;

import raptor.chess.Result;

public abstract class AbstractPgnParser implements PgnParser {

	public List<PgnParserListener> listeners = new ArrayList<PgnParserListener>(
			3);

	public void addPgnParserListener(PgnParserListener listener) {
		listeners.add(listener);
	}

	protected void fireAnnotation(String annotation) {
		for (PgnParserListener listener : listeners) {
			listener.onAnnotation(this, annotation);
		}
	}

	protected void fireGameEnd(Result result) {
		for (PgnParserListener listener : listeners) {
			listener.onGameEnd(this, result);
		}
	}

	protected void fireGameStart() {
		for (PgnParserListener listener : listeners) {
			listener.onGameStart(this);
		}
	}

	protected void fireHeader(String headerName, String headerValue) {
		for (PgnParserListener listener : listeners) {
			listener.onHeader(this, headerName, headerValue);
		}
	}

	protected void fireMoveNag(Nag nag) {
		for (PgnParserListener listener : listeners) {
			listener.onMoveNag(this, nag);
		}
	}

	protected void fireMoveNumber(int moveNumber) {
		for (PgnParserListener listener : listeners) {
			listener.onMoveNumber(this, moveNumber);
		}
	}

	protected void fireMoveWord(String moveWord) {
		for (PgnParserListener listener : listeners) {
			listener.onMoveWord(this, moveWord);
		}
	}

	protected void fireSublineEnd() {
		for (PgnParserListener listener : listeners) {
			listener.onMoveSublineEnd(this);
		}
	}

	protected void fireSublineStart() {
		for (PgnParserListener listener : listeners) {
			listener.onMoveSublineStart(this);
		}
	}

	protected void fireUnknown(String unknown) {
		for (PgnParserListener listener : listeners) {
			listener.onUnknown(this, unknown);
		}
	}

	public void removePgnParserListener(PgnParserListener listener) {
		listeners.remove(listener);
	}
}
