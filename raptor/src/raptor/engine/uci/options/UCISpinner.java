package raptor.engine.uci.options;

import raptor.engine.uci.UCIOption;

public class UCISpinner extends UCIOption {
	protected int minimum;
	protected int maximum;

	public int getMaximum() {
		return maximum;
	}

	public int getMinimum() {
		return minimum;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}
}
