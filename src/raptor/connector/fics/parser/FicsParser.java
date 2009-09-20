package raptor.connector.fics.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;

public class FicsParser {
	public static final int MAX_GAME_MESSAGE = 1000;

	public static final String LF = "\n";

	public static final String CR = "\r";

	public static final String STYLE_12 = "<12>";

	public static final String GAME_END = "{Game";

	public static final String B1 = "<b1>";

	public static final String G1 = "<g1>";

	public static final String ILLEGAL_MOVE = "Illegal move (";

	private static final Logger LOGGER = Logger.getLogger(FicsParser.class);

	private static final int STYLE_12_INDEX = 0;

	private static final int GAME_END_INDEX = 1;

	private static final int B1_INDEX = 2;

	private static final int G1_INDEX = 3;

	private static final int ILLEGAL_INDEX = 4;

	// private Style12Parser style12Parser;

	// private G1Parser g1Parser;

	// private B1Parser b1Parser;

	// private GameEndParser gameEndParser;

	// private IllegalMoveParser illegalMoveParser;

	// private List<GameStartEvent> orhpanedG1s = new
	// LinkedList<GameStartEvent>();

	// private List<Class> classesHidingFromUser = new LinkedList<Class>();

	/**
	 * Tricky case where a G1 gets oprhaned its rare: [java] <g1> 329 p=0
	 * t=bughouse r=1 u=0,0 it=120,0 i=120,0 pt=271 rt=1597,1583 ts=1,1 m=2 n=0
	 * 
	 * [java] Game notification: BillJr (1489) vs. lomez (1653) rated bughouse 2
	 * 0: Game 271 [java] fics% [java] <12> rnbqkbnr pppppppp -------- --------
	 * -------- -------- PPPPPPPP RNBQKBNR W -1 1 1 1 1 0 329 GenyaG cday -1 2 0
	 * 39 39 120000 120000 1 none (0:00.000) none 1 1 0 [java] <b1> game 329
	 * white [] black []2007-12-16 18:28:46,219 DEBUG (?:?) - style12Index=350
	 * gameEndIndex=56 b1Index=515 g1Index=173 illegalIndex=-1 [java] 2007-12-16
	 * 18:28:46,219 DEBUG (?:?) - Creating: GenyaG (1597) cday (1583) rated
	 * bughouse 2 0
	 * 
	 * [java] {Game 329 (GenyaG vs. cday) Creating rated bughouse match.} [java]
	 * Your partner is playing game 271 (BillJr vs. lomez).
	 * 
	 * [java] <g1> 329 p=0 t=bughouse r=1 u=0,0 it=120,0 i=120,0 pt=271
	 * rt=1597,1583 ts=1,1 m=2 n=0
	 * 
	 * [java] Game notification: BillJr (1489) vs. lomez (1653) rated bughouse 2
	 * 0: Game 271 [java] fics% [java] <12> rnbqkbnr pppppppp -------- --------
	 * -------- -------- PPPPPPPP RNBQKBNR W -1 1 1 1 1 0 329 GenyaG cday -1 2 0
	 * 39 39 120000 120000 1 none (0:00.000) none 1 1 0 [java] <b1> game 329
	 * white [] black []2007-12-16 18:28:46,220 DEBUG (?:?) - style12Index=289
	 * gameEndIndex=-1 b1Index=454 g1Index=112 illegalIndex=-1 [java] 2007-12-16
	 * 18:28:46,220 DEBUG (?:?) - Creating: GenyaG (1597) cday (1583) rated
	 * bughouse 2 0
	 * 
	 * [java] Your partner is playing game 271 (BillJr vs. lomez).
	 * 
	 * [java] <g1> 329 p=0 t=bughouse r=1 u=0,0 it=120,0 i=120,0 pt=271
	 * rt=1597,1583 ts=1,1 m=2 n=0
	 * 
	 */

	List<ChatEventParser> nonGameEventParsers = new ArrayList<ChatEventParser>(
			30);

	public FicsParser() {
		// style12Parser = new Style12Parser(icsId);
		// gameEndParser = new GameEndParser(icsId);
		// b1Parser = new B1Parser(icsId);
		// g1Parser = new G1Parser(icsId);
		// illegalMoveParser = new IllegalMoveParser(icsId);

		// handle user tell types of events first so others can't be spoofed
		// nonGameEventParsers.add(new BugWhoGEventParser(icsId));
		// nonGameEventParsers.add(new BugWhoPEventParser(icsId));
		// nonGameEventParsers.add(new BugWhoUEventParser(icsId));
		// nonGameEventParsers.add(new SoughtEventParser());
		nonGameEventParsers.add(new ChannelTellEventParser());
		nonGameEventParsers.add(new CShoutEventParser());
		nonGameEventParsers.add(new ShoutEventParser());
		nonGameEventParsers.add(new KibitzEventParser());
		nonGameEventParsers.add(new PartnerTellEventParser());
		nonGameEventParsers.add(new TellEventParser());
		nonGameEventParsers.add(new WhisperEventParser());

		// Non tell types of events.
		// nonGameEventParsers.add(new MoveListParser(icsId));
		nonGameEventParsers.add(new ChallengeEventParser());
		nonGameEventParsers.add(new PartnershipCreatedEventParser());
		nonGameEventParsers.add(new PartnershipEndedEventParser());
		nonGameEventParsers.add(new FollowingEventParser());
	}

