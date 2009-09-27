package raptor.service;

import raptor.connector.fics.chat.john.Bugger;
import raptor.connector.fics.chat.john.Partnership;

public class BughouseService {
	private Bugger[] unpartneredBuggers;
	private Partnership[] availablePartnerships;

	/*public void buggersAvailableUpdated() {
		
	}
	
	public void bugGamesPlayingUpdated() {
		
	}

	public void bugTeamsAvailableUpdated() {
		
	}*/

	/**
	 * This method should only be called by BugWhoUParser.parse().
	 */
	public void setUnpartneredBuggers(Bugger[] unpartneredBuggers) {
		this.unpartneredBuggers = unpartneredBuggers;
	}

	public Bugger[] getUnpartneredBuggers() {
		return unpartneredBuggers;
	}

	/**
	 * This method should only be called by BugWhoPParser.parse().
	 */
	public void setAvailablePartnerships(Partnership[] availablePartnerships) {
		this.availablePartnerships = availablePartnerships;
	}

	public Partnership[] getAvailablePartnerships() {
		return availablePartnerships;
	}

}
