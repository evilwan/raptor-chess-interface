package raptor.chess.pgn;

import java.io.Serializable;

import raptor.util.RaptorStringUtils;

public class PgnParserError implements Serializable {
	static final long serialVersionUID = 1;

	public static enum Action {
		IGNORING, IGNORING_CURRENT_GAME, IGNORING_CURRENT_SUBLINE, IGNORIONG_HEADER, NONE,
	}

	/**
	 * Provided for internationalization support. To add a new cause add it to
	 * this enum and update the resource bundles which use it.
	 */
	public static enum Type {
		DANGLING_ANNOTATION, DANGLING_NAG,
		/**
		 * String[0] = annotation (This can happen in chessbase and its a real
		 * pain to support).
		 */
		DANGLING_SUBLINE, ILLEGAL_MOVE_ENCOUNTERED, INVALID_SUBLINE_STATE, UNABLE_TO_PARSE_INITIAL_FEN,
		/**
		 * String[0] = annotation text.
		 */
		UNEXPECTED_GAME_END, UNEXPECTED_GAME_START, UNEXPECTED_HEADER,
		/**
		 * String[0] = text.
		 */
		UNEXPECTED_MOVE_ANNOTATION, UNEXPECTED_MOVE_WORD, /**
		 * String[0] =word.
		 */
		UNEXPECTED_SUBLINE_END, UNEXPECTED_SUBLINE_START, /**
		 * String[0] = move
		 **/
		UNKNOWN_TEXT_ENCOUNTERED, /**
		 * String[0] =nag
		 */
		UNKNWON_ERROR,
		/**
		 * String[0] = header name. String[1] = header value.
		 **/
		UNSUPPORTED_PGN_HEADER
	}

	private Action action;

	private String[] args;

	private int lineNumber;

	private Type type;

	public PgnParserError(Type type, Action action, int lineNumber) {
		this.type = type;
		this.action = action;
		this.lineNumber = lineNumber;
	}

	public PgnParserError(Type type, Action action, int lineNumber,
			String... args) {
		this(type, action, lineNumber);
		this.args = args;
	}

	public Action getAction() {
		return action;
	}

	public String[] getArgs() {
		return args;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.name() + " " + action.name() + " "
				+ RaptorStringUtils.toDelimitedString(args, "'") + " "
				+ getLineNumber();
	}
}
