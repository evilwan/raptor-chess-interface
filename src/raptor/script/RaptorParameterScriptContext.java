package raptor.script;

import java.util.Map;

import raptor.connector.Connector;

public class RaptorParameterScriptContext extends RaptorScriptContext implements
		ParameterScriptContext {
	protected Map<String, Object> parameterMap;

	public RaptorParameterScriptContext(Connector connector,
			Map<String, Object> parameterMap) {
		super(connector);
		this.parameterMap = parameterMap;
	}

	public boolean containsParameter(String parameterName) {
		return parameterMap.containsKey(parameterName);
	}

	public Object getParameter(String parameterName) {
		return parameterMap.get(parameterName);
	}
}