package raptor.chess.pgn;

import java.io.Serializable;
import java.util.ArrayList;

import raptor.chess.ClassicGame;

public class PgnParseOutput implements Serializable {
	static final long serialVersionUID = 1;
	ArrayList<PgnParserError> errors;

	ArrayList<ClassicGame> games;

	/**
	 * Don't use this its for gwt serizliation only.
	 */
	@SuppressWarnings("unused")
	private PgnParseOutput() {
	}

	public PgnParseOutput(ArrayList<ClassicGame> games,
			ArrayList<PgnParserError> errors) {
		super();
		this.games = games;
		this.errors = errors;
	}

	public ArrayList<PgnParserError> getErrors() {
		return errors;
	}

	public ArrayList<ClassicGame> getGames() {
		return games;
	}

}
