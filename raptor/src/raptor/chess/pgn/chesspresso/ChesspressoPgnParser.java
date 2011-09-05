package raptor.chess.pgn.chesspresso;

import java.io.FileReader;
import java.io.IOException;
import chesspresso.game.Game;
import chesspresso.move.Move;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;

import raptor.chess.GameFactory;
import raptor.chess.Variant;
import raptor.chess.pgn.AbstractPgnParser;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnParserListener;
import raptor.swt.PgnProcessingDialog.ChesspressoPgnProgressListener;

public class ChesspressoPgnParser extends AbstractPgnParser {
	
	private PGNReader reader;

	public ChesspressoPgnParser(FileReader fileReader) {
		reader = new PGNReader(fileReader, null);
	}

	@Override
	public int getLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void parse() {		
		Game chessprGame = null;
		try {
			chessprGame = reader.parseGame();
		} catch (PGNSyntaxError e) {
		} catch (IOException e) {
		}		
		do {
			for (PgnParserListener listener: listeners) {
				((ChesspressoPgnProgressListener)listener).
				gameParsed(chessprGame, 0);
			}
			try {
				chessprGame = reader.parseGame();
			} catch (PGNSyntaxError e) {
				for (PgnParserListener listener: listeners) {
					((ChesspressoPgnProgressListener)listener).errorEncountered(null);					
				}
				continue;
			} catch (RuntimeException e) {
				for (PgnParserListener listener: listeners) {
					((ChesspressoPgnProgressListener)listener).errorEncountered(null);					
				}
				continue;
			} catch (IOException e) {
			}
		} while(chessprGame != null);
	}

	public static raptor.chess.Game convertToRaptorGame(Game selectedGame) {
		raptor.chess.Game raptorGame = GameFactory.createStartingPosition(Variant.classic);
		raptorGame.setHeader(PgnHeader.White, selectedGame.getWhite());
		raptorGame.setHeader(PgnHeader.Black, selectedGame.getBlack());
		raptorGame.setHeader(PgnHeader.WhiteElo, selectedGame.getWhiteEloStr());
		raptorGame.setHeader(PgnHeader.BlackElo, selectedGame.getBlackEloStr());
		selectedGame.gotoStart();
		for (Move move: selectedGame.getMainLine()) {
			/*System.out.println(move.getLAN() 
					+ " From: " + move.getFromSqi()
					+ " To: " + move.getToSqi());*/
			raptorGame.makeSanMove(move.getSAN());			
		}
		return raptorGame;
	}

	

}
