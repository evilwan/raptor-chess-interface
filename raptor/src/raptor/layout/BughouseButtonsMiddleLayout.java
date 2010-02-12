package raptor.layout;

import raptor.Quadrant;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.layout.TopBottomOrientedLayout;
import raptor.util.RaptorStringUtils;

public class BughouseButtonsMiddleLayout extends AbstractLayout {
	public BughouseButtonsMiddleLayout() {
		super(null, "Buttons between boards");

		addCrossConnectorSetting(PreferenceKeys.BUG_BUTTONS_QUADRANT,
				Quadrant.III.toString());

		addCrossConnectorSetting(PreferenceKeys.MAIN_TAB_QUADRANT, Quadrant.VI
				.toString());

		addCrossConnectorSetting(PreferenceKeys.CHANNEL_TAB_QUADRANT,
				Quadrant.VI.toString());
		addCrossConnectorSetting(PreferenceKeys.PERSON_TAB_QUADRANT,
				Quadrant.VI.toString());
		addCrossConnectorSetting(PreferenceKeys.REGEX_TAB_QUADRANT, Quadrant.VI
				.toString());
		addCrossConnectorSetting(PreferenceKeys.PARTNER_TELL_TAB_QUADRANT,
				Quadrant.VI.toString());
		addCrossConnectorSetting(PreferenceKeys.GAME_CHAT_TAB_QUADRANT,
				Quadrant.VI.toString());

		addCrossConnectorSetting(PreferenceKeys.GAMES_TAB_QUADRANT,
				Quadrant.VIII.toString());
		addCrossConnectorSetting(PreferenceKeys.GAME_BOT_QUADRANT,
				Quadrant.VIII.toString());
		addCrossConnectorSetting(PreferenceKeys.BUG_WHO_QUADRANT, Quadrant.VIII
				.toString());
		addCrossConnectorSetting(PreferenceKeys.SEEK_TABLE_QUADRANT,
				Quadrant.VIII.toString());

		preferenceAdjustments.put(PreferenceKeys.APP_PGN_RESULTS_QUADRANT,
				Quadrant.II.toString());
		preferenceAdjustments.put(PreferenceKeys.APP_BROWSER_QUADRANT,
				Quadrant.II.toString());

		preferenceAdjustments.put(PreferenceKeys.APP_CHESS_BOARD_QUADRANTS,
				RaptorStringUtils.toDelimitedString(new String[] {
						Quadrant.II.toString(), Quadrant.IV.toString(),
						Quadrant.V.toString() }, ","));

		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD9_QUAD12345678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD1_QUAD2345678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD2345_QUAD678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 60, 40 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 30, 10, 30, 30 }));
		preferenceAdjustments.put(PreferenceKeys.APP_QUAD67_QUAD8_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 70, 30 }));
		preferenceAdjustments.put(PreferenceKeys.APP_QUAD6_QUAD7_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));

		preferenceAdjustments.put(PreferenceKeys.BOARD_LAYOUT,
				TopBottomOrientedLayout.class.getName());
	}
}