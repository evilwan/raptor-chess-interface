package raptor.layout;

import raptor.Quadrant;
import raptor.pref.PreferenceKeys;
import raptor.util.RaptorStringUtils;

public class ChatOnRightSplitPaneChat extends AbstractLayout {
	public ChatOnRightSplitPaneChat() {
		super(null, "Console on right (Channels/Person Tabs Bottom)");

		addCrossConnectorSetting(PreferenceKeys.BUG_BUTTONS_QUADRANT,
				Quadrant.I.toString());

		addCrossConnectorSetting(PreferenceKeys.MAIN_TAB_QUADRANT, Quadrant.VI
				.toString());

		addCrossConnectorSetting(PreferenceKeys.CHANNEL_TAB_QUADRANT,
				Quadrant.VII.toString());
		addCrossConnectorSetting(PreferenceKeys.PERSON_TAB_QUADRANT,
				Quadrant.VII.toString());
		addCrossConnectorSetting(PreferenceKeys.REGEX_TAB_QUADRANT, Quadrant.VI
				.toString());
		addCrossConnectorSetting(PreferenceKeys.PARTNER_TELL_TAB_QUADRANT,
				Quadrant.VII.toString());
		addCrossConnectorSetting(PreferenceKeys.GAME_CHAT_TAB_QUADRANT,
				Quadrant.VII.toString());

		addCrossConnectorSetting(PreferenceKeys.GAMES_TAB_QUADRANT,
				Quadrant.VII.toString());
		addCrossConnectorSetting(PreferenceKeys.GAME_BOT_QUADRANT, Quadrant.VII
				.toString());
		addCrossConnectorSetting(PreferenceKeys.BUG_WHO_QUADRANT, Quadrant.VII
				.toString());
		addCrossConnectorSetting(PreferenceKeys.SEEK_TABLE_QUADRANT,
				Quadrant.VII.toString());

		preferenceAdjustments.put(PreferenceKeys.APP_PGN_RESULTS_QUADRANT,
				Quadrant.VII.toString());
		preferenceAdjustments.put(PreferenceKeys.APP_BROWSER_QUADRANT,
				Quadrant.IX.toString());

		preferenceAdjustments.put(PreferenceKeys.APP_CHESS_BOARD_QUADRANTS,
				RaptorStringUtils.toDelimitedString(new String[] { Quadrant.IX
						.toString() }, ","));

		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD9_QUAD12345678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 60, 40 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD1_QUAD2345678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 10, 90 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD2345_QUAD678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 25, 25, 25, 25 }));
		preferenceAdjustments.put(PreferenceKeys.APP_QUAD67_QUAD8_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 70, 30 }));
		preferenceAdjustments.put(PreferenceKeys.APP_QUAD6_QUAD7_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));
	}
}