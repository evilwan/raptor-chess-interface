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
package raptor.script;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import bsh.Interpreter;

/**
 * The chat script class. Currently uses BeanShell to execute scripts.
 */
public class ChatScript implements Comparable<ChatScript> {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(ChatScript.class);

	public static class ChatScriptNameComparator implements
			Comparator<ChatScript> {
		public int compare(ChatScript script1, ChatScript script2) {
			return script1.getName().compareTo(script2.getName());
		}
	}

	public static enum ChatScriptType {
		/**
		 * Used for right clicks or selected text right clicks in the chat
		 * consoles. Scripts that are just executed on demand.
		 */
		RightClickOneShot,
		/**
		 * Used for toolbar scripts. Scripts that are just executed on demand.
		 */
		ToolbarOneShot,
		/**
		 * Used for bughouse botton scripts. Scripts that are just executed on
		 * demand.
		 */
		BugButtonsOneShot,
		/**
		 * Used for ChatScripts which are processed on each channel tell
		 * received.
		 */
		onChannelTellMessages,
		/**
		 * Used for ChatScripts which are processed on each partner tell, ptell
		 * on ics servers.
		 */
		OnPartnerTellMessages,
		/**
		 * Used for ChatScripts which are processed on each person tell
		 * received.
		 */
		OnPersonTellMessages
	};

	protected String name = "";

	protected String description = "";

	protected String script = "";
	protected boolean isActive = false;
	protected ChatScriptType chatScriptType = ChatScriptType.ToolbarOneShot;
	protected ScriptConnectorType scriptConnectorType = ScriptConnectorType.ICS;
	protected int order = Integer.MAX_VALUE;
	/**
	 * The ScriptService managed this variable. It does not need to be
	 * serialized.
	 */
	protected transient boolean isSystemScript;

	/**
	 * Loads a ChatScript from a file.
	 */
	public static ChatScript load(String file) throws IOException {
		ChatScript result = new ChatScript();
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		result.order = NumberUtils.toInt(properties.getProperty("order"),
				Integer.MAX_VALUE);
		result.isActive = BooleanUtils.toBoolean(properties
				.getProperty("isActive"));
		result.name = StringUtils.defaultIfEmpty(
				properties.getProperty("name"), "EmptyName");
		result.description = StringUtils.defaultIfEmpty(properties
				.getProperty("description"), "EmptyDescription");
		result.script = StringUtils.defaultIfEmpty(properties
				.getProperty("script"), "EmptyScript");
		result.chatScriptType = ChatScriptType.valueOf(StringUtils
				.defaultIfEmpty(properties.getProperty("chatScriptType"),
						"OneShot"));
		result.scriptConnectorType = ScriptConnectorType.valueOf(StringUtils
				.defaultIfEmpty(properties
						.getProperty("chatScriptConnectorType"), "ICS"));
		return result;
	}

	/**
	 * Saves a ChatScript to a file.
	 */
	public static void store(ChatScript script, String file) throws IOException {
		Properties properties = new Properties();
		properties.put("order", "" + script.order);
		properties.put("name", script.name);
		properties.put("isActive", "" + script.isActive);
		properties.put("description", script.description);
		properties.put("script", script.script);
		properties.put("chatScriptType", script.chatScriptType.name());
		properties
				.put("scriptConnectorType", script.scriptConnectorType.name());
		properties.store(new FileOutputStream(file), "Saved on " + new Date());
	}

	public int compareTo(ChatScript script) {
		return order == script.order ? 0 : order < script.order ? -1 : 1;
	}

	/**
	 * Executes the script with the specified context.
	 * 
	 * @param context
	 */
	public void execute(ChatScriptContext context) {
		try {
			Interpreter interpeter = new Interpreter();
			interpeter.set("context", context);
			interpeter.eval(getScript());
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error executing script " + getName(),
					t);
		}
	}

	public ChatScriptType getChatScriptType() {
		return chatScriptType;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	/**
	 * Used for sorting scripts in toolbars. Not used for anything else.
	 */
	public int getOrder() {
		return order;
	}

	public String getScript() {
		return script;
	}

	public ScriptConnectorType getScriptConnectorType() {
		return scriptConnectorType;
	}

	/**
	 * Returns true if the current script is enabled.
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Returns true if this script is a system script. System scripts can never
	 * be deleted. They are the scripts contained in the resources/scripts/chat
	 * folder.
	 */
	public boolean isSystemScript() {
		return isSystemScript;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setChatScriptType(ChatScriptType chatScriptType) {
		this.chatScriptType = chatScriptType;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setScriptConnectorType(ScriptConnectorType scriptConnectorType) {
		this.scriptConnectorType = scriptConnectorType;
	}

	public void setSystemScript(boolean isSystemScript) {
		this.isSystemScript = isSystemScript;
	}
}
