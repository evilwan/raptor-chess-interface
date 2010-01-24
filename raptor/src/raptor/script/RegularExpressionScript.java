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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.util.RegExUtils;
import bsh.Interpreter;

/**
 * The chat script class. Currently uses BeanShell to execute scripts.
 */
public class RegularExpressionScript implements
		Comparable<RegularExpressionScript> {
	public static class ChatScriptNameComparator implements
			Comparator<RegularExpressionScript> {
		public int compare(RegularExpressionScript script1,
				RegularExpressionScript script2) {
			return script1.getName().compareTo(script2.getName());
		}
	}

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory
			.getLog(RegularExpressionScript.class);

	protected String regularExpression;
	protected String name = "";
	protected String description = "";
	protected String script = "";
	protected boolean isActive = false;
	protected boolean isSystemScript = true;
	protected ScriptConnectorType connectorType = ScriptConnectorType.ICS;
	protected Pattern pattern = null;

	public int compareTo(RegularExpressionScript arg0) {
		return name.compareTo(arg0.getName());
	}

	@Override
	public boolean equals(Object obj) {
		return name.equalsIgnoreCase(((RegularExpressionScript) obj).getName());
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

	public String getRegularExpression() {
		return regularExpression;
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

	public boolean matches(String text) {
		return RegExUtils.matches(pattern, text);
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

	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
		pattern = RegExUtils.getPattern(regularExpression);
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setSystemScript(boolean isSystemScript) {
		this.isSystemScript = isSystemScript;
	}
}
