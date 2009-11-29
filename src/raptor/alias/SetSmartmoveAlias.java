package raptor.alias;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsoleController;

public class SetSmartmoveAlias extends RaptorAlias {

	public SetSmartmoveAlias() {
		super("smartmove", "Turns smartmove on or off.",
				"set smartmove [on | off]. "
						+ "Example: 'set smartmove on' will turn on smartmove.");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("set smartmove ")) {
			String whatsLeft = command.substring(14).trim();
			if (whatsLeft.equals("on") || whatsLeft.equals("1")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.APP_SOUND_ENABLED, true);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Smartmove on.");
			} else if (whatsLeft.equals("off") || whatsLeft.equals("0")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.APP_SOUND_ENABLED, false);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Smartmove off.");
			}
		}
		return null;
	}

}
