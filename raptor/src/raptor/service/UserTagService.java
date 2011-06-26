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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;

public class UserTagService {
	private static final RaptorLogger LOG = RaptorLogger.getLog(UserTagService.class);
	private static final String TAG_FILE = Raptor.USER_RAPTOR_HOME_PATH
			+ "/logs/tags.txt";
	private static final UserTagService singletonInstance = new UserTagService();
	public static boolean serviceCreated = false;
	protected Map<String, Set<String>> tagToUsersMap = new TreeMap<String, Set<String>>();

	public static UserTagService getInstance() {
		return singletonInstance;
	}

	private UserTagService() {
		try {
			File file = new File(TAG_FILE);
			if (!file.exists()) {
				file.createNewFile();
			} else {
				loadFile();
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Error opening tags file: " + TAG_FILE,
					ioe);
		}
		serviceCreated = true;
	}

	protected void loadFile() {
		tagToUsersMap.clear();
		BufferedReader reader = null;
		int counter = 0;
		try {
			reader = new BufferedReader(new FileReader(TAG_FILE));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtils.isNotBlank(line)) {
					RaptorStringTokenizer tok = new RaptorStringTokenizer(line,
							"" + FIELD_SEPARATOR, true);
					String tag = tok.nextToken().toLowerCase();
					Set<String> users = new TreeSet<String>();
					while (tok.hasMoreTokens()) {
						users.add(tok.nextToken().toLowerCase());
						counter++;
					}
					tagToUsersMap.put(tag, users);
				}
			}
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error parsing tag file: " + TAG_FILE,
					t);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable t) {
				}
			}
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Loaded " + counter + " tagged users.");
		}
	}

	protected void saveFile() {
		FileWriter writer = null;
		try {
			writer = new FileWriter(TAG_FILE, false);
			String[] tags = tagToUsersMap.keySet().toArray(new String[0]);
			for (String tag : tags) {
				Set<String> users = tagToUsersMap.get(tag);
				if (users.size() > 0) {
					String line = tag.toLowerCase() + FIELD_SEPARATOR
							+ serializSet(users);
					writer.write(line + "\n");
				}
			}
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error saving tag file: " + TAG_FILE,
					t);
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	public static final char FIELD_SEPARATOR = '\u0005';

	protected static Set<String> deserializeSet(String lineOfText) {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(lineOfText, ""
				+ FIELD_SEPARATOR, false);
		Set<String> result = new TreeSet<String>();
		while (tok.hasMoreTokens()) {
			result.add(tok.nextToken().toLowerCase());
		}
		return result;
	}

	protected static String serializSet(Set<String> set) {
		StringBuilder result = new StringBuilder(1000);
		String[] users = set.toArray(new String[0]);
		for (int i = 0; i < users.length; i++) {
			result.append(users[i]
					+ (i < users.length - 1 ? FIELD_SEPARATOR : ""));
		}
		return result.toString();
	}

	public void dispose() {
		tagToUsersMap.clear();
	}

	public String[] getTags(String user) {
		user = user.toLowerCase();
		List<String> result = new ArrayList<String>(10);
		String[] tags = getTags();
		for (String tag : tags) {
			String lowerCaseTag = tag.toLowerCase();
			Set<String> set = tagToUsersMap.get(lowerCaseTag);
			if (set != null && set.contains(user)) {
				result.add(tag);
			}
		}
		Collections.sort(result);
		return result.toArray(new String[0]);
	}

	public String[] getTags() {
		String[] tags = Raptor.getInstance().getPreferences().getStringArray(
				PreferenceKeys.APP_USER_TAGS);
		Arrays.sort(tags);
		return tags;

	}

	public void addTag(String tag) {
		String[] oldTags = Raptor.getInstance().getPreferences()
				.getStringArray(PreferenceKeys.APP_USER_TAGS);
		boolean contains = false;
		for (int i = 0; i < oldTags.length; i++) {
			if (oldTags[i].equalsIgnoreCase(tag)) {
				contains = true;
				break;
			}
		}
		if (!contains) {
			String[] newTags = new String[oldTags.length + 1];
			System.arraycopy(oldTags, 0, newTags, 0, oldTags.length);
			newTags[oldTags.length] = tag;
			Arrays.sort(newTags);
			Raptor.getInstance().getPreferences().setValue(
					PreferenceKeys.APP_USER_TAGS, newTags);
			Raptor.getInstance().getPreferences().save();
		}
	}

	public String[] getUsersInTag(String tag) {
		tag = tag.toLowerCase();
		Set<String> tagList = tagToUsersMap.get(tag);
		if (tagList == null) {
			return new String[0];
		} else {
			String[] result = tagList.toArray(new String[0]);
			Arrays.sort(result);
			return result;
		}
	}

	public boolean clearTag(String tag, String user) {
		boolean result = false;
		tag = tag.toLowerCase();
		user = user.toLowerCase();
		Set<String> tagList = tagToUsersMap.get(tag);
		if (tagList != null) {
			result = tagList.remove(user);
			result = true;
		}

		if (result) {
			saveFile();
		}

		return result;
	}

	public void clear() {
		for (String key : tagToUsersMap.keySet()) {
			tagToUsersMap.remove(key);
		}
		saveFile();
	}

	public void clear(String tag) {
		tagToUsersMap.remove(tag.toLowerCase());
		saveFile();
	}

	public boolean clearTags(String user) {
		boolean result = false;
		user = user.toLowerCase();
		for (Set<String> set : tagToUsersMap.values()) {
			boolean removed = set.remove(user);
			if (removed) {
				result = true;
			}
		}
		if (result) {
			saveFile();
		}
		return result;
	}

	public void addUser(String tag, String user) {
		String lowerCaseTag = tag.toLowerCase();
		user = user.toLowerCase();
		Set<String> tagList = tagToUsersMap.get(lowerCaseTag);
		if (tagList == null) {
			addTag(tag);
			tagToUsersMap.put(lowerCaseTag, tagList = new TreeSet<String>());
		}
		if (!tagList.contains(user)) {
			tagList.add(user);
		}
		saveFile();
	}
}
