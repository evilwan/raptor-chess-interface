package raptor.engine.uci.options;

import raptor.engine.uci.UCIOption;

public class UCICombo extends UCIOption {
	protected String[] options;

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}
}
