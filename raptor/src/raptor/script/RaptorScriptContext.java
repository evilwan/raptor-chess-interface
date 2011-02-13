/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.Connector;
import raptor.connector.MessageCallback;
import raptor.service.SoundService;
import raptor.swt.chat.ChatUtils;
import raptor.util.BrowserUtils;

public class RaptorScriptContext implements ScriptContext {

	protected Connector connector;

	public RaptorScriptContext(Connector connector) {
		this.connector = connector;
	}

	public void alert(String message) {
		Raptor.getInstance().alert(message);
	}

	public void appendToFile(String fileName, String message) {
		FileWriter writer = null;
		try {
			File file = new File(fileName);
			writer = new FileWriter(file, true);
			writer.append(message);
			writer.flush();

		} catch (Throwable t) {
			Raptor.getInstance().onError("Error writing to file: " + fileName,
					t);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	public long getPingMillis() {
		return connector.getPingTime();
	}

	public String getUserFollowing() {
		Raptor.getInstance().alert("getUserFollowing is not yet implemented.");
		return "";
	}

	public int getUserIdleSeconds() {
		return (int) (System.currentTimeMillis() - connector.getLastSendTime()) / 1000;
	}

	public String getUserName() {
		return connector.getUserName();
	}

	public Object getValue(String key) {
		return connector.getScriptVariable(key);
	}

	public void launchProcess(String... commandAndArgs) {
		try {
			ProcessBuilder builder = new ProcessBuilder(commandAndArgs);
			builder.start();
		} catch (Throwable t) {
			connector.onError("Error launching process: "
					+ Arrays.toString(commandAndArgs), t);
		}
	}

	public void openChannelTab(String channel) {
		ChatUtils.openChannelTab(connector, channel, false);
	}

	public void openGameChatTab(String gameId) {
		ChatUtils.openGameChatTab(connector, gameId, false);
	}

	public void openPartnerTab() {
		ChatUtils.openPartnerTab(connector, false);
	}

	public void openPersonTab(String person) {
		ChatUtils.openPersonTab(connector, person, false);
	}

	public void openRegularExpressionTab(String regularExpression) {
		ChatUtils.openRegularExpressionTab(connector, regularExpression, false);
	}

	public void openUrl(String url) {
		BrowserUtils.openUrl(url);
	}

	public void play(String pathToSound) {
		SoundService.getInstance().play(pathToSound);
	}

	public void playBughouseSound(String soundName) {
		SoundService.getInstance().playBughouseSound(soundName);
	}

	public void playSound(String soundName) {
		SoundService.getInstance().playSound(soundName);
	}

	public String prompt(String message) {
		return Raptor.getInstance().promptForText(message);
	}

	public void registerMessageCallback(String regularExpression,
			MessageCallback callback) {
		connector.invokeOnNextMatch(regularExpression, callback);
	}

	public void send(String message) {
		connector.sendMessage(message);
	}

	public void sendHidden(String message) {
		if (message.contains("tell")) {
			connector.sendMessage(message, true, ChatType.TOLD);
		} else {
			connector.sendMessage(message, true);
		}
	}

	public void sendToConsole(String message) {
		connector.publishEvent(new ChatEvent(null, ChatType.INTERNAL, message));
	}

	public void speak(String message) {
		SoundService.getInstance().textToSpeech(message);
	}

	public void storeValue(String key, Object value) {
		connector.setScriptVariable(key, value);
	}

	public String urlEncode(String stringToEncode) {
		try {
			return URLEncoder.encode(stringToEncode, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
		}// Eat it wont happen.
		return stringToEncode;
	}

	public void writeToFile(String fileName, String message) {
		FileWriter writer = null;
		try {
			File file = new File(fileName);
			writer = new FileWriter(file, false);
			writer.append(message);
			writer.flush();

		} catch (Throwable t) {
			Raptor.getInstance().onError("Error writing to file: " + fileName,
					t);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable t) {
				}
			}
		}
	}
}