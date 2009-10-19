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
import java.util.List;

import raptor.chat.BugGame;
import raptor.chat.Bugger;
import raptor.chat.Partnership;
import raptor.connector.Connector;

public class BughouseService {
	public static interface BughouseServiceListener {
		public void availablePartnershipsChanged(Partnership[] newPartnerships);

		public void gamesInProgressChanged(BugGame[] newGamesInProgress);

		public void unpartneredBuggersChanged(Bugger[] newUnpartneredBuggers);
	}

	private BugGame[] gamesInProgress = new BugGame[0];
	private Partnership[] availablePartnerships = new Partnership[0];
	private Bugger[] unpartneredBuggers = new Bugger[0];
	private Connector connector;

	private List<BughouseServiceListener> listeners = new ArrayList<BughouseServiceListener>(
			10);

	public BughouseService(Connector connector) {
		this.connector = connector;
	}

	public void addBughouseServiceListener(BughouseServiceListener listener) {
		listeners.add(listener);
	}

	public Partnership[] getAvailablePartnerships() {
		return availablePartnerships;
	}

	public Connector getConnector() {
		return connector;
	}

	public BugGame[] getGamesInProgress() {
		return gamesInProgress;
	}

	public Bugger[] getUnpartneredBuggers() {
		return unpartneredBuggers;
	}

	public void refreshAvailablePartnerships() {
		connector.sendBugAvailableTeamsMessage();
	}

	public void refreshGamesInProgress() {
		connector.sendBugGamesMessage();
	}

	public void refreshUnpartneredBuggers() {
		connector.sendBugUnpartneredBuggersMessage();
	}

	public void removeBughouseServiceListener(BughouseServiceListener listener) {
		listeners.remove(listener);
	}

	public void setAvailablePartnerships(Partnership[] availablePartnerships) {
		this.availablePartnerships = availablePartnerships;
		fireAvaialblePartnershipsChanged();
	}

	public void setGamesInProgress(BugGame[] gamesInProgress) {
		this.gamesInProgress = gamesInProgress;
		fireGamesInProgressChanged();
	}

	public void setUnpartneredBuggers(Bugger[] unpartneredBuggers) {
		this.unpartneredBuggers = unpartneredBuggers;
		fireUnpartneredBuggersChanged();
	}

	protected void fireAvaialblePartnershipsChanged() {
		for (BughouseServiceListener listener : listeners) {
			listener.availablePartnershipsChanged(availablePartnerships);
		}
	}

	protected void fireGamesInProgressChanged() {
		for (BughouseServiceListener listener : listeners) {
			listener.gamesInProgressChanged(gamesInProgress);
		}
	}

	protected void fireUnpartneredBuggersChanged() {
		for (BughouseServiceListener listener : listeners) {
			listener.unpartneredBuggersChanged(unpartneredBuggers);
		}
	}

}
