/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logs chat messages to a file and provides, and allows a
 * ChatEventParseListener to parse the file.
 * 
 * This is being used to add old tells to a newly created Channel or Person tab.
 * 
 */
public class ChatLogger {
	private static final Log LOG = LogFactory.getLog(ChatLogger.class);

	public static interface ChatEventParseListener {
		/**
		 * Invoked on each chat event encountered in the file. Returns true if
		 * the parse should continue, false if it should cease.
		 */
		public boolean onNewEventParsed(ChatEvent event);

		public void onParseCompleted();
	}

	protected String pathToFile;

	/**
	 * Constructs a ChatLogger which writes to the specified file.
	 * 
	 * @param pathToFile
	 */
	public ChatLogger(String pathToFile) {
		this.pathToFile = pathToFile;
		delete();
	}

	/**
	 * Deletes the backing file.
	 */
	public void delete() {
		File file = new File(pathToFile);
		file.delete();
	}

	/**
	 * Parses the ChatLogger and invokes the listener on each chat event
	 * encountered.
	 */
	public void parseFile(ChatEventParseListener listener) {
		synchronized (this) {
			@SuppressWarnings("unused")
			long startTime = System.currentTimeMillis();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(pathToFile));
				String currentLine = reader.readLine();
				while (currentLine != null) {
					try {
						ChatEvent event = ChatEventUtils
								.deserializeChatEvent(currentLine);
						if (!listener.onNewEventParsed(event)) {
							break;
						}
					} catch (Throwable t) {
						LOG.warn("Error reading chat event line " + currentLine
								+ " skipping ChatEvent", t);
					}
					currentLine = reader.readLine();
				}
				listener.onParseCompleted();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable t) {
					}
				}
			}
		}
	}

	/**
	 * Writes a chat even to this chat logger.
	 */
	public void write(ChatEvent event) {
		synchronized (this) {
			if (event.getMessage().length() < 1500) {
				@SuppressWarnings("unused")
				long startTime = System.currentTimeMillis();
				FileWriter writer = null;
				try {
					writer = new FileWriter(pathToFile, true);
					writer.write(ChatEventUtils.serializeChatEvent(event)
							+ "\n");
				} catch (Throwable t) {
					LOG.warn("Error occured writihg chat event: ", t);
				} finally {
					if (writer != null) {
						try {
							writer.close();
						} catch (Throwable t) {
						}
					}
				}
			}
		}
	}
}
