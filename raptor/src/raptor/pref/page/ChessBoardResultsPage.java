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
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;

public class ChessBoardResultsPage extends FieldEditorPreferencePage {
 
	protected static L10n local = L10n.getInstance();
	
	public static final String[][] RESULTS_ANIMATION_DELAY_OPTIONS = {
		{ local.getString("xMilliseconds",100), "100" }, { local.getString("xMilliseconds",150), "150" },
		{ local.getString("xMilliseconds",175), "175" }, { local.getString("xMilliseconds",200), "200" },
		{ local.getString("xMilliseconds",250), "250" }, { local.getString("xMilliseconds",300), "300" },
		{ local.getString("xMilliseconds",350), "350" }, { local.getString("xMilliseconds",400), "400" },
		{ local.getString("xMilliseconds",500), "500" }, { local.getString("xMilliseconds",600), "600" },
		{ local.getString("xMilliseconds",700), "700" }, { local.getString("xMilliseconds",800), "800" },
		{ local.getString("xMilliseconds",900), "900" }, { local.getString("xMilliseconds",1000), "1000" }, 
		{ local.getString("xMilliseconds",1200), "1200" }, { local.getString("xMilliseconds",1400), "1400" },
		{ local.getString("xMilliseconds",1600), "1600" }, { local.getString("xMilliseconds",1800), "1800" },
		{ local.getString("xMilliseconds",2000), "2000" }, { local.getString("xMilliseconds",2500), "2500" },
		{ local.getString("xMilliseconds",3000), "3000" }, { local.getString("xMilliseconds",3500), "3500" },
		{ local.getString("xMilliseconds",4000), "4000" }, { local.getString("xMilliseconds",4500), "4500" }};

	public static final String[][] RESULTS_PERCENTAGE = { { "50%", "50" },
			{ "60%", "60" }, { "70%", "70" }, { "75%", "75" }, { "80%", "80" },
			{ "85%", "85" }, { "90%", "90" }, { "95%", "95" }, };

	public ChessBoardResultsPage() {
		super(GRID);
		setTitle(local.getString("results"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceKeys.RESULTS_FADE_AWAY_MODE,
				local.getString("resFedAw"), getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.RESULTS_ANIMATION_DELAY,
				local.getString("resAnDel"), RESULTS_ANIMATION_DELAY_OPTIONS,
				getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.RESULTS_WIDTH_PERCENTAGE,
				local.getString("resPreSz"), RESULTS_PERCENTAGE,
				getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.RESULTS_COLOR,
				local.getString("resCol"), getFieldEditorParent()));
		addField(new FontFieldEditor(PreferenceKeys.RESULTS_FONT,
				local.getString("resFnt"), getFieldEditorParent()));
	}
}