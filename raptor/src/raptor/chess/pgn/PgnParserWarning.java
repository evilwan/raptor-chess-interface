package raptor.chess.pgn;

import java.io.Serializable;

import raptor.util.RaptorStringUtils;

public class PgnParserWarning implements Serializable {
	static final long serialVersionUID = 1;

	/**
	 * Provided for internationalization support. To add a new cause add it to
	 * this enum and update the resource bundles which use it.
	 */
	public static enum Type {
	}

	private String[] args;

	private int rowNumber;

	private Type type;

	public PgnParserWarning(Type type, int rowNumber) {
		args = new String[0];
		this.rowNumber = rowNumber;
	}

	public PgnParserWarning(Type type, String[] args, int rowNumber) {
		this.type = type;
		this.args = args;
		this.rowNumber = rowNumber;
	}

	public String[] getArgs() {
		return args;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.name() + " "
				+ RaptorStringUtils.toDelimitedString(args, "'") + " "
				+ getRowNumber() + " ";
	}
}
