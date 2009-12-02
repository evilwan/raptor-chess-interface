package raptor.script;

import java.util.Map;

import raptor.connector.Connector;

public class RaptorParameterScriptContext extends RaptorScriptContext implements
		ParameterScriptContext {
	protected Map<String, String> parameterMap;

	public RaptorParameterScriptContext(Connector connector,
			Map<String, String> parameterMap) {
		super(connector);
		this.parameterMap = parameterMap;
	}

	public boolean containsParameter(String parameterName) {
		return parameterMap.containsKey(parameterName);
	}

	public String getParameter(String parameterName) {
		return parameterMap.get(parameterName);
	}
}