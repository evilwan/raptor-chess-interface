package raptor.game.pgn;

import java.util.ArrayList;

import raptor.game.Game;

public class ListMaintainingPgnParserListener extends LenientPgnParserListener {
	private ArrayList<Game> games = new ArrayList<Game>();

	private ArrayList<PgnParserError> errors = new ArrayList<PgnParserError>();

	public ListMaintainingPgnParserListener() {
		super();
	}

	@Override
	public void errorEncountered(PgnParserError error) {
		errors.add(error);
	}

	@Override
	public void gameParsed(Game game, int lineNumber) {
		games.add(game);
	}

	public ArrayList<PgnParserError> getErrors() {
		return errors;
	}

	public ArrayList<Game> getGames() {
		return games;
	}

};
