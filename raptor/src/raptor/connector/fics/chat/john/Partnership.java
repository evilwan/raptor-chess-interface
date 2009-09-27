package raptor.connector.fics.chat.john;

import java.util.List;

public class Partnership {
	private Bugger[] buggers = new Bugger[2];

	
	public void setBuggers(Bugger[] buggers) {
		this.buggers = buggers;
	}

	public Bugger[] getBuggers() {
		return buggers;
	}
	
	public List<Bugger> getBuggersAsList() {
		return java.util.Arrays.asList(getBuggers());
	}
	
	
}
