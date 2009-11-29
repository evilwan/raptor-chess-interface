package raptor.alias;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsoleController;

public class SetSoundOnOfAlias extends RaptorAlias {

	public SetSoundOnOfAlias() {
		super("sound", "Turns all sound in Raptor either on or off.",
				"set sound [on | off | 1 | 0]. Example: 'set sound off'");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("set sound ")) {
			String whatsLeft = command.substring(10).trim();
			if (whatsLeft.equals("on") || whatsLeft.equals("1")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.APP_SOUND_ENABLED, true);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Sound on.");
			} else if (whatsLeft.equals("off") || whatsLeft.equals("0")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.APP_SOUND_ENABLED, false);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Sound off.");
			}
		}
		return null;
	}

}
