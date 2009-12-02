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

import org.apache.commons.lang.StringUtils;

import raptor.service.AliasService;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class AliasHelpAlias extends RaptorAlias {
	public AliasHelpAlias() {
		super(
				"aliashelp",
				"Lists all alias names or prints the description and usage for a "
						+ "particular alias",
				"aliashelp OR aliashelp aliasName. Examples: 'aliasHelp' "
						+ "prints a list of all the aliases,'aliashelp aliasName' Prints "
						+ "the description and usage of an alias named aliasName.");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("aliashelp")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			String aliasName = null;
			if (tok.hasMoreTokens()) {
				aliasName = tok.getWhatsLeft();
			}

			if (StringUtils.isBlank(aliasName)) {
				StringBuilder aliasHelp = new StringBuilder(2000);
				aliasHelp
						.append("Raptor aliases: (Type \"aliashelp aliasName\" for a description and usage.):\n");
				RaptorAlias[] aliases = AliasService.getInstance().getAliases();
				for (RaptorAlias alias : aliases) {
					if (!alias.isHidden) {
						aliasHelp.append("\t" + alias.getName() + "\n");
					}
				}
				return new RaptorAliasResult(null, aliasHelp.toString());
			} else {
				RaptorAlias alias = AliasService.getInstance().getAlias(
						aliasName);
				if (alias == null) {
					return new RaptorAliasResult(null, "Alias '" + aliasName
							+ " 'not found.");
				} else {
					StringBuilder aliasHelp = new StringBuilder(2000);
					aliasHelp.append("Alias '" + aliasName + "':\n");
					aliasHelp.append("\tDescription: " + alias.getDescription()
							+ "\n");
					aliasHelp.append("\n");
					aliasHelp.append("\tUsage: " + alias.getUsage() + "\n");
					return new RaptorAliasResult(null, aliasHelp.toString());
				}
			}
		} else {
			return null;
		}
	}
}