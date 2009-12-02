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
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.BughousePartnerController;
import raptor.swt.chat.controller.ChannelController;
import raptor.swt.chat.controller.PersonController;
import raptor.swt.chat.controller.RegExController;
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
		if (!Raptor.getInstance().getWindow().containsChannelItem(connector,
				channel)) {
			ChatConsoleWindowItem windowItem = new ChatConsoleWindowItem(
					new ChannelController(connector, channel));
			Raptor.getInstance().getWindow().addRaptorWindowItem(windowItem,
					false);
			ChatUtils.appendPreviousChatsToController(windowItem.getConsole());
		}
	}

	public void openPartnerTab() {
		if (!Raptor.getInstance().getWindow()
				.containsPartnerTellItem(connector)) {
			ChatConsoleWindowItem windowItem = new ChatConsoleWindowItem(
					new BughousePartnerController(connector));
			Raptor.getInstance().getWindow().addRaptorWindowItem(windowItem,
					false);
			ChatUtils.appendPreviousChatsToController(windowItem.getConsole());
		}
	}

	public void openPersonTab(String person) {
		if (!Raptor.getInstance().getWindow().containsPersonalTellItem(
				connector, person)) {
			ChatConsoleWindowItem windowItem = new ChatConsoleWindowItem(
					new PersonController(connector, person));
			Raptor.getInstance().getWindow().addRaptorWindowItem(windowItem,
					false);
			ChatUtils.appendPreviousChatsToController(windowItem.getConsole());
		}
	}

	public void openRegExTab(String regularExpression) {
		if (!Raptor.getInstance().getWindow()
				.containsPartnerTellItem(connector)) {
			ChatConsoleWindowItem windowItem = new ChatConsoleWindowItem(
					new RegExController(connector, regularExpression));
			Raptor.getInstance().getWindow().addRaptorWindowItem(windowItem,
					false);
			ChatUtils.appendPreviousChatsToController(windowItem.getConsole());
		}
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