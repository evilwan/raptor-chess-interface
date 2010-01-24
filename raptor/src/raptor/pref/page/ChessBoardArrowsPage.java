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
package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

public class ChessBoardArrowsPage extends FieldEditorPreferencePage {

	public static final String[][] ARROW_ANIMATION_DELAY_OPTIONS = {
			{ "10 milliseconds", "10" }, { "20 milliseconds", "20" },
			{ "30 milliseconds", "30" }, { "40 milliseconds", "40" },
			{ "50 milliseconds", "50" }, { "60 milliseconds", "60" },
			{ "70 milliseconds", "70" }, { "80 milliseconds", "80" },
			{ "90 milliseconds", "90" }, { "100 milliseconds", "100" },
			{ "125 millseconds", "125" }, { "150 millseconds", "150" },
			{ "175 millseconds", "175" }, { "200 millseconds", "200" } };

	public static final String[][] ARROW_BORDER_PERCENTAGE_OPTIONS = {
			{ "8%", "8" }, { "10%", "10" }, { "12%", "12" }, { "15%", "15" },
			{ "18%", "18" } };

	public ChessBoardArrowsPage() {
		super(GRID);
		setTitle("Arrows");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				PreferenceKeys.ARROW_SHOW_ON_OBS_AND_OPP_MOVES,
				"Show arrows on opponent and observed moves",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.ARROW_SHOW_ON_MY_MOVES,
				"Show arrows on my moves", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.ARROW_SHOW_ON_MOVE_LIST_MOVES,
				"Show arrows on move list moves", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.ARROW_SHOW_ON_MY_PREMOVES,
				"Show non fading arrows as my premoves are made",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.ARROW_FADE_AWAY_MODE,
				"Arrows fade away (excludes premove arrows)",
				getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.ARROW_ANIMATION_DELAY,
				"Arrow animation delay:", ARROW_ANIMATION_DELAY_OPTIONS,
				getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.ARROW_WIDTH_PERCENTAGE,
				"Arrow percentage of square size:",
				ARROW_BORDER_PERCENTAGE_OPTIONS, getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.ARROW_MY_COLOR,
				"My Arrow Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceKeys.ARROW_PREMOVE_COLOR,
				"My Premove Arrow Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceKeys.ARROW_OBS_OPP_COLOR,
				"Opponent Arrow Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceKeys.ARROW_OBS_COLOR,
				"Observe Arrow Color:", getFieldEditorParent()));
	}
}