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
	public static interface ChatEventParseListener {
		/**
		 * Invoked on each chat event encountered in the file.
		 */
		public void onNewEventParsed(ChatEvent event);
	}

	private static final Log LOG = LogFactory.getLog(ChatLogger.class);

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
						listener.onNewEventParsed(event);
					} catch (Throwable t) {
						LOG.warn("Error reading chat event line " + currentLine
								+ " skipping ChatEvent", t);
					}
					currentLine = reader.readLine();
				}
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
