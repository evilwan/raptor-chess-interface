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
package raptor.connector.ics;

/**
 * The ICS (Internet Chess Servier) Context.
 */
public class IcsConnectorContext {

	/**
	 * DO NOT change the constants in here FicsConnector uses them. Instead
	 * subclass this and make changes.
	 */

	IcsParser parser;

	public IcsConnectorContext(IcsParser parser) {
		this.parser = parser;
	}

	public String getDescription() {
		return "Free Internet Chess Server";
	}

	public String getEnterPrompt() {
		return "\":";
	}

	public String getLoggedInMessage() {
		return "**** Starting FICS session as ";
	}

	public String getLoginErrorMessage() {
		return "\n*** ";
	}

	public String getLoginPrompt() {
		return "login: ";
	}

	public IcsParser getParser() {
		return parser;
	}

	public String getPasswordPrompt() {
		return "password:";
	}

	public String getPreferencePrefix() {
		return "fics-";
	}

	public String getPrompt() {
		return "fics%";
	}

	public String getRawPrompt() {
		return "\nfics% ";
	}

	public int getRawPromptLength() {
		return getRawPrompt().length();
	}

	public String getShortName() {
		return "fics";
	}

}
