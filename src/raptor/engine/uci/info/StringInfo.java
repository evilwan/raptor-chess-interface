package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class StringInfo extends UCIInfo {
	protected String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
