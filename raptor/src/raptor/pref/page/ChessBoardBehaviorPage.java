/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.UserMoveInputMode;

public class ChessBoardBehaviorPage extends FieldEditorPreferencePage {
	
	protected static L10n local = L10n.getInstance();

	public static final String[][] USER_MOVE_INPUT_MODE_ARRAY = {
			{ local.getString("dragNDrop"), UserMoveInputMode.DragAndDrop.toString() },
			{ local.getString("clClMove"), UserMoveInputMode.ClickClickMove.toString() } };

	public static final String[][] SHOW_SECONDS_OPTIONS = {
			{ local.getString("always"), "" + Integer.MAX_VALUE },
			{ local.getString("chesBBehP1"), "" + (60 * 60 * 1000 + 1) },
			{ local.getString("chesBBehP2"), "" + (30 * 60 * 1000 + 1) },
			{ local.getString("chesBBehP3"), "" + (15 * 60 * 1000 + 1) },
			{ local.getString("chesBBehP4"), "" + (10 * 60 * 1000 + 1) } };

	public static final String[][] SHOW_TENTHS_OPTIONS = {
			{ local.getString("never"), "" + Integer.MIN_VALUE },
			{ local.getString("chesBBehP5"), "" + (10 * 1000 + 1) },
			{ local.getString("chesBBehP6"), "" + (60 * 1000 + 1) },
			{ local.getString("chesBBehP7"), "" + (3 * 60 * 1000 + 1) },
			{ local.getString("chesBBehP8"), "" + (5 * 60 * 1000 + 1) },
			{ local.getString("chesBBehP9"), "" + (10 * 60 * 1000 + 1) },
			{ local.getString("always"), "" + Integer.MAX_VALUE } };

	public ChessBoardBehaviorPage() {
		// Use the "flat" layout
		super(GRID);
		setTitle(local.getString("behavior"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new ComboFieldEditor(
				PreferenceKeys.BOARD_USER_MOVE_INPUT_MODE, local.getString("chesBBehP10"),
				USER_MOVE_INPUT_MODE_ARRAY, getFieldEditorParent()));

		addField(new ComboFieldEditor(
				PreferenceKeys.BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN,
				local.getString("chesBBehP11"), SHOW_TENTHS_OPTIONS,
				getFieldEditorParent()));

		addField(new ComboFieldEditor(
				PreferenceKeys.BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN,
				local.getString("chesBBehP12"), SHOW_SECONDS_OPTIONS,
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_TRAVERSE_WITH_MOUSE_WHEEL,
				local.getString("chesBBehP13"), getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_ALLOW_MOUSE_WHEEL_NAVIGATION_WHEEL_PLAYING,
				local.getString("chesBBehP14"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_ANNOUNCE_CHECK_WHEN_I_CHECK_OPPONENT,
				local.getString("chesBBehP15"), getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_ANNOUNCE_CHECK_WHEN_OPPONENT_CHECKS_ME,
				local.getString("chesBBehP16"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IGNORE_OBSERVED_GAMES_IF_PLAYING,
				local.getString("chesBBehP17"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_TAKEOVER_INACTIVE_GAMES,
				local.getString("chesBBehP18"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_USING_CROSSHAIRS_CURSOR,
				local.getString("chesBBehP19"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_PLAY_ABORT_REQUEST_SOUND,
				local.getString("chesBBehP20"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_PLAY_CHALLENGE_SOUND,
				local.getString("chesBBehP21"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_PLAY_DRAW_OFFER_SOUND,
				local.getString("chesBBehP22"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING,
				local.getString("chesBBehP23"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS,
				local.getString("chesBBehP24"), getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.BOARD_PREMOVE_ENABLED,
				local.getString("chesBBehP25"), getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED,
				local.getString("chesBBehP26"), getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_SHOW_COORDINATES, local.getString("chesBBehP27"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_SHOWING_PIECE_JAIL, local.getString("chesBBehP28"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_SHOW_PLAYING_GAME_STATS_ON_GAME_END,
				local.getString("chesBBehP29"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_SPEAK_RESULTS,
				local.getString("chesBBehP30"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_SPEAK_MOVES_I_MAKE,
				local.getString("chesBBehP31"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_SPEAK_MOVES_OPP_MAKES,
				local.getString("chesBBehP32"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_SPEAK_WHEN_OBSERVING,
				local.getString("chesBBehP33"),
				getFieldEditorParent()));
	}
}
