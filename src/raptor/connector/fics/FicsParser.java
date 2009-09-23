package raptor.connector.fics;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.connector.fics.chat.CShoutEventParser;
import raptor.connector.fics.chat.ChallengeEventParser;
import raptor.connector.fics.chat.ChannelTellEventParser;
import raptor.connector.fics.chat.ChatEventParser;
import raptor.connector.fics.chat.FollowingEventParser;
import raptor.connector.fics.chat.KibitzEventParser;
import raptor.connector.fics.chat.PartnerTellEventParser;
import raptor.connector.fics.chat.PartnershipCreatedEventParser;
import raptor.connector.fics.chat.PartnershipEndedEventParser;
import raptor.connector.fics.chat.ShoutEventParser;
import raptor.connector.fics.chat.TellEventParser;
import raptor.connector.fics.chat.WhisperEventParser;
import raptor.connector.fics.game.B1Parser;
import raptor.connector.fics.game.G1Parser;
import raptor.connector.fics.game.GameEndParser;
import raptor.connector.fics.game.IllegalMoveParser;
import raptor.connector.fics.game.RemovingObsGameParser;
import raptor.connector.fics.game.Style12Parser;
import raptor.connector.fics.game.message.B1Message;
import raptor.connector.fics.game.message.G1Message;
import raptor.connector.fics.game.message.GameEndMessage;
import raptor.connector.fics.game.message.IllegalMoveMessage;
import raptor.connector.fics.game.message.RemovingObsGameMessage;
import raptor.connector.fics.game.message.Style12Message;
import raptor.service.GameService;
import raptor.util.RaptorStringTokenizer;

public class FicsParser {
	public static final int MAX_GAME_MESSAGE = 1000;
	private static final Log LOG = LogFactory.getLog(FicsParser.class);

	protected Style12Parser style12Parser;
	protected G1Parser g1Parser;
	protected B1Parser b1Parser;
	protected GameEndParser gameEndParser;
	protected IllegalMoveParser illegalMoveParser;
	protected RemovingObsGameParser removingObsGameParser;

	protected List<ChatEventParser> nonGameEventParsers = new ArrayList<ChatEventParser>(
			30);

	public FicsParser() {
		style12Parser = new Style12Parser();
		gameEndParser = new GameEndParser();
		b1Parser = new B1Parser();
		g1Parser = new G1Parser();
		illegalMoveParser = new IllegalMoveParser();
		removingObsGameParser = new RemovingObsGameParser();

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
			events.add(new ChatEvent(null, ChatTypes.UNKNOWN, inboundEvent));
		}
		return events.toArray(new ChatEvent[0]);
	}

	/**
	 * Parses and removes all of the game events from inboundEvent. Adjusts the
	 * games in service. Returns a String with the game events removed.
	 */
	public String parseOutAndProcessGameEvents(GameService service,
			String inboundEvent) {
		if (inboundEvent.length() > MAX_GAME_MESSAGE) {
			return inboundEvent;
		} else {
			StringBuilder result = new StringBuilder(inboundEvent.length());
			RaptorStringTokenizer tok = new RaptorStringTokenizer(inboundEvent,
					"\n");

			while (tok.hasMoreTokens()) {
				String line = tok.nextToken();
				System.err.println("Processing raw line: " + line);

				G1Message g1Message = g1Parser.parse(line);
				if (g1Message != null) {
					process(g1Message, service);
					continue;
				}

				Style12Message style12Message = style12Parser.parse(line);
				if (style12Message != null) {
					process(style12Message, service);
					continue;
				}

				B1Message b1Message = b1Parser.parse(line);
				if (b1Message != null) {
					process(b1Message, service);
					continue;
				}

				GameEndMessage gameEndMessage = gameEndParser.parse(line);
				if (gameEndMessage != null) {
					process(gameEndMessage, service);
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				IllegalMoveMessage illegalMoveMessage = illegalMoveParser
						.parse(line);
				if (illegalMoveMessage != null) {
					process(illegalMoveMessage, service);
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				RemovingObsGameMessage removingObsGameMessage = removingObsGameParser
						.parse(line);
				if (removingObsGameMessage != null) {
					process(removingObsGameMessage, service);
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}
				result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
			}
			return result.toString();
		}
	}

	public void process(G1Message message, GameService service) {
		LOG.debug("Processing g1: " + message);
	}

	public void process(Style12Message message, GameService service) {
		LOG.debug("Processing style 12: " + message);
	}

	public void process(B1Message message, GameService service) {
		LOG.debug("Processing b1: " + message);
	}

	public void process(IllegalMoveMessage message, GameService service) {
		LOG.debug("Processing illegal move: " + message);
	}

	public void process(GameEndMessage message, GameService service) {
		LOG.debug("Processing game end: " + message);
	}

	public void process(RemovingObsGameMessage message, GameService service) {
		LOG.debug("Processing removing obs game: " + message);
	}
}
