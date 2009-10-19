package raptor.connector.ics.bughouse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.bughouse.Bugger;
import raptor.bughouse.BughouseGame;
import raptor.bughouse.Bugger.BuggerStatus;
import raptor.util.RaptorStringTokenizer;

public class BugWhoGParser {

	/**
	 * Bughouse games in progress 65 1613 crankinhaus 1692 RooRooBear [ Br 2 0]
	 * 1:12 - 1:35 (35-36) B: 19 280 1794 Tinker 1847 sadness [ Br 2 0] 1:13 -
	 * 1:32 (43-42) B: 18
	 * 
	 * 27 1986 Nathaniel 1964 PariahCare [ Br 3 0] 1:37 - 1:25 (31-41) B: 25 110
	 * 1514 HawaiianKin 1615 Poindexter [ Br 3 0] 1:35 - 1:19 (47-37) B: 30
	 * 
	 * 66 2130 gorbunaak 1713 FigureOfLi [ Br 2 0] 1:03 - 1:30 (28-33) W: 19 187
	 * 2029 nikechessni 1799 Jlexa [ Br 2 0] 1:17 - 1:19 (50-45) W: 18
	 * 
	 * 3 games displayed.
	 * 
	 * 2 partnerships displayed.
	 */

	private static final Log LOG = LogFactory.getLog(BugWhoUParser.class);

	public static final String ID = "Bughouse games in progress\n";
	public static final String ID2 = "\nBughouse games in progress\n";

	public BugWhoGParser() {
	}

	public BughouseGame[] parse(String message) {
		if (message.startsWith(ID) && !message.contains(BugWhoPParser.ID)) {
			message = message.substring(ID.length(), message.length());
			message = message.replaceAll("[0-9]+ games displayed.", "");
			message = message.replaceAll("1 game displayed.", "");
			message = message.replaceAll("\nfics%", "");
			return process(message.trim());
		} else if (message.startsWith(ID2)
				&& !message.contains(BugWhoPParser.ID)) {
			message = message.replaceAll("[0-9]+ games displayed.", "");
			message = message.replaceAll("1 game displayed.", "");
			message = message.replaceAll("\nfics%", "");
			return process(message.trim());
		}
		return null;
	}

	private BughouseGame[] process(String text) {
		System.err.println(text);
		if (text.equals("")) {
			return new BughouseGame[0];
		}
		RaptorStringTokenizer tok = new RaptorStringTokenizer(text,
				" \n[]-():", true);
		List<BughouseGame> result = new ArrayList<BughouseGame>(10);
		while (tok.hasMoreTokens()) {
			BughouseGame game = new BughouseGame();
			game.setGame1Id(tok.nextToken());
			game.setGame1White(new Bugger());
			game.getGame1White().setRating(tok.nextToken());
			game.getGame1White().setName(tok.nextToken());
			game.getGame1White().setStatus(BuggerStatus.Available);
			game.setGame1Black(new Bugger());
			game.getGame1Black().setRating(tok.nextToken());
			game.getGame1Black().setName(tok.nextToken());
			game.getGame1Black().setStatus(BuggerStatus.Available);
			game.setRated(tok.nextToken().indexOf('r') != -1);
			game.setTimeControl(tok.nextToken() + " " + tok.nextToken());
			for (int i = 0; i < 8; i++) {
				tok.nextToken();
			}
			game.setGame2Id(tok.nextToken());
			game.setGame2White(new Bugger());
			game.getGame2White().setRating(tok.nextToken());
			game.getGame2White().setName(tok.nextToken());
			game.getGame2White().setStatus(BuggerStatus.Available);
			game.setGame2Black(new Bugger());
			game.getGame2Black().setRating(tok.nextToken());
			game.getGame2Black().setName(tok.nextToken());
			game.getGame2Black().setStatus(BuggerStatus.Available);

			for (int i = 0; i < 11; i++) {
				tok.nextToken();
			}
			result.add(game);

		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Games = " + result);
		}
		return result.toArray(new BughouseGame[0]);
	}
}
