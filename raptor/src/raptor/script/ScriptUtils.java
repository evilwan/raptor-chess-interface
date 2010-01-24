/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.script;

import java.util.Properties;

import raptor.script.ParameterScript.Type;

public class ScriptUtils {
	public static Properties serialize(ParameterScript script) {
		Properties properties = new Properties();
		properties.put("name", script.getName());
		properties.put("isActive", "" + script.isActive());
		properties.put("description", script.getDescription());
		properties.put("script", script.getScript());
		properties.put("type", script.getType().toString());
		properties.put("connectorType", script.getConnectorType().name());
		return properties;
	}

	public static Properties serialize(RegularExpressionScript script) {
		Properties properties = new Properties();
		properties.put("name", script.getName());
		properties.put("isActive", "" + script.isActive());
		properties.put("description", script.getDescription());
		properties.put("script", script.getScript());
		properties.put("regularExpression", script.getRegularExpression());
		properties.put("connectorType", script.getConnectorType().name());
		return properties;
	}

	public static ParameterScript unserializeParameterScript(
			Properties properties) {
		ParameterScript result = new ParameterScript();
		result.setName(properties.getProperty("name"));
		result.setActive(properties.getProperty("isActive").equals("true"));
		result.setDescription(properties.getProperty("description"));
		result.setScript(properties.getProperty("script"));
		result.setType(Type.valueOf(properties.getProperty("type")));
		result.setConnectorType(ScriptConnectorType.valueOf((String) properties
				.get("connectorType")));
		return result;
	}

	public static RegularExpressionScript unserializeRegularExpressionScript(
			Properties properties) {
		RegularExpressionScript result = new RegularExpressionScript();
		result.setName(properties.getProperty("name"));
		result.setActive(properties.getProperty("isActive").equals("true"));
		result.setDescription(properties.getProperty("description"));
		result.setScript(properties.getProperty("script"));
		result
				.setRegularExpression(properties
						.getProperty("regularExpression"));
		result.setConnectorType(ScriptConnectorType.valueOf((String) properties
				.get("connectorType")));
		return result;
	}
}
