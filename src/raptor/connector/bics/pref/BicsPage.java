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
package raptor.connector.bics.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.TextFieldEditor;
import raptor.pref.fields.ListFieldEditor;

public class BicsPage extends FieldEditorPreferencePage {
	public BicsPage() {
		super(FLAT);
		setTitle("Bics");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceKeys.BICS_AUTO_CONNECT,
				"Auto Connect", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BICS_CLOSE_TABS_ON_DISCONNECT,
				"Close all bics tabs on disconnect", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP,
				"Show bughouse buttons on partnerships (excluding simul)",
				getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
				PreferenceKeys.BICS_KEEP_ALIVE,
				"Keep Alive (Sends Keep-Alive command below to avoid the hour idle kickout)",
				getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceKeys.BICS_KEEP_ALIVE_COMMAND,
				"Keep-Alive command", getFieldEditorParent()));

		addField(new TextFieldEditor(PreferenceKeys.BICS_LOGIN_SCRIPT,
				"Login Script:", getFieldEditorParent()));

		addField(new ListFieldEditor(PreferenceKeys.BICS_PERSON_COMMANDS,
				"Right Click Person Commands:", getFieldEditorParent(), ',', 75));

		addField(new ListFieldEditor(PreferenceKeys.BICS_GAME_COMMANDS,
				"Right Click Game Commands:", getFieldEditorParent(), ',', 75));

		addField(new ListFieldEditor(PreferenceKeys.BICS_CHANNEL_COMMANDS,
				"Right Click Channel Commands:", getFieldEditorParent(), ',',
				75));

	}
}