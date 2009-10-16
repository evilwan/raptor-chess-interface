/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class GameScript implements Comparable<GameScript> {

	public enum GameScriptControllerType {
		Examine, Playing, Observing, Inactive
	}

	public static class GameScriptNameComparator implements
			Comparator<GameScript> {
		public int compare(GameScript script1, GameScript script2) {
			return script1.name.compareTo(script2.name);
		}
	};

	public enum GameScriptType {
		OneShot, GameStart, GameEnd, EveryMove
	};

	protected int order = Integer.MAX_VALUE;

	protected String name = "";

	protected String description = "";
	protected boolean isActive = false;
	protected String script;
	protected GameScriptType gameScriptType;
	protected GameScriptControllerType gameScriptControllerType;
	protected ScriptConnectorType scriptConnectorType;
	/**
	 * The ScriptService managed this variable. It does not need to be
	 * serialized.
	 */
	protected transient boolean isSystemScript;

	public static GameScript load(String file) throws IOException {
		GameScript result = new GameScript();
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		result.order = NumberUtils.toInt(properties.getProperty("order"),
				Integer.MAX_VALUE);
		result.isActive = BooleanUtils.toBoolean(properties
				.getProperty("isActive"));
		result.name = StringUtils.defaultIfEmpty(
				properties.getProperty("name"), "EmptyName");
		result.description = StringUtils.defaultIfEmpty(properties
				.getProperty("description"), "EmptyDescription");
		result.script = StringUtils.defaultIfEmpty(properties
				.getProperty("script"), "EmptyScript");
		result.gameScriptType = GameScriptType.valueOf(StringUtils
				.defaultIfEmpty(properties.getProperty("gameScriptType"),
						"Playing"));
		result.gameScriptControllerType = GameScriptControllerType
				.valueOf(StringUtils.defaultIfEmpty(properties
						.getProperty("gameScriptControllerType"), "OneShot"));
		result.scriptConnectorType = ScriptConnectorType.valueOf(StringUtils
				.defaultIfEmpty(properties.getProperty("scriptConnectorType"),
						"ICS"));
		return result;
	}

	public static void store(GameScript script, String file) throws IOException {
		Properties properties = new Properties();
		properties.put("order", "" + script.order);
		properties.put("name", script.name);
		properties.put("isActive", "" + script.isActive);
		properties.put("description", script.description);
		properties.put("script", script.script);
		properties.put("gameScriptType", script.gameScriptType.name());
		properties.put("gameScriptControllerType",
				script.gameScriptControllerType.name());
		properties
				.put("scriptConnectorType", script.scriptConnectorType.name());
		properties.store(new FileOutputStream(file), "Saved on " + new Date());
	}

	public GameScript() {
	}

	public int compareTo(GameScript script) {
		return order == script.order ? 0 : order < script.order ? -1 : 1;
	}

	public void execute(GameScriptContext context) {
	}

	public String getDescription() {
		return description;
	}

	public GameScriptControllerType getGameScriptControllerType() {
		return gameScriptControllerType;
	}

	public GameScriptType getGameScriptType() {
		return gameScriptType;
	}

	public String getName() {
		return name;
	}

	public int getOrder() {
		return order;
	}

	public String getScript() {
		return script;
	}

	public ScriptConnectorType getScriptConnectorType() {
		return scriptConnectorType;
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean isSystemScript() {
		return isSystemScript;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setGameScriptControllerType(
			GameScriptControllerType gameScriptControllerType) {
		this.gameScriptControllerType = gameScriptControllerType;
	}

	public void setGameScriptType(GameScriptType gameScriptType) {
		this.gameScriptType = gameScriptType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setScriptConnectorType(ScriptConnectorType scriptConnectorType) {
		this.scriptConnectorType = scriptConnectorType;
	}

	public void setSystemScript(boolean isSystemScript) {
		this.isSystemScript = isSystemScript;
	}
}
