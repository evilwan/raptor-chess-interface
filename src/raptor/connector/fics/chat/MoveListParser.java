package raptor.connector.fics.chat;

import org.apache.log4j.Logger;

import raptor.chat.ChatEvent;

public class MoveListParser extends ChatEventParser {
	private static final Logger LOGGER = Logger.getLogger(MoveListParser.class);

	private static final String EVENT_START = "Movelist for game ";

	//private ShortAlgebraicEncoder encoder = new ShortAlgebraicEncoder();

	public MoveListParser() {
		super();
	}

	public ChatEvent parse(String string) {
//		if (string.startsWith(EVENT_START)) {
//			int lastDash = string.lastIndexOf("--");
//			int firstColon = string.indexOf(':', 0);
//			int gameNumber = Integer.parseInt(string.substring(EVENT_START
//					.length(), firstColon));
//
//			Position startingPosition = new Position();
//			MoveListModel moveList = new MoveListModel(startingPosition);
//			Position currentPosition = (Position) startingPosition.clone();
//
//			if (lastDash != -1) {
//				try {
//					String afterDash = string.substring(lastDash);
//					BufferedReader reader = new BufferedReader(
//							new StringReader(afterDash));
//					String currentLine = reader.readLine();
//
//					while (currentLine != null) {
//						currentLine = currentLine.trim();
//						if (currentLine.startsWith("{Still in progress}")) {
//							break;
//						} else if (currentLine.startsWith("{")) {
//							moveList.end(currentLine);
//							break;
//						} else {
//							StringTokenizer tok = new StringTokenizer(
//									currentLine, " ");
//
//							String moveNumber = null;
//							String whiteMove = null;
//							String whiteTime = null;
//							String blackMove = null;
//							String blackTime = null;
//
//							if (tok.hasMoreTokens()) {
//								moveNumber = tok.nextToken();
//							}
//							if (tok.hasMoreTokens()) {
//								whiteMove = tok.nextToken();
//							}
//							if (tok.hasMoreTokens()) {
//								whiteTime = tok.nextToken();
//								currentPosition = appendMove(whiteTime,
//										whiteMove, moveList, currentPosition);
//							}
//							if (tok.hasMoreTokens()) {
//								blackMove = tok.nextToken();
//							}
//							if (tok.hasMoreTokens()) {
//								blackTime = tok.nextToken();
//								currentPosition = appendMove(blackTime,
//										blackMove, moveList, currentPosition);
//							}
//						}
//						currentLine = reader.readLine();
//					}
//				} catch (Exception e) {
//					LOGGER.error("Error occured parsing movelist", e);
//					return null;
//				}
//
//				return new MoveListEvent(icsId, string, gameNumber, moveList);
//			} else {
//				return null;
//			}
//		}
		return null;
	}

	// private Position appendMove(String time, String shortAlg,
	// MoveListModel moveList, Position currentPosition) {
	//
	// try {
	// Position afterMovePosition = currentPosition.makeMove(encoder
	// .decode(shortAlg, currentPosition), false);
	//
	// // There is a bug in the PositionUtil that does not reset castling
	// // positions.
	// // For now this is being dealt with by enabling it in the cloned
	// // position.
	// afterMovePosition = new Position(afterMovePosition.getBoard(),
	// true, true, true, true, afterMovePosition
	// .getLastMoveDoublePawnPushFile(), afterMovePosition
	// .isWhitesMove());
	//
	// MoveListModelMove move = new MoveListModelMove(shortAlg,
	// afterMovePosition, timeToLong(time));
	// moveList.append(move);
	//
	// return (Position) afterMovePosition;
	// } catch (IllegalMoveException ime) {
	// LOGGER.error(ime);
	// throw new RuntimeException(ime);
	// }
	// }
	//
	// private int timeToLong(String timeString) {
	// StringTokenizer tok = new StringTokenizer(timeString, "(:.)");
	// int minutes = Integer.parseInt(tok.nextToken());
	// int seconds = Integer.parseInt(tok.nextToken());
	// int millis = Integer.parseInt(tok.nextToken());
	//
	// return minutes * 1000 * 60 + seconds * 1000 + millis;
	// }

