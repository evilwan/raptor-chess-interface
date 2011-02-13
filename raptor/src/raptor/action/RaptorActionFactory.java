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
package raptor.action;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.action.RaptorAction.Category;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.pref.PreferenceKeys;
import raptor.util.RaptorStringUtils;

/**
 * Contains methods to create and load RaptorActions.
 */
public class RaptorActionFactory {

	/**
	 * Creates a Separator action. This is the only method that should be used
	 * to create separators.
	 */
	public static SeparatorAction createSeparator() {
		int sequenceValue = Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.ACTION_SEPARATOR_SEQUENCE);
		SeparatorAction action = new SeparatorAction();
		Raptor.getInstance().getPreferences().setValue(
				PreferenceKeys.ACTION_SEPARATOR_SEQUENCE, sequenceValue + 1);
		Raptor.getInstance().getPreferences().save();
		action.setName("Separator " + sequenceValue);
		return action;
	}

	/**
	 * Loads the RaptorAction from the specified properties file.
	 * 
	 * @param properties
	 *            THe properties to load the RaptorAction from.
	 * @return The RaptorAction.
	 */
	public static RaptorAction load(Properties properties) {
		AbstractRaptorAction result = null;

		try {
			result = (AbstractRaptorAction) Class.forName(
					properties.getProperty("class")).newInstance();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

		try {
			result.setDescription(properties.getProperty("description"));
			result.setName(properties.getProperty("name"));
			result.setIcon(properties.getProperty("icon"));
			if (StringUtils.isBlank(result.getIcon())) {
				result.setIcon(null);
			}
			result.setCategory(Category.valueOf(properties
					.getProperty("category")));
			result.setKeyCode(Integer.parseInt(properties
					.getProperty("keyCode")));
			result.setModifierKey(Integer.parseInt(properties
					.getProperty("modifierKey")));

			if (result instanceof ScriptedAction) {
				((ScriptedAction) result).setScript(properties.getProperty(
						"script").trim());
			}

			if (StringUtils.isNotBlank(properties.getProperty("containers"))) {
				String[] containerNames = RaptorStringUtils
						.stringArrayFromString(properties
								.getProperty("containers"));

				for (String containerName : containerNames) {
					result.addContainer(RaptorActionContainer
							.valueOf(containerName), Integer
							.parseInt(properties.getProperty(containerName)));
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Error loading properties: "
					+ properties, t);
		}
		return result;
	}

	/**
	 * Saves the RaptorAction to the specified properties file.
	 * 
	 * @param action
	 *            The RaptorAction to save.
	 * @return Properties The properties the RaptorAction was serialized into.
	 */
	public static Properties save(RaptorAction action) {
		Properties properties = new Properties();
		properties.put("description", StringUtils.defaultString(action
				.getDescription()));
		properties.put("name", StringUtils.defaultString(action.getName()));
		properties.put("icon", StringUtils.defaultString(action.getIcon()));
		properties.put("keyCode", "" + action.getKeyCode());
		properties.put("modifierKey", "" + action.getModifierKey());
		properties.put("category", action.getCategory().toString());
		properties.put("class", action.getClass().getName());
		properties.put("containers", RaptorStringUtils.toDelimitedString(action
				.getContainers()));

		for (RaptorActionContainer container : action.getContainers()) {
			properties.put(container.toString(), ""
					+ action.getOrder(container));
		}

		if (action instanceof ScriptedAction) {
			properties.put("script", ((ScriptedAction) action).getScript()
					.trim());
		}
		return properties;
	}
}
