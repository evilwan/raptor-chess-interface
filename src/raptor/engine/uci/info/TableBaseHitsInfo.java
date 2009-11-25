package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class TableBaseHitsInfo extends UCIInfo {
	protected int numberOfHits;

	public int getNumberOfHits() {
		return numberOfHits;
	}

	public void setNumberOfHits(int numberOfHits) {
		this.numberOfHits = numberOfHits;
	}
}
