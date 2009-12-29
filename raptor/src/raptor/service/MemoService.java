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
