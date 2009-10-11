/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.pref;

//import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import raptor.Raptor;

public class ChessBoardClocksPage extends FieldEditorPreferencePage {
	public static final String[][] SHOW_SECONDS_OPTIONS = {
			{ "At 60 Minutes", "" + (60 * 60 * 1000 + 1) },
			{ "At 30 Minutes", "" + (30 * 60 * 1000 + 1) },
			{ "At 15 Minutes", "" + (15 * 60 * 1000 + 1) },
			{ "At 10 Minutes", "" + (10 * 10 * 1000 + 1) },
			{ "Always", "" + Long.MAX_VALUE } };

	public static final String[][] SHOW_TENTHS_OPTIONS = {
			{ "At 10 Seconds", "" + (10 * 1000 + 1) },
			{ "At 1 Minute", "" + (60 * 1000 + 1) },
			{ "At 3 Minute", "" + (3 * 60 * 1000 + 1) },
			{ "At 5 Minute", "" + (5 * 60 * 1000 + 1) },
			{ "At 10 Minute", "" + (10 * 60 * 1000 + 1) },
			{ "Always", "" + Long.MAX_VALUE } };

	public ChessBoardClocksPage() {
		// Use the "flat" layout
		super(GRID);
		setTitle("Clock Preferences");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS,
				"Play 10 second countdown sounds", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceKeys.BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN,
				"Show Seconds:", 3, SHOW_SECONDS_OPTIONS,
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceKeys.BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN,
				"Show Tenths of Seconds:", 3, SHOW_TENTHS_OPTIONS,
				getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR,
				"Active Clock Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(
				PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR,
				"Inactive Clock Color:", getFieldEditorParent()));

		addField(new FontFieldEditor(PreferenceKeys.BOARD_CLOCK_FONT,
				"Clock Font:", getFieldEditorParent()));
	}
}