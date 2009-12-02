package raptor.script;

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

	public String getValue(String key) {
		return connector.getScriptVariable(key);
	}

	public void launchProcess(String... commandAndArgs) {
		try {
			Runtime.getRuntime().exec(commandAndArgs);
		} catch (Throwable t) {
			connector.onError("Error launching process: "
					+ Arrays.toString(commandAndArgs), t);
		}
	}

	public void openChannelTab(String channel) {
		ChatUtils.openChannelTab(connector, channel);
	}

	public void openPartnerTab() {
		ChatUtils.openPartnerTab(connector);
	}

	public void openPersonTab(String person) {
		ChatUtils.openPersonTab(connector, person);
	}

	public void openRegularExpressionTab(String regularExpression) {
		ChatUtils.openRegularExpressionTab(connector, regularExpression);
	}

	public void openUrl(String url) {
		BrowserUtils.openUrl(url);
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

	public void storeValue(String key, String value) {
		connector.setScriptVariable(key, value);
	}

	public String urlEncode(String stringToEncode) {
		try {
			return URLEncoder.encode(stringToEncode, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
		}// Eat it wont happen.
		return stringToEncode;
	}
}