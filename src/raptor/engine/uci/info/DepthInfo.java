package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class DepthInfo extends UCIInfo {
	protected int searchDepthPlies;

	public int getSearchDepthPlies() {
		return searchDepthPlies;
	}

	public void setSearchDepthPlies(int searchDepthPlies) {
		this.searchDepthPlies = searchDepthPlies;
	}

}
