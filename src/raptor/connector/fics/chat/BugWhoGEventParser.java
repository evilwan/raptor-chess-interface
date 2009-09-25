package raptor.connector.fics.chat;

public class BugWhoGEventParser {

	// private static final Logger LOGGER = Logger
	// .getLogger(BugWhoGEventParser.class);

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

	// public BugWhoGEventParser(int icsId) {
	// super(icsId);
	// }
	//
	// @Override
	// public IcsNonGameEvent parse(String text) {
	// if (text.startsWith(GAMES_IN_PROGRESS)) {
	// StringTokenizer lines = new StringTokenizer(text, "\r\n");
	// String currentLine = lines.nextToken();
	// List<BugWhoGGame> games = new LinkedList<BugWhoGGame>();
	//							
	//
	// while (lines.hasMoreTokens()) {
	// currentLine = lines.nextToken();
	//
	// if (currentLine.endsWith(DISPLAYED))
	// {
	// break;
	// }
	// else if (currentLine.trim().equals(""))
	// {
	// continue;
	// }
	//
	// int spaceIndex = currentLine.indexOf(" ",1);
	// BugWhoGGame game = new BugWhoGGame();
	//				
	// if (spaceIndex == -1)
	// {
	// break;
	// }
	// else
	// {
	// try
	// {
	// game.setGame1Id(Integer.parseInt(currentLine.substring(0,spaceIndex).trim()));
	// game.setGame1Description(currentLine.substring(spaceIndex +
	// 1,currentLine.length()).trim());
	// currentLine = lines.nextToken();
	// spaceIndex = currentLine.indexOf(" ",1);
	// game.setGame2Id(Integer.parseInt(currentLine.substring(0,spaceIndex).trim()));
	// game.setGame2Description(currentLine.substring(spaceIndex +
	// 1,currentLine.length()).trim());
	// games.add(game);
	// }
	// catch (Exception e)
	// {
	// LOGGER.error("Unexpected error occured:",e);
	// break;
	// }
	// }
	// }
	//
	// return new BugWhoGEvent(getIcsId(), text, games);
	// } else {
	// return null;
	// }
	// }
	//
	// private static final String GAMES_IN_PROGRESS =
	// "Bughouse games in progress";
	//
	// private static final String DISPLAYED = "displayed.";
}