	public ChatEvent[] parse(String inboundEvent) {
		List<ChatEvent> events = new ArrayList<ChatEvent>(5);
		for (ChatEventParser parser : nonGameEventParsers) {
			ChatEvent event = parser.parse(inboundEvent);
			if (event != null) {
				events.add(event);
			}
		}
		if (events.isEmpty()) {
			events.add(new ChatEvent(null, ChatTypes.UNKNOWN,
					inboundEvent));
		}
		return events.toArray(new ChatEvent[0]);

	}
	// public void hideNextClassFromUser(Class clazz) {
	// classesHidingFromUser.add(clazz);
	// }

	// public IcsInboundEvent[] parse(StringBuffer message) {
	// long startTime = System.currentTimeMillis();
	// if (LOGGER.isDebugEnabled()) {
	// LOGGER.debug("raw in: " + message);
	// }
	//
	// List<IcsInboundEvent> result = new ArrayList<IcsInboundEvent>(10);
	//
	// if (message.length() < MAX_GAME_MESSAGE) {
	// int style12Index = indexOf(message, STYLE_12);
	// int gameEndIndex = indexOf(message, GAME_END);
	// int b1Index = indexOf(message, B1);
	// int g1Index = indexOf(message, G1);
	// int illegalIndex = indexOf(message, ILLEGAL_MOVE);
	//
	// while (style12Index != -1 || gameEndIndex != -1 || b1Index != -1
	// || g1Index != -1 || illegalIndex != -1) {
	//
	// if (LOGGER.isDebugEnabled()) {
	// LOGGER.debug("style12Index=" + style12Index
	// + " gameEndIndex=" + gameEndIndex + " b1Index="
	// + b1Index + " g1Index=" + g1Index
	// + " illegalIndex=" + illegalIndex + "\n" + message);
	// }
	//
	// switch (leastIndex(style12Index, gameEndIndex, b1Index,
	// g1Index, illegalIndex)) {
	// case ILLEGAL_INDEX: {
	// int charAfterNextLT = charAfterNextLT(message, illegalIndex);
	// int charBeforeNextLT = charBeforeNextLT(message,
	// illegalIndex);
	// String illegalMoveString = message
	// .substring(
	// illegalIndex,
	// charBeforeNextLT == message.length() ? charBeforeNextLT
	// : charBeforeNextLT + 1);
	// result.add(illegalMoveParser.parse(illegalMoveString));
	// result.add(new IcsNonGameEvent(icsId, illegalMoveString));
	// message = message.delete(illegalIndex, charAfterNextLT);
	// break;
	// }
	// case GAME_END_INDEX: {
	// int charAfterNextLT = charAfterNextLT(message, gameEndIndex);
	// int charBeforeNextLT = charBeforeNextLT(message,
	// gameEndIndex);
	//
	// String gameEndString = message
	// .substring(
	// gameEndIndex,
	// charBeforeNextLT == message.length() ? charBeforeNextLT
	// : charBeforeNextLT + 1);
	// result.add(gameEndParser.parse(gameEndString));
	// result.add(new IcsNonGameEvent(icsId, gameEndString));
	// message = message.delete(gameEndIndex, charAfterNextLT);
	// break;
	// }
	// case B1_INDEX: {
	// int charAfterNextLT = charAfterNextLT(message, b1Index);
	// int charBeforeNextLT = charBeforeNextLT(message, b1Index);
	// result.add(b1Parser.parse(message
	// .substring(b1Index, charBeforeNextLT == message
	// .length() ? charBeforeNextLT
	// : charBeforeNextLT + 1)));
	// message = message.delete(b1Index, charAfterNextLT);
	// break;
	// }
	// case STYLE_12_INDEX: {
	// int charAfterNextLT = charAfterNextLT(message, style12Index);
	// int charBeforeNextLT = charBeforeNextLT(message,
	// style12Index);
	// if (b1Index != -1 && b1Index == charAfterNextLT) {
	// int charAfterNextLT2 = charAfterNextLT(message, b1Index);
	// int charBeforeNextLT2 = charBeforeNextLT(message,
	// b1Index);
	// MoveEvent moveEvent = style12Parser
	// .parse(
	// message.substring(style12Index,
	// charBeforeNextLT + 1),
	// message
	// .substring(
	// b1Index,
	// charBeforeNextLT2 == message
	// .length() ? charBeforeNextLT2
	// : charBeforeNextLT2 + 1));
	//
	// GameStartEvent orphanedGameStart = findOrpahnedGameStart(moveEvent);
	//
	// if (orphanedGameStart != null) {
	// result.add(orphanedGameStart);
	// } else {
	// result.add(moveEvent);
	// }
	//
	// message = message
	// .delete(style12Index, charAfterNextLT2);
	// } else {
	// MoveEvent moveEvent = style12Parser
	// .parse(message
	// .substring(
	// style12Index,
	// charBeforeNextLT == message
	// .length() ? charBeforeNextLT
	// : charBeforeNextLT + 1));
	//
	// GameStartEvent orphanedGameStart = findOrpahnedGameStart(moveEvent);
	//
	// if (orphanedGameStart != null) {
	// result.add(orphanedGameStart);
	// } else {
	// result.add(moveEvent);
	// }
	// message = message.delete(style12Index, charAfterNextLT);
	// }
	// break;
	// }
	// case G1_INDEX: {
	// int charAfterNextLT = charAfterNextLT(message, g1Index);
	// int charBeforeNextLT = charBeforeNextLT(message, g1Index);
	// if (style12Index != -1 && style12Index == charAfterNextLT) {
	// int charAfterNextLT2 = charAfterNextLT(message,
	// style12Index);
	// int charBeforeNextLT2 = charBeforeNextLT(message,
	// style12Index);
	// if (b1Index != -1 && b1Index == charAfterNextLT2) {
	// int charAfterNextLT3 = charAfterNextLT(message,
	// b1Index);
	// int charBeforeNextLT3 = charBeforeNextLT(message,
	// b1Index);
	// result
	// .add(g1Parser
	// .parse(
	// message
	// .substring(
	// g1Index,
	// charBeforeNextLT + 1),
	// message
	// .substring(
	// style12Index,
	// charBeforeNextLT2 + 1),
	// message
	// .substring(
	// b1Index,
	// charBeforeNextLT3 == message
	// .length() ? charBeforeNextLT3
	// : charBeforeNextLT3 + 1)));
	//
	// message = message.delete(g1Index, charAfterNextLT3);
	// } else {
	// result
	// .add(g1Parser
	// .parse(
	// message
	// .substring(
	// g1Index,
	// charBeforeNextLT + 1),
	// message
	// .substring(
	// style12Index,
	// charBeforeNextLT2 == message
	// .length() ? charBeforeNextLT2
	// : charBeforeNextLT2 + 1)));
	//
	// message = message.delete(g1Index, charAfterNextLT2);
	// }
	// } else {
	// // Oprhaned G1 case: style 12 should be here shortly.
	// if (LOGGER.isDebugEnabled()) {
	// LOGGER
	// .debug("Received orphan G1 event without matching style 12, waiting for matching style 12");
	// }
	// orhpanedG1s.add(g1Parser.parse(message.substring(
	// g1Index, charBeforeNextLT + 1)));
	// message = message.delete(g1Index, charAfterNextLT);
	// }
	// break;
	// }
	// default: {
	// throw new RuntimeException(
	// "Invalid value returned from leastIndex");
	// }
	//
	// }
	// style12Index = indexOf(message, STYLE_12);
	// gameEndIndex = indexOf(message, GAME_END);
	// b1Index = indexOf(message, B1);
	// g1Index = indexOf(message, G1);
	// illegalIndex = indexOf(message, ILLEGAL_MOVE);
	// }
	//
	// String messageString = message.toString().trim();
	//
	// if (messageString.length() > 0) {
	// parseNonGameMessages(result, messageString);
	// }
	// } else {
	// parseNonGameMessages(result, message.toString());
	// }
	//
	// if (LOGGER.isDebugEnabled()) {
	// LOGGER.debug("Time to parse inbound message: "
	// + (System.currentTimeMillis() - startTime));
	// }
	// return result.toArray(new IcsInboundEvent[0]);
	// }
	//
	// private GameStartEvent findOrpahnedGameStart(MoveEvent moveEvent) {
	// GameStartEvent result = null;
	// if (!orhpanedG1s.isEmpty()) {
	// for (int i = 0; result == null && i < orhpanedG1s.size(); i++) {
	// GameStartEvent current = orhpanedG1s.get(i);
	// if (current.getGameId() == moveEvent.getGameId()) {
	// result = current;
	// orhpanedG1s.remove(i);
	// }
	// }
	// }
	//
	// if (result != null) {
	// if (LOGGER.isDebugEnabled()) {
	// LOGGER.debug("Found orphaned G1 events style 12.");
	// }
	// result.setFirstEvent(moveEvent);
	// }
	// return result;
	// }
	//
	// /**
	// * Returns the char after the next line termination sequence, or if there
	// is
	// * not one message.length(). Line terminator is /r/n/r in winblows ,/n/r
	// in
	// * osx, /r in linux.
	// *
	// * @param message
	// * The string buffer to search on
	// * @param startIndex
	// * The index to start the search at
	// * @return The index
	// */
	// private int charAfterNextLT(StringBuffer message, int startIndex) {
	// int nextLT = message.indexOf(CR, startIndex);
	//
	// if (nextLT == -1) {
	// nextLT = message.indexOf(LF, startIndex);
	// }
	//
	// if (nextLT != -1) {
	// while (nextLT + 1 != message.length()
	// && (message.charAt(nextLT + 1) == '\r' || message
	// .charAt(nextLT + 1) == '\n')) {
	// nextLT++;
	// }
	// return nextLT + 1;
	// } else {
	// return message.length();
	// }
	// }
	//
	// /**
	// * Returns the char before the next Line termination character sequence,
	// or
	// * if there is not one message.length().
	// *
	// * @param message
	// * The string buffer to search on
	// * @param startIndex
	// * The index to start the search at
	// * @return The index
	// */
	// private int charBeforeNextLT(StringBuffer message, int startIndex) {
	// int nextLT = message.indexOf(CR, startIndex);
	//
	// if (nextLT == -1) {
	// nextLT = message.indexOf(LF, startIndex);
	// }
	//
	// if (nextLT != -1 && nextLT != 0) {
	// return nextLT - 1;
	//
	// } else if (nextLT == 0) {
	// return 0;
	// } else {
	// return message.length();
	// }
	// }
	//
	// /**
	// * Returns the index of search in message where search is preceeded by a
	// LT
	// * or its at the begining of the string. LT is /r/n/r in windows, /n/r in
	// * OSX,/r in linux.
	// *
	// * @param message
	// * The string buffer to search on
	// * @param search
	// * The string to search for
	// * @return The index
	// */
	// private int indexOf(StringBuffer message, String search) {
	// int indexWithoutLT = message.indexOf(search);
	//
	// if (indexWithoutLT == 0) {
	// return 0;
	// } else if (indexWithoutLT != -1) {
	// if (message.charAt(indexWithoutLT - 1) == '\r'
	// || message.charAt(indexWithoutLT - 1) == '\n') {
	// return indexWithoutLT;
	// } else {
	// return -1;
	// }
	// } else {
	// return -1;
	// }
	// }
	//
	// private void parseNonGameMessages(List<IcsInboundEvent> events,
	// String message) {
	// boolean parsedEvent = false;
	//
	// try {
	// for (ChatEventParser parser : nonGameEventParsers) {
	// IcsNonGameEvent event = parser.parse(message);
	//
	// if (event != null) {
	// events.add(event);
	// if (classesHidingFromUser.contains(event.getClass())) {
	// classesHidingFromUser.remove(event.getClass());
	// event.setHideFromUser(true);
	// }
	// parsedEvent = true;
	// break;
	// }
	// }
	// } catch (Throwable t) {
	// LOGGER.error("Error occured parsing non game event:", t);
	// }
	//
	// if (!parsedEvent) {
	// events.add(new IcsNonGameEvent(icsId, message));
	// }
	// }
	//
	// private int leastIndex(int style12Index, int gameEndIndex, int b1Index,
	// int g1Index, int illegalIndex) {
	// int modStyle12Index = style12Index == -1 ? Integer.MAX_VALUE
	// : style12Index;
	// int modGameEndIndex = gameEndIndex == -1 ? Integer.MAX_VALUE
	// : gameEndIndex;
	// int modB1Index = b1Index == -1 ? Integer.MAX_VALUE : b1Index;
	// int modG1Index = g1Index == -1 ? Integer.MAX_VALUE : g1Index;
	// int modIllegal = illegalIndex == -1 ? Integer.MAX_VALUE : illegalIndex;
	//
	// if (modStyle12Index < modGameEndIndex && modStyle12Index < modB1Index
	// && modStyle12Index < modG1Index && modStyle12Index < modIllegal) {
	// return STYLE_12_INDEX;
	// } else if (modGameEndIndex < modStyle12Index
	// && modGameEndIndex < modB1Index && modGameEndIndex < modG1Index
	// && modGameEndIndex < modIllegal) {
	// return GAME_END_INDEX;
	// } else if (modB1Index < modStyle12Index && modB1Index < modGameEndIndex
	// && modB1Index < modG1Index && modB1Index < modIllegal) {
	// return B1_INDEX;
	// } else if (modG1Index < modB1Index && modG1Index < modStyle12Index
	// && modG1Index < modGameEndIndex && modG1Index < modIllegal) {
	// return G1_INDEX;
	// } else {
	// return ILLEGAL_INDEX;
	// }
	// }

}
