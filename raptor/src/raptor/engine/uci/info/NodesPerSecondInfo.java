package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class NodesPerSecondInfo extends UCIInfo {
	protected int nodesPerSecond;

	public int getNodesPerSecond() {
		return nodesPerSecond;
	}

	public void setNodesPerSecond(int nodesPerSecond) {
		this.nodesPerSecond = nodesPerSecond;
	}
}
