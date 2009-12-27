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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;

public class BughousePage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public BughousePage() {
		super(GRID);
		setTitle("Bughouse");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				PreferenceKeys.BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD,
				"Auto open partners board on games I observe",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD,
				"Auto open partners board on games I play",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BUGHOUSE_IS_PLAYING_PARTNERSHIP_OFFERED_SOUND,
				"Play partnership offered sound on partnership offers.",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_SHOW_BUGHOUSE_SIDE_UP_TIME,
				"Show up time indicator", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BUGHOUSE_SPEAK_COUNTDOWN_ON_PARTNER_BOARD,
				"Speak countdown on partners board.", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BUGHOUSE_SPEAK_PARTNER_TELLS,
				"Speak partner tells.", getFieldEditorParent()));

		addField(new FontFieldEditor(PreferenceKeys.BUG_BUTTONS_FONT,
				"Button Font:", getFieldEditorParent()));
	}
}