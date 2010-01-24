/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
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
			+ "words.txt";
	private static final String FICS_DICTIONARY_PATH = Raptor.RESOURCES_DIR
			+ "customDictionary.txt";
	private static final String USER_DICTIONARY_PATH = Raptor.USER_RAPTOR_HOME_PATH
			+ "/customDictionary.txt";

	private static final Pattern VALID_WORD_PATTERN = RegExUtils
			.getPattern("\\p{Alpha}*");
	private static final Pattern MIXED_CASE_PATTERN = RegExUtils
			.getPattern(".*\\p{Upper}.*");
	private static final DictionaryService singletonInstance = new DictionaryService();

	public Set<String> ficsWords = new TreeSet<String>();

	private DictionaryService() {
		init();
	}

	public static DictionaryService getInstance() {
		return singletonInstance;
	}

	public void addWord(String word) {
		if (!ficsWords.contains(word.toLowerCase())) {
			ficsWords.add(word);
			FileWriter writer = null;
			try {
				writer = new FileWriter(Raptor.USER_RAPTOR_HOME_PATH
						+ "/customDictionary.txt", false);
				for (String currentWord : ficsWords) {
					writer.write(currentWord + "\n");
				}
				writer.flush();
			} catch (Throwable t) {
				Raptor.getInstance().onError(
						"Error occured saving dictionary file "
								+ USER_DICTIONARY_PATH, t);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException ioe) {
					}
				}
			}
		}
	}

	/**
	 * This method started out as code from
	 * http://stackoverflow.com/questions/736556/binary
	 * -search-in-a-sorted-memory-mapped-file-in-java
	 * 
	 * It has been altered to be case insensitive and return a boolean.
	 */
	protected boolean binarySearch(String filename, String string) {
		string = string.toLowerCase();
		long startTime = System.currentTimeMillis();
		RandomAccessFile raf = null;
		boolean result = false;
		try {
			File file = new File(filename);
			raf = new RandomAccessFile(file, "r");

			long low = 0;
			long high = file.length();

			long p = -1;
			while (low < high) {
				long mid = (low + high) / 2;
				p = mid;
				while (p >= 0) {
					raf.seek(p);

					char c = (char) raf.readByte();
					if (c == '\n')
						break;
					p--;
				}
				if (p < 0)
					raf.seek(0);
				String line = raf.readLine();
				// System.out.println("-- " + mid + " " + line);
				if (line.toLowerCase().compareTo(string) < 0)
					low = mid + 1;
				else
					high = mid;
			}

			p = low;
			while (p >= 0) {
				raf.seek(p);
				if (((char) raf.readByte()) == '\n')
					break;
				p--;
			}

			if (p < 0)
				raf.seek(0);

			while (true) {
				String line = raf.readLine().toLowerCase();
				if (!line.startsWith(string)) {
					result = false;
					break;
				} else if (line.equals(string)) {
					result = true;
					break;
				}
			}
		} catch (Throwable t) {
			Raptor.getInstance().onError(
					"Error reading dictionary file: " + DICTIONARY_PATH, t);
		} finally {
			try {
				raf.close();
			} catch (Throwable t) {
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Searched " + string + " ("
						+ (System.currentTimeMillis() - startTime) + ") "
						+ result);
			}

		}
		return result;
	}

	protected void init() {
		long startTime = System.currentTimeMillis();
		ficsWords.clear();
		BufferedReader reader = null;
		try {
			File userFile = new File(USER_DICTIONARY_PATH);
			if (userFile.exists()) {
				reader = new BufferedReader(new FileReader(userFile));
			} else {
				reader = new BufferedReader(
						new FileReader(FICS_DICTIONARY_PATH));
			}
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				if (StringUtils.isNotBlank(currentLine)
						&& !currentLine.startsWith("#")) {
					RaptorStringTokenizer tok = new RaptorStringTokenizer(
							currentLine, " /", true);
					ficsWords.add(tok.nextToken());
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
		LOG.info("Initialized Dictionary Service " + ficsWords.size()
				+ " words in " + (System.currentTimeMillis() - startTime)
				+ "ms");
	}

	public void dispose() {
		ficsWords.clear();
		ficsWords = null;
	}

	public boolean isValidWord(String word) {
		if (RegExUtils.matches(VALID_WORD_PATTERN, word)
				&& !RegExUtils.matches(MIXED_CASE_PATTERN, word)
				&& !ficsWords.contains(word.toLowerCase())) {
			return binarySearch(DICTIONARY_PATH, word);
		} else {
			return true;
		}
	}

	public String[] suggest(String word) {
		return new String[0];
	}

}
