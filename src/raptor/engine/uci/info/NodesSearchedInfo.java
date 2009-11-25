package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class NodesSearchedInfo extends UCIInfo {
	protected int nodesSearched;

	public int getNodesSearched() {
		return nodesSearched;
	}

	public void setNodesSearched(int nodesSearched) {
		this.nodesSearched = nodesSearched;
	}

}
