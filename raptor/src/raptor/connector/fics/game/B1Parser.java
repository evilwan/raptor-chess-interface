package raptor.connector.fics.game;

import raptor.connector.fics.game.message.B1Message;
import raptor.game.GameConstants;
import raptor.util.RaptorStringTokenizer;

public class B1Parser implements GameConstants {
	public static final String B1_START = "<b1>";

	public static int[] buildPieceHoldingsArray(String s) {
		int[] result = new int[6];

		for (int i = 0; i < s.length(); i++) {
			switch (s.charAt(i)) {
			case 'P':
			case 'p':
				result[PAWN] = result[PAWN] + 1;
				break;

			case 'N':
			case 'n':
				result[KNIGHT] = result[KNIGHT] + 1;
				break;

			case 'B':
			case 'b':
				result[BISHOP] = result[BISHOP] + 1;
				break;

			case 'R':
			case 'r':
				result[ROOK] = result[ROOK] + 1;
				break;

			case 'Q':
			case 'q':
				result[QUEEN] = result[QUEEN] + 1;
				break;
			case 'K':
			case 'k':
				result[KING] = result[KING] + 1;
				break;
			default:
				throw new IllegalArgumentException("Invalid piece "
						+ s.charAt(i));
			}
		}

		return result;
	}

	public B1Message parse(String message) {
		if (message.startsWith(B1_START)) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(message,
					" {}><-\n", true);
			B1Message result = new B1Message();

			tok.nextToken();
			tok.nextToken();
			result.gameId = tok.nextToken();
			tok.nextToken();
			String whiteHoldings = tok.nextToken();
			result.whiteHoldings = buildPieceHoldingsArray(whiteHoldings
					.substring(1, whiteHoldings.length() - 1));
			tok.nextToken();
			String blackHoldings = tok.nextToken();
			result.blackHoldings = buildPieceHoldingsArray(blackHoldings
					.substring(1, blackHoldings.length() - 1));
			return result;
		}
		return null;
	}
}
