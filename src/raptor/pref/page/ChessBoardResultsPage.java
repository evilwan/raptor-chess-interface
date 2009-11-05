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
package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

public class ChessBoardResultsPage extends FieldEditorPreferencePage {

	public static final String[][] RESULTS_ANIMATION_DELAY_OPTIONS = {
			{ "100 milliseconds", "100" }, { "200 milliseconds", "200" },
			{ "300 milliseconds", "300" }, { "500 milliseconds", "500" },
			{ "750 milliseconds", "750" },
			{ "1 second", "1000" } };

	public static final String[][] RESULTS_PERCENTAGE = { { "50%", "50" },
			{ "60%", "60" }, { "70%", "70" }, { "75%", "75" }, { "80%", "80" },
			{ "85%", "85" }, { "90%", "90" }, { "95%", "95" }, };

	public ChessBoardResultsPage() {
		super(GRID);
		setTitle("Game End Result");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceKeys.RESULTS_FADE_AWAY_MODE,
				"Result fades away", getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.RESULTS_ANIMATION_DELAY,
				"Result animation delay:", RESULTS_ANIMATION_DELAY_OPTIONS,
				getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.RESULTS_WIDTH_PERCENTAGE,
				"Result percentage of square size:", RESULTS_PERCENTAGE,
				getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.RESULTS_COLOR,
				"Result Color:", getFieldEditorParent()));
		addField(new FontFieldEditor(PreferenceKeys.RESULTS_FONT,
				"Result Font", getFieldEditorParent()));
	}
}