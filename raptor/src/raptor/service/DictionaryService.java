package raptor.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.util.RaptorStringTokenizer;
import raptor.util.RegExUtils;

public class DictionaryService {
	private static final Log LOG = LogFactory.getLog(DictionaryService.class);
	private static final String DICTIONARY_PATH = Raptor.RESOURCES_DIR
			+ "en_US.txt";
	private static final Pattern VALID_WORD_PATTERN = RegExUtils
			.getPattern("\\p{Alpha}*");
	private static final DictionaryService singletonInstance = new DictionaryService();
	public Set<String> dictionary = new TreeSet<String>();

	private DictionaryService() {
        init();
	}

	public static DictionaryService getInstance() {
		return singletonInstance;
	}

	protected void init() {
		long startTime = System.currentTimeMillis();
		dictionary.clear();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(DICTIONARY_PATH));
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				if (StringUtils.isNotBlank(currentLine)
						&& !currentLine.startsWith("#")) {
					RaptorStringTokenizer tok = new RaptorStringTokenizer(
							currentLine, " /", true);
					dictionary.add(tok.nextToken());
				}
			}
		} catch (Throwable t) {
			Raptor.getInstance().onError(
					"Error reading dictionary file: " + DICTIONARY_PATH, t);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable t) {
				}
			}
		}
		LOG.info("Initialized Dictionary Service " + dictionary.size()
				+ " words in " + (System.currentTimeMillis() - startTime)
				+ "ms");
	}

	public void dispose() {
		dictionary.clear();
		dictionary = null;
	}

	public boolean isValidWord(String word) {
		if (RegExUtils.matches(VALID_WORD_PATTERN, word)) {
			return dictionary.contains(word);
		} else {
			return true;
		}
	}

	public String[] suggest(String word) {
		return new String[0];
	}

}
