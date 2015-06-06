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

public class RaptorWindowPage extends FieldEditorPreferencePage {
	
	protected static L10n local = L10n.getInstance();
	
	public RaptorWindowPage() {
		super(GRID);
		setTitle(local.getString("window"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {

		String[][] sliderWidthPreferences = { { local.getString("tiny"), "3" },
				{ local.getString("small"), "5" }, { local.getString("medium"), "8" }, { local.getString("large"), "11" },
				{ local.getString("extWide"), "15" } };
		ComboFieldEditor setFieldEditor = new ComboFieldEditor(
				PreferenceKeys.APP_SASH_WIDTH, local.getString("divSashWid"),
				sliderWidthPreferences, getFieldEditorParent());
		addField(setFieldEditor);

		ColorFieldEditor pingTimeColor = new ColorFieldEditor(
				PreferenceKeys.APP_PING_COLOR, local.getString("pingTFontCol"),
				getFieldEditorParent());
		addField(pingTimeColor);

		addField(pingTimeColor);
		FontFieldEditor pingTimeFont = new FontFieldEditor(
				PreferenceKeys.APP_PING_FONT, local.getString("pingTFont"),
				getFieldEditorParent());
		addField(pingTimeFont);

		ColorFieldEditor statusBarFontColor = new ColorFieldEditor(
				PreferenceKeys.APP_STATUS_BAR_COLOR, local.getString("statBarFontCol"),
				getFieldEditorParent());
		addField(statusBarFontColor);

		FontFieldEditor statusBarFont = new FontFieldEditor(
				PreferenceKeys.APP_STATUS_BAR_FONT, local.getString("statBarFont"),
				getFieldEditorParent());
		addField(statusBarFont);
		
		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_SHOW_STATUS_BAR,
				local.getString("showWinStar"),
				getFieldEditorParent()));

	}
}