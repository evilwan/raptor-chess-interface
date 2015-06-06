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
package raptor.layout;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.RaptorConnectorWindowItem;
import raptor.RaptorWindowItem;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.util.RaptorLogger;

/**
 * A layout which can be loaded/stored to a properties file.
 */
public class CustomLayout extends AbstractLayout {
	private static final RaptorLogger LOG = RaptorLogger.getLog(CustomLayout.class);

	public static final String LAYOUT_NAME = "layout-name";

	public static CustomLayout loadFromProperties(String pathToFile) {
		FileInputStream fileIn = null;
		try {
			fileIn = new FileInputStream(pathToFile);
			Properties properties = new Properties();
			properties.load(fileIn);
			CustomLayout result = new CustomLayout();
			result.setName(properties.getProperty(LAYOUT_NAME));
			for (Enumeration<Object> e = properties.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				if (!StringUtils.equals(key, LAYOUT_NAME)) {
					String value = properties.getProperty(key);
					if (value.equals("null")) {
						value = null;
					}
					result.preferenceAdjustments.put(key, properties
							.getProperty(key));
				}
			}
			return result;
		} catch (Throwable t) {
			Raptor.getInstance().onError(
					"Error loading layout from file: " + pathToFile, t);
			return null;
		} finally {
			try {
				fileIn.close();
			} catch (Throwable t) {
			}
		}
	}

	public static Properties saveAsProperties(CustomLayout layout) {
		Properties properties = new Properties();
		properties.put(LAYOUT_NAME, layout.getName());
		for (String key : layout.preferenceAdjustments.keySet()) {
			String value = layout.preferenceAdjustments.get(key);
			if (value == null) {
				value = "null";
			}

			LOG.info("Writing: " + key + "=" + value);
			properties.put(key, value);
		}
		return properties;
	}

	public static CustomLayout createFromCurrentSettings() {
		CustomLayout result = new CustomLayout();

		// Try and determine the current connector.
		String prefix = "fics-";
		RaptorWindowItem[] windowItems = Raptor.getInstance().getWindow()
				.getWindowItems();
		for (RaptorWindowItem item : windowItems) {
			if (item instanceof RaptorConnectorWindowItem) {
				RaptorConnectorWindowItem connectorItem = (RaptorConnectorWindowItem) item;
				if (connectorItem.getConnector() != null) {
					prefix = connectorItem.getConnector().getShortName() + "-";
					break;
				}
			}
		}

		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();
		
		//Set all the connector specific quadrant settings.
		result.addCrossConnectorSetting(PreferenceKeys.BUG_BUTTONS_QUADRANT,
				prefs.getString(prefix + PreferenceKeys.BUG_BUTTONS_QUADRANT));
		result.addCrossConnectorSetting(PreferenceKeys.MAIN_TAB_QUADRANT, prefs
				.getString(prefix + PreferenceKeys.MAIN_TAB_QUADRANT));
		result.addCrossConnectorSetting(PreferenceKeys.CHANNEL_TAB_QUADRANT,
				prefs.getString(prefix + PreferenceKeys.CHANNEL_TAB_QUADRANT));
		result.addCrossConnectorSetting(PreferenceKeys.PERSON_TAB_QUADRANT,
				prefs.getString(prefix + PreferenceKeys.PERSON_TAB_QUADRANT));
		result.addCrossConnectorSetting(PreferenceKeys.REGEX_TAB_QUADRANT,
				prefs.getString(prefix + PreferenceKeys.REGEX_TAB_QUADRANT));
		result.addCrossConnectorSetting(
				PreferenceKeys.PARTNER_TELL_TAB_QUADRANT, prefs
						.getString(prefix
								+ PreferenceKeys.PARTNER_TELL_TAB_QUADRANT));
		result
				.addCrossConnectorSetting(
						PreferenceKeys.GAME_CHAT_TAB_QUADRANT,
						prefs.getString(prefix
								+ PreferenceKeys.GAME_CHAT_TAB_QUADRANT));
		result.addCrossConnectorSetting(PreferenceKeys.GAMES_TAB_QUADRANT,
				prefs.getString(prefix + PreferenceKeys.GAMES_TAB_QUADRANT));
		result.addCrossConnectorSetting(PreferenceKeys.GAME_BOT_QUADRANT, prefs
				.getString(prefix + PreferenceKeys.GAME_BOT_QUADRANT));
		result.addCrossConnectorSetting(PreferenceKeys.BUG_WHO_QUADRANT, prefs
				.getString(prefix + PreferenceKeys.BUG_WHO_QUADRANT));
		result.addCrossConnectorSetting(PreferenceKeys.SEEK_TABLE_QUADRANT,
				prefs.getString(prefix + PreferenceKeys.SEEK_TABLE_QUADRANT));

		//Set all of the global settings.
		result.preferenceAdjustments.put(
				PreferenceKeys.APP_PGN_RESULTS_QUADRANT, prefs
						.getString(PreferenceKeys.APP_PGN_RESULTS_QUADRANT));

		result.preferenceAdjustments.put(PreferenceKeys.APP_BROWSER_QUADRANT,
				prefs.getString(PreferenceKeys.APP_BROWSER_QUADRANT));
		result.preferenceAdjustments.put(
				PreferenceKeys.APP_CHESS_BOARD_QUADRANTS, prefs
						.getString(PreferenceKeys.APP_CHESS_BOARD_QUADRANTS));

		result.preferenceAdjustments
				.put(
						PreferenceKeys.APP_QUAD9_QUAD12345678_SASH_WEIGHTS,
						prefs
								.getString(PreferenceKeys.APP_QUAD9_QUAD12345678_SASH_WEIGHTS));

		result.preferenceAdjustments
				.put(
						PreferenceKeys.APP_QUAD1_QUAD2345678_SASH_WEIGHTS,
						prefs
								.getString(PreferenceKeys.APP_QUAD1_QUAD2345678_SASH_WEIGHTS));

		result.preferenceAdjustments
				.put(
						PreferenceKeys.APP_QUAD2345_QUAD678_SASH_WEIGHTS,
						prefs
								.getString(PreferenceKeys.APP_QUAD2345_QUAD678_SASH_WEIGHTS));

		result.preferenceAdjustments
				.put(
						PreferenceKeys.APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS,
						prefs
								.getString(PreferenceKeys.APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS));

		result.preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD67_QUAD8_SASH_WEIGHTS,
				prefs.getString(PreferenceKeys.APP_QUAD67_QUAD8_SASH_WEIGHTS));

		result.preferenceAdjustments
				.put(PreferenceKeys.APP_QUAD6_QUAD7_SASH_WEIGHTS, prefs
						.getString(PreferenceKeys.APP_QUAD6_QUAD7_SASH_WEIGHTS));

		result.preferenceAdjustments.put(PreferenceKeys.BOARD_LAYOUT, prefs
				.getString(PreferenceKeys.BOARD_LAYOUT));

		return result;
	}

	public CustomLayout() {
		super(null, null);
	}

}
