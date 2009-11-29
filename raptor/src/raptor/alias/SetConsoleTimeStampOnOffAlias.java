package raptor.alias;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsoleController;

public class SetConsoleTimeStampOnOffAlias extends RaptorAlias {
	public SetConsoleTimeStampOnOffAlias() {
		super(
				"timestamp",
				"Turns console time stamping on or off for all Raptor console tabs.",
				"set timestamp [on | off | 1 | 0]. Example: set timestamp on");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("set timestamp ")) {
			String whatsLeft = command.substring(14).trim();
			if (whatsLeft.equals("on") || whatsLeft.equals("1")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.CHAT_TIMESTAMP_CONSOLE, true);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Console timestamping on");
			} else if (whatsLeft.equals("off") || whatsLeft.equals("0")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.CHAT_TIMESTAMP_CONSOLE, false);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Console timestamping off");
			}
		}
		return null;
	}

}
