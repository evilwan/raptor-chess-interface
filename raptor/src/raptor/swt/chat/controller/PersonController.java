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
package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.Connector;
import raptor.swt.chat.ChatConsoleController;

public class PersonController extends ChatConsoleController {

	protected String person;

	public PersonController(Connector connector, String person) {
		super(connector);
		this.person = person;
	}

	@Override
	public String getName() {
		return person;
	}

	public String getPerson() {
		return person;
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				getConnector().getShortName() + "-" + PERSON_TAB_QUADRANT);

	}

	@Override
	public String getPrependText(boolean checkButton) {
		if (isIgnoringActions()) {
			return "";
		}

		if (checkButton
				&& isToolItemSelected(ToolBarItemKey.PREPEND_TEXT_BUTTON)) {
			return connector.getPersonTabPrefix(person);
		} else if (!checkButton) {
			return connector.getPersonTabPrefix(person);
		} else {
			return "";
		}
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent event) {
		return isDirectTellFromPerson(event)
				|| isOutboundTellPertainingToPerson(event);
	}

	@Override
	public boolean isAwayable() {
		return false;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isPrependable() {
		return true;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

	protected boolean isDirectTellFromPerson(ChatEvent event) {
		return StringUtils.equalsIgnoreCase(event.getSource(), person)
				&& (event.getType() == ChatType.TELL
						|| event.getType() == ChatType.PARTNER_TELL || event
						.getType() == ChatType.TOLD);
	}

	protected boolean isOutboundTellPertainingToPerson(ChatEvent event) {
		return StringUtils.containsIgnoreCase(event.getMessage(), person)
				&& event.getType() == ChatType.OUTBOUND;
	}

}
