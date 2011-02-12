/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.layout;

import raptor.Quadrant;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.layout.TopBottomOrientedLayout;
import raptor.util.RaptorStringUtils;

public class BughouseButtonsFarLeftLayout extends AbstractLayout {
	public BughouseButtonsFarLeftLayout() {
		super(null, L10n.getInstance().getString("bugLeftWh"));

		addCrossConnectorSetting(PreferenceKeys.BUG_BUTTONS_QUADRANT,
				Quadrant.IX.toString());

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
						Quadrant.II.toString(), Quadrant.III.toString(),
						Quadrant.IV.toString(), Quadrant.V.toString() }, ","));

		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD9_QUAD12345678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 10, 90 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD1_QUAD2345678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD2345_QUAD678_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 60, 40 }));
		preferenceAdjustments.put(
				PreferenceKeys.APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 25, 25, 25, 25 }));
		preferenceAdjustments.put(PreferenceKeys.APP_QUAD67_QUAD8_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 70, 30 }));
		preferenceAdjustments.put(PreferenceKeys.APP_QUAD6_QUAD7_SASH_WEIGHTS,
				RaptorStringUtils.toString(new int[] { 50, 50 }));

		preferenceAdjustments.put(PreferenceKeys.BOARD_LAYOUT,
				TopBottomOrientedLayout.class.getName());
	}
}
