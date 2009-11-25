package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class CPULoadInfo extends UCIInfo {
	protected int cpuUsage;

	public int getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(int cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
}
