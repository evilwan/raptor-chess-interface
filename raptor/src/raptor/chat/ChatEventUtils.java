package raptor.chat;

import org.apache.commons.lang.StringUtils;

import raptor.util.RaptorStringTokenizer;

public class ChatEventUtils {

	// Use low unicode ascii characters so a hacker can't defeat this feature.
	public static final char FIELD_SEPARATOR = '\u0005';
	public static final char NEW_LINE_REPLACEMENT = '\u0006';

	public static ChatEvent deserializeChatEvent(String lineOfText) {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(lineOfText, ""
				+ FIELD_SEPARATOR, false);
		ChatEvent result = new ChatEvent();
		result.setTime(Long.parseLong(deserializeField(tok.nextToken())));
		result.setType(Integer.parseInt(deserializeField(tok.nextToken())));
		result.setGameId(deserializeField(tok.nextToken()));
		result.setChannel(deserializeField(tok.nextToken()));
		result.setSource(deserializeField(tok.nextToken()));
		result.setMessage(deserializeField(tok.nextToken()));
		return result;
	}

	protected static String deserializeField(String field) {
		String result = StringUtils.defaultString(field);
		result = StringUtils.replaceChars(result, NEW_LINE_REPLACEMENT, '\n');
		return result;
	}

	public static String serializeChatEvent(ChatEvent e) {
		StringBuilder result = new StringBuilder(1000);
		result.append(serializeField("" + e.time) + FIELD_SEPARATOR);
		result.append(serializeField("" + e.type) + FIELD_SEPARATOR);
		result.append(serializeField(e.gameId) + FIELD_SEPARATOR);
		result.append(serializeField(e.channel) + FIELD_SEPARATOR);
		result.append(serializeField(e.source) + FIELD_SEPARATOR);
		result.append(serializeField(e.message));
		return result.toString();
	}

	protected static String serializeField(String field) {
		String result = StringUtils.defaultString(field);
		result = StringUtils.replaceChars(result, '\n', NEW_LINE_REPLACEMENT);
		return result;
	}
}
