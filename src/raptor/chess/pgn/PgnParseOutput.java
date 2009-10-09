package raptor.chess.pgn;

import java.io.Serializable;
import java.util.ArrayList;

import raptor.chess.Game;

public class PgnParseOutput implements Serializable {
	static final long serialVersionUID = 1;
	ArrayList<Game> games;

	ArrayList<PgnParserError> errors;

	/**
	 * Don't use this its for gwt serizliation only.
	 */
	@SuppressWarnings("unused")
	private PgnParseOutput() {
	}

	public PgnParseOutput(ArrayList<Game> games,
			ArrayList<PgnParserError> errors) {
		super();
		this.games = games;
		this.errors = errors;
	}

	public ArrayList<PgnParserError> getErrors() {
		return errors;
	}

	public ArrayList<Game> getGames() {
		return games;
	}

}
