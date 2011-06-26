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
package raptor.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatLogger;
import raptor.chat.ChatLogger.ChatEventParseListener;

public class MemoService {
	private static final String MEMOS_FILE = Raptor.USER_RAPTOR_HOME_PATH
			+ "/logs/memos.txt";
	private static final MemoService singletonInstance = new MemoService();
	public static boolean serviceCreated = false;
	public static final SimpleDateFormat FORMAT = new SimpleDateFormat(
			"'['yyyy-MM-dd' 'hh:mma']'");

	public static MemoService getInstance() {
		return singletonInstance;
	}

	protected ChatLogger memoLogger;

	private MemoService() {
		try {
			File file = new File(MEMOS_FILE);
			if (!file.exists()) {
				file.createNewFile();
			}
			memoLogger = new ChatLogger(file.getAbsolutePath(), false);
		} catch (IOException ioe) {
			throw new RuntimeException("Error opening memos file: "
					+ MEMOS_FILE, ioe);
		}
		serviceCreated = true;
	}

	public void dispose() {
	}

	public ChatEvent[] getMemos() {
		final List<ChatEvent> result = new ArrayList<ChatEvent>(100);
		memoLogger.parseFile(new ChatEventParseListener() {
			public boolean onNewEventParsed(ChatEvent event) {
				result.add(event);
				return true;
			}

			public void onParseCompleted() {
			}
		});
		return result.toArray(new ChatEvent[0]);

	}

	public void addMemo(ChatEvent event) {
		memoLogger.write(event);
	}

	public String getMemosHTML() {
		StringBuilder result = new StringBuilder(2000);

		result.append("<html>\n");
		result.append("<body>\n");
		result.append("<h1>Memos</h1>");
		result.append("<ul>");
		ChatEvent[] memos = getMemos();
		for (ChatEvent event : memos) {
			result.append("<li>" + FORMAT.format(new Date(event.getTime()))
					+ event.getMessage() + "</li>\n");
		}
		result.append("</ul>");
		result.append("</body>\n");
		result.append("</html>\n");
		return result.toString();
	}

	public void clearMemos() {
		try {
			memoLogger.delete();
			File file = new File(MEMOS_FILE);
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Error creating memos file: "
					+ MEMOS_FILE, ioe);
		}
	}

}
