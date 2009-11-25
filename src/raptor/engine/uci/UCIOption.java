package raptor.engine.uci;

import org.apache.commons.lang.StringUtils;

public abstract class UCIOption {
	protected String name;
	protected String value;
	protected String defaultValue;

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value == null ? defaultValue : value;
	}

	public boolean isDefaultValue() {
		return StringUtils.equals(getDefaultValue(), getValue());
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return name + " default:" + defaultValue + " value:" + value;
	}
}
