/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
		result.setType(ChatType.valueOf(deserializeField(tok.nextToken())));
		result.setGameId(deserializeField(tok.nextToken()));
		result.setChannel(deserializeField(tok.nextToken()));
		result.setSource(deserializeField(tok.nextToken()));
		result.setMessage(deserializeField(tok.nextToken()));
		return result;
	}

	public static String serializeChatEvent(ChatEvent e) {
		StringBuilder result = new StringBuilder(1000);
        result.append(serializeField(String.valueOf(e.time))).append(FIELD_SEPARATOR);
        result.append(serializeField(String.valueOf(e.type))).append(FIELD_SEPARATOR);
        result.append(serializeField(e.gameId)).append(FIELD_SEPARATOR);
        result.append(serializeField(e.channel)).append(FIELD_SEPARATOR);
        result.append(serializeField(e.source)).append(FIELD_SEPARATOR);
		result.append(serializeField(e.message));
		return result.toString();
	}

	protected static String deserializeField(String field) {
		String result = StringUtils.defaultString(field);
		result = StringUtils.replaceChars(result, NEW_LINE_REPLACEMENT, '\n');
		return result;
	}

	protected static String serializeField(String field) {
		String result = StringUtils.defaultString(field);
		result = StringUtils.replaceChars(result, '\n', NEW_LINE_REPLACEMENT);
		return result;
	}
}
