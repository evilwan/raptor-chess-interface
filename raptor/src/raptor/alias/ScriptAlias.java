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
package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.script.ChatEventScript;
import raptor.service.ScriptService;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

@Deprecated
public class ScriptAlias extends RaptorAlias {
	public ScriptAlias() {
		super(
				"script",
				"Creates a regular expression script on the command line and makes it active. ",
				"'script \"name\" \"description\" \"regularExpression\" script. "
						+ "Example: The following is a script which will message you when anyone sends you a direct tell.\n"
						+ "script \"messageMeDirectTells\" \"Messages me whenever someone sends me a direct tell\" "
						+ "\"(a-z\\(\\))* tells you: .*\" "
						+ "context.send(\"message \" + context.getUserName() + \" \" + context.getChatEvent().getMessage());");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWith(command, "script")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command,
					"\"", true);
			tok.nextToken();
			String scriptName = tok.nextToken();
			tok.nextToken();
			String scriptDescription = tok.nextToken();
			tok.nextToken();
			String regularExpression = tok.nextToken();
			String script = tok.getWhatsLeft();

			if (StringUtils.isBlank(scriptName)) {
				return new RaptorAliasResult(null, "name is required.\n"
						+ getUsage());
			} else if (StringUtils.isBlank(scriptDescription)) {
				return new RaptorAliasResult(null, "description is required.\n"
						+ getUsage());
			} else if (StringUtils.isBlank(regularExpression)) {
				return new RaptorAliasResult(null,
						"regularExpression is required.\n" + getUsage());
			} else if (StringUtils.isBlank(script)) {
				return new RaptorAliasResult(null, "script is required.\n"
						+ getUsage());
			} else {
				ChatEventScript chatEventScript = new ChatEventScript();
				chatEventScript.setName(name);
				chatEventScript.setDescription(scriptDescription);
				//chatEventScript.setRegularExpression(regularExpression);
				chatEventScript.setScript(script);
				chatEventScript.setActive(true);
				chatEventScript.setConnectorType(controller
						.getConnector().getScriptConnectorType());
				chatEventScript.setSystemScript(false);
				ScriptService.getInstance().save(chatEventScript);
				return new RaptorAliasResult(null, "Your new script "
						+ chatEventScript.getName() + " is now active.");
			}
		}
		return null;
	}
}
