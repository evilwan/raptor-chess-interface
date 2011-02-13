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
package raptor.script;

import java.util.Comparator;
import java.util.regex.Pattern;

import raptor.Raptor;
import raptor.chat.ChatType;
import raptor.util.RaptorLogger;
import bsh.Interpreter;

/**
 * The chat script class. Currently uses BeanShell to execute scripts.
 */
public class ChatEventScript implements
		Comparable<ChatEventScript> {
	public static class ChatScriptNameComparator implements
			Comparator<ChatEventScript> {
		public int compare(ChatEventScript script1,
				ChatEventScript script2) {
			return script1.getName().compareTo(script2.getName());
		}
	}

	@SuppressWarnings("unused")
	private static final RaptorLogger LOG = RaptorLogger.getLog(ChatEventScript.class);

	protected ChatType chatType;
	protected String name = "";
	protected String description = "";
	protected String script = "";
	protected boolean isActive = false;
	protected boolean isSystemScript = true;
	protected ScriptConnectorType connectorType = ScriptConnectorType.ICS;
	protected Pattern pattern = null;

	public int compareTo(ChatEventScript arg0) {
		return name.compareTo(arg0.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ChatEventScript))
			return false;
		
		return name.equalsIgnoreCase(((ChatEventScript) obj).getName());
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
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
			interpeter.eval("import raptor.chat.*;\n" + getScript());
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error executing script " + getName(),
					t);
		}
	}

	public ScriptConnectorType getConnectorType() {
		return connectorType;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public ChatType getChatType() {
		return chatType;
	}

	public String getScript() {
		return script;
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean isSystemScript() {
		return isSystemScript;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setConnectorType(ScriptConnectorType connectorType) {
		this.connectorType = connectorType;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setChatType(ChatType chatType) {
		this.chatType = chatType;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setSystemScript(boolean isSystemScript) {
		this.isSystemScript = isSystemScript;
	}
}
