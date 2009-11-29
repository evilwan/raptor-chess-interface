package raptor.alias;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsoleController;

public class SetPremoveModeAlias extends RaptorAlias {

	public SetPremoveModeAlias() {
		super(
				"premove",
				"Turns premove on or off.",
				"set premove [on | off | 1 | 0 | queued]. "
						+ "Example: 'set premove queued' will set premove to queued premove mode.");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("set premove ")) {
			String whatsLeft = command.substring(12).trim();
			if (whatsLeft.equals("on") || whatsLeft.equals("1")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_PREMOVE_ENABLED, true);
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED, false);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Non-Queued premove on.");
			} else if (whatsLeft.equals("off") || whatsLeft.equals("0")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_PREMOVE_ENABLED, false);
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED, false);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Premove off.");
			} else if (whatsLeft.equals("queued")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_PREMOVE_ENABLED, true);
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED, true);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Queued premove on.");
			}
		}
		return null;
	}

}
