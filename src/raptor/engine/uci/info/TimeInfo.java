package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class TimeInfo extends UCIInfo {
	protected int timeMillis;

	public int getTimeMillis() {
		return timeMillis;
	}

	public void setTimeMillis(int timeMillis) {
		this.timeMillis = timeMillis;
	}

}
