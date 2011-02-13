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

import raptor.Raptor;
import raptor.script.ScriptContext;
import bsh.Interpreter;

/**
 * A RaptorAction executed from a script.
 */
public class ScriptedAction extends AbstractRaptorAction {
	protected String script;

	public ScriptedAction() {
	}

	/**
	 * Returns the script.
	 * 
	 * @return
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Executes the script.
	 */
	public void run() {
		ScriptContext context = null;
		if (getChatConsoleControllerSource() != null) {
			context = getChatConsoleControllerSource().getConnector()
					.getScriptContext();
		} else if (context == null && getChessBoardControllerSource() != null
				&& getChessBoardControllerSource().getConnector() != null) {
			context = getChessBoardControllerSource().getConnector()
					.getScriptContext();
		} else if (context == null && getConnectorSource() != null) {
			context = getConnectorSource().getScriptContext();
		}

		if (context == null) {
			Raptor
					.getInstance()
					.onError(
							"Could not executed "
									+ getName()
									+ " because a ScriptContext could not be created from a null connector.");
		} else {
			try {
				Interpreter interpeter = new Interpreter();
				interpeter.set("context", context);
				interpeter.eval(getScript());
			} catch (Throwable t) {
				Raptor.getInstance().onError(
						"Error executing script " + getName(), t);
			}
		}
	}

	/**
	 * Sets the script.
	 * 
	 * @param script
	 */
	public void setScript(String script) {
		this.script = script;
	}
}