	/***************************************************************************
	 * Movelist for game 317:
	 * 
	 * GMFridman (2621) vs. GMLami (2617) --- Fri Nov 23, 10:07 EST 2007
	 * 
	 * Unrated standard match, initial time: 120 minutes, increment: 0 seconds.
	 * 
	 * 
	 * 
	 * Move GMFridman GMLami
	 * 
	 * ---- --------------------- ---------------------
	 * 
	 * 1. d4 (0:00.000) Nf6 (0:00.000)
	 * 
	 * 2. c4 (0:04.795) g6 (0:04.678)
	 * 
	 * 3. Nc3 (0:04.241) d5 (0:04.243)
	 * 
	 * 4. Nf3 (0:03.529) Bg7 (0:04.225)
	 * 
	 * 5. Bf4 (0:04.442) dxc4 (0:35.023)
	 * 
	 * 6. e4 (1:07.852) Bg4 (1:08.073)
	 * 
	 * 7. Bxc4 (2:18.737) O-O (0:05.781)
	 * 
	 * 8. Be2 (6:46.331) Nfd7 (4:37.310)
	 * 
	 * 9. O-O (3:25.254) Nc6 (9:17.124)
	 * 
	 * 10. Be3 (9:11.211) Nb6 (4:41.689)
	 * 
	 * 11. d5 (3:27.965) Bxf3 (0:04.249)
	 * 
	 * 12. gxf3 (1:06.836) Ne5 (5:50.054)
	 * 
	 * 13. Qb3 (5:52.835) c6 (6:57.075)
	 * 
	 * 14. f4 (9:21.271) Ned7 (1:10.942)
	 * 
	 * 15. Rfd1 (2:21.745) Qe8 (16:30.391)
	 * 
	 * 16. a4 (3:31.260) cxd5 (7:01.569)
	 * 
	 * 17. a5 (3:32.960) d4 (4:41.201)
	 * 
	 * 18. Bxd4 (3:30.739) Bxd4 (1:11.232)
	 * 
	 * 19. Rxd4 (0:04.295) Nc8 (22:21.275)
	 * 
	 * 20. e5 (4:42.737) a6 (4:43.694)
	 * 
	 * 21. Qd5 (5:53.150) Nb8 (1:10.783)
	 * 
	 * 22. Qxb7 (1:14.463) Nc6 (7:03.021)
	 * 
	 * 23. Rc4 (2:21.858) N8a7 (0:04.283)
	 * 
	 * 24. Bf3 (0:04.220) Rc8 (0:04.230)
	 * 
	 * 25. Qxa6 (14:03.500) Qd7 (3:30.980)
	 * 
	 * 26. Rd1 (4:47.296) Qe6 (0:04.757)
	 * 
	 * 27. Bg2 (5:52.019) Nxa5 (0:04.266)
	 * 
	 * 28. Qxe6 (1:05.047) fxe6 (0:04.263)
	 * 
	 * 29. Ra4 (1:06.484) N7c6 (0:04.286)
	 * 
	 * 30. Bh3 (0:04.237) Rb8 (2:14.263)
	 * 
	 * 31. Bxe6+ (0:04.284) Kg7 (0:04.266)
	 * 
	 * 32. Nd5 (8:15.413) Nb3 (4:47.634)
	 * 
	 * 33. Bd7 (3:41.072) Nd8 (3:35.402)
	 * 
	 * 34. Nxe7 (2:21.723) Nc5 (0:04.247)
	 * 
	 * 35. Rc4 (1:14:24.313) Nde6 (0:05.403)
	 * 
	 * 36. b4 (0:04.291) Nxd7 (0:04.294)
	 * 
	 * 37. Rxd7 (0:04.388) Rf7 (0:05.140)
	 * 
	 * 38. f5 (0:05.673)
	 * 
	 * {Still in progress}
	 **************************************************************************/

}
