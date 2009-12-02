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
package raptor.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.alias.RaptorAlias;
import raptor.alias.RaptorAliasResult;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.ReflectionUtils;

public class AliasService {
	private static final Log LOG = LogFactory.getLog(AliasService.class);

	private static final AliasService singletonInstance = new AliasService();
	List<RaptorAlias> aliases = new ArrayList<RaptorAlias>(20);

	public static AliasService getInstance() {
		return singletonInstance;
	}

	@SuppressWarnings("unchecked")
	private AliasService() {
		try {
			long startTime = System.currentTimeMillis();
			Class[] classes = ReflectionUtils.getClasses("raptor.alias");
			for (Class clazz : classes) {
				if (clazz.getName().endsWith("Alias")
						&& !clazz.getName().endsWith("RaptorAlias")) {
					aliases.add((RaptorAlias) clazz.newInstance());
				}
			}
			Collections.sort(aliases);
			LOG
					.info("AliasService initialized " + aliases.size()
							+ " aliases in "
							+ (System.currentTimeMillis() - startTime));
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error loading AliasService", t);
		}
	}

	public void dispose() {
		aliases.clear();
	}

	/**
	 * Returns an alias by name. Returns null if the alias is not found.
	 * 
	 * @param name
	 *            The alias name.
	 * @return The alias, or null if the alias could not be found.
	 */
	public RaptorAlias getAlias(String name) {
		RaptorAlias result = null;
		for (RaptorAlias alias : aliases) {
			if (alias.getName().equalsIgnoreCase(name)) {
				result = alias;
				break;
			}
		}
		return result;
	}

	public RaptorAlias[] getAliases() {
		return aliases.toArray(new RaptorAlias[0]);
	}

	public String getAliasHtml() {
		StringBuilder builder = new StringBuilder(5000);
		builder.append("<html>\n<body>\n");
		builder.append("<h1>Raptor Aliases</h1>\n");
		builder
				.append("<p>Aliases are special commands you can type into a console. "
						+ "Some aliases are preprocessed by Raptor and then sent along to the server."
						+ "Other aliases are Raptor commands which are processed by Raptor and not sent "
						+ "along to the server.</p>\n");

		builder.append("<UL>\n");
		builder.append("<LH>Aliases:</LH>\n");
		for (RaptorAlias alias : getAliases()) {
			if (!alias.isHidden()) {
				builder.append("<LI><a href=\"#" + alias.getName() + "\">"
						+ alias.getName() + "</a></LI>\n");
			}
		}
		builder.append("</UL>\n");
		builder.append("<br/>\n");

		for (RaptorAlias alias : getAliases()) {
			if (!alias.isHidden()) {
				builder.append("<a name=\"" + alias.getName() + "\"><h2>"
						+ alias.getName() + "</h2></a>\n");
				builder.append("<h3>Description:</h3>\n");
				builder.append("<p>" + alias.getDescription() + "</p>\n");
				builder.append("<h3>Usage:</h3>\n");
				builder.append("<p>" + alias.getUsage() + "</p>\n");
				builder.append("<br/>\n");
				builder.append("<br/>\n");
			}
		}
		builder.append("</body></html>\n");
		return builder.toString();
	}

	/**
	 * If null is returned no alias was applied to the command. If a non-null
	 * value was returned an alias was applied.
	 * 
	 * @param command
	 *            The command.
	 * @return The command after applying aliases, or null if on action should
	 *         be taken.
	 */
	public RaptorAliasResult processAlias(ChatConsoleController controller,
			String command) {
		RaptorAliasResult result = null;
		for (RaptorAlias alias : aliases) {
			result = alias.apply(controller, command);
			if (result != null) {
				break;
			}
		}
		return result;
	}
}
