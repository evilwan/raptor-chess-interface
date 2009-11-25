package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class SelectiveSearchDepthInfo extends UCIInfo {
	protected int depthInPlies;

	public int getDepthInPlies() {
		return depthInPlies;
	}

	public void setDepthInPlies(int depthInPlies) {
		this.depthInPlies = depthInPlies;
	}

}
