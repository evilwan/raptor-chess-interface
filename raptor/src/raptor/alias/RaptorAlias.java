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

import raptor.swt.chat.ChatConsoleController;

/**
 * <p>
 * Aliases are commands applied to text before its sent to a connector.
 * </p>
 * <p>
 * All aliases must end with Alias and they all must be in this package.
 * Reflection is used to load them all.
 * </p>
 */
public abstract class RaptorAlias implements Comparable<RaptorAlias> {
	protected String name;
	protected String description;
	protected String usage;
	protected boolean isHidden;

	public RaptorAlias(String name, String description, String usage) {
		this.name = name;
		this.description = description;
		this.usage = usage;
	}

	/**
	 * Returns null if the alias was'nt applied, otherwise returns a
	 * RaptorAliasResult.
	 * 
	 * @param controller
	 *            The chat console controller applying the alias.
	 * @param command
	 *            The command to apply the alias on.
	 * @return Null if the alias wan't applied, otherwise the results after
	 *         applying the alias.
	 */
	public abstract RaptorAliasResult apply(ChatConsoleController controller,
			String command);

	/**
	 * Compares by name.
	 */
	public int compareTo(RaptorAlias arg0) {
		return name.compareTo(arg0.getName());
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String getUsage() {
		return usage;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}
}
