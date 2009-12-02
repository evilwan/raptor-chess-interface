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
package raptor.alias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import raptor.script.RegularExpressionScript;
import raptor.service.ScriptService;
import raptor.swt.chat.ChatConsoleController;

public class ListScriptsAlias extends RaptorAlias {
	public ListScriptsAlias() {
		super("=script", "Lists all of the regular expression scripts. ",
				"'=script'" + "Example: '=scripts'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.startsWith("=script")) {
			RegularExpressionScript[] scripts = ScriptService.getInstance()
					.getRegularExpressionScripts();
			StringBuilder text = new StringBuilder(2000);

			List<RegularExpressionScript> activeScripts = new ArrayList<RegularExpressionScript>(
					10);
			List<RegularExpressionScript> inactiveScripts = new ArrayList<RegularExpressionScript>(
					10);
			for (RegularExpressionScript script : scripts) {
				if (script.isActive()) {
					activeScripts.add(script);
				} else {
					inactiveScripts.add(script);
				}
			}
			Collections.sort(activeScripts);
			Collections.sort(inactiveScripts);

			text.append("Active Scripts:\n");
			for (RegularExpressionScript script : activeScripts) {
				text.append("\t" + script.getName() + "\n");
			}
			text.append("\n\n");
			text.append("Inactive Scripts:\n");
			for (RegularExpressionScript script : inactiveScripts) {
				text.append("\t" + script.getName() + "\n");
			}
			return new RaptorAliasResult("", text.toString());
		}
		return null;
	}
}
