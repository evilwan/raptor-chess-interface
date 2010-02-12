package raptor.layout;

import raptor.Quadrant;
import raptor.pref.PreferenceKeys;
import raptor.util.RaptorStringUtils;

public class ChatOnLeftLayout extends AbstractLayout {
	public ChatOnLeftLayout() {
		super(null, "Console on left");

		addCrossConnectorSetting(PreferenceKeys.BUG_BUTTONS_QUADRANT,
				Quadrant.II.toString());

		addCrossConnectorSetting(PreferenceKeys.MAIN_TAB_QUADRANT, Quadrant.IX
				.toString());

		addCrossConnectorSetting(PreferenceKeys.CHANNEL_TAB_QUADRANT,
				Quadrant.IX.toString());
		addCrossConnectorSetting(PreferenceKeys.PERSON_TAB_QUADRANT,
				Quadrant.IX.toString());
		addCrossConnectorSetting(PreferenceKeys.REGEX_TAB_QUADRANT, Quadrant.VI
				.toString());
		addCrossConnectorSetting(PreferenceKeys.PARTNER_TELL_TAB_QUADRANT,
				Quadrant.IX.toString());
		addCrossConnectorSetting(PreferenceKeys.GAME_CHAT_TAB_QUADRANT,
				Quadrant.IX.toString());

		addCrossConnectorSetting(PreferenceKeys.GAMES_TAB_QUADRANT,
				Quadrant.III.toString());
		addCrossConnectorSetting(PreferenceKeys.GAME_BOT_QUADRANT, Quadrant.III
				.toString());
		addCrossConnectorSetting(PreferenceKeys.BUG_WHO_QUADRANT, Quadrant.III
				.toString());
		addCrossConnectorSetting(PreferenceKeys.SEEK_TABLE_QUADRANT,
				Quadrant.III.toString());

		preferenceAdjustments.put(PreferenceKeys.APP_PGN_RESULTS_QUADRANT,
				Quadrant.III.toString());
		preferenceAdjustments.put(PreferenceKeys.APP_BROWSER_QUADRANT,
				Quadrant.III.toString());

		preferenceAdjustments.put(PreferenceKeys.APP_CHESS_BOARD_QUADRANTS,
				RaptorStringUtils.toDelimitedString(new String[] {
						Quadrant.III.toString(), Quadrant.IV.toString(),
						Quadrant.V.toString() }, ","));

		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD9_QUAD12345678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 40, 60 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD1_QUAD2345678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD2345_QUAD678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 10, 30, 30, 30 }));
		preferenceAdjustments.put(PreferenceKeys.APP_QUAD67_QUAD8_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 70, 30 }));
		preferenceAdjustments.put(PreferenceKeys.APP_QUAD6_QUAD7_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));
	}
}