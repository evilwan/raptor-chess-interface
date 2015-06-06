package raptor.chess.pgn.chesspresso;

import java.util.ArrayList;

import chesspresso.pgn.PGNSyntaxError;
import raptor.chess.Game;
import raptor.chess.pgn.LenientPgnParserListener;
import raptor.chess.pgn.PgnParserError;

public class ChesspressoPgnListener extends LenientPgnParserListener {

	protected ArrayList<chesspresso.game.Game> games = new ArrayList<chesspresso.game.Game>();
	protected ArrayList<PGNSyntaxError> errors = new ArrayList<PGNSyntaxError>();
	
	
	public void error(PGNSyntaxError error) {
		errors.add(error);
	}
	
	@Override
	public void errorEncountered(PgnParserError error) {
	}

	@Override
	public void gameParsed(Game game, int lineNumber) {			
	}
	
	/**
	 * Analogous to the inherited method, but instead uses Chesspresso game object 
	 */
	public void gameParsed(chesspresso.game.Game game, final int lineNumber) {	
		if (game == null)
			return;
		
		games.add(game);
	}
	
	public ArrayList<chesspresso.game.Game> getGames() {
		return games;
	}
	
	public ArrayList<PGNSyntaxError> getErrors() {
		return errors;
	}		
}